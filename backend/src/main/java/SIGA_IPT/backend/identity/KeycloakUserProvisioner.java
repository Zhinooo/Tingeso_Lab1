package SIGA_IPT.backend.identity;

import SIGA_IPT.backend.exceptions.IdentityProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "keycloak.admin.enabled", havingValue = "true", matchIfMissing = true)
public class KeycloakUserProvisioner implements UserProvisioner {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserProvisioner.class);
    private static final long TOKEN_SAFETY_SECONDS = 30;

    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    @Autowired
    public KeycloakUserProvisioner(@Value("${keycloak.admin.base-url}") String baseUrl,
                                   @Value("${keycloak.admin.realm}") String realm,
                                   @Value("${keycloak.admin.client-id}") String clientId,
                                   @Value("${keycloak.admin.client-secret}") String clientSecret){
        this(RestClient.builder(), baseUrl, realm, clientId, clientSecret);
    }

    KeycloakUserProvisioner(RestClient.Builder restClientBuilder, String baseUrl, String realm, String clientId, String clientSecret){
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private synchronized String accessToken(){
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)){
            return cachedToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        Map<String, Object> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response == null || response.get("access_token") == null){
            throw new IdentityProviderException("Keycloak did not return an access token.", null);
        }
        cachedToken = response.get("access_token").toString();
        long expiresIn = response.get("expires_in") instanceof Number number ? number.longValue() : 60;
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn - TOKEN_SAFETY_SECONDS));
        return cachedToken;
    }

    private Map<String, String> nameParts(String fullName){
        String trimmed = fullName.trim();
        int separator = trimmed.indexOf(' ');
        if (separator < 0){
            return Map.of("firstName", trimmed, "lastName", trimmed);
        }
        return Map.of("firstName", trimmed.substring(0, separator), "lastName", trimmed.substring(separator + 1).trim());
    }

    private Optional<String> findUserId(String email){
        List<Map<String, Object>> users = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/admin/realms/{realm}/users")
                        .queryParam("email", "{email}")
                        .queryParam("exact", true)
                        .build(realm, email))
                .headers(headers -> headers.setBearerAuth(accessToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (users == null || users.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(users.get(0).get("id").toString());
    }

    private String createUser(String email, String fullName, String password){
        Map<String, Object> credential = Map.of("type", "password", "value", password, "temporary", true);
        Map<String, String> name = nameParts(fullName);
        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", name.get("firstName"),
                "lastName", name.get("lastName"),
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential));
        URI location = restClient.post()
                .uri("/admin/realms/{realm}/users", realm)
                .headers(headers -> headers.setBearerAuth(accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();
        if (location == null){
            throw new IdentityProviderException("Keycloak did not return the id of the new user.", null);
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void assignRole(String userId, String role){
        Map<String, Object> roleRepresentation = restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", realm, role)
                .headers(headers -> headers.setBearerAuth(accessToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        restClient.post()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", realm, userId)
                .headers(headers -> headers.setBearerAuth(accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteUser(String userId){
        restClient.delete()
                .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                .headers(headers -> headers.setBearerAuth(accessToken()))
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteQuietly(String userId){
        try {
            deleteUser(userId);
        } catch (RuntimeException ex){
            log.error("Could not remove the Keycloak user {}: {}", userId, ex.getMessage());
        }
    }

    private void afterRollback(Runnable action){
        if (!TransactionSynchronizationManager.isSynchronizationActive()){
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status){
                if (status != STATUS_COMMITTED){
                    action.run();
                }
            }
        });
    }

    private void afterCommit(Runnable action){
        if (!TransactionSynchronizationManager.isSynchronizationActive()){
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit(){
                action.run();
            }
        });
    }

    @Override
    public void create(String email, String fullName, String password, String role){
        String createdUserId = null;
        try {
            Optional<String> existing = findUserId(email);
            String userId = existing.orElseGet(() -> createUser(email, fullName, password));
            if (existing.isEmpty()){
                createdUserId = userId;
            }
            assignRole(userId, role);
        } catch (RestClientException | IdentityProviderException ex){
            if (createdUserId != null){
                deleteQuietly(createdUserId);
            }
            throw new IdentityProviderException("The identity provider could not create the user.", ex);
        }
        if (createdUserId != null){
            String idToRemove = createdUserId;
            afterRollback(() -> deleteQuietly(idToRemove));
        }
    }

    @Override
    public void updateProfile(String currentEmail, String newEmail, String fullName){
        try {
            Optional<String> userId = findUserId(currentEmail);
            if (userId.isEmpty()){
                return;
            }
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{id}", realm, userId.get())
                    .headers(headers -> headers.setBearerAuth(accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", newEmail,
                            "firstName", nameParts(fullName).get("firstName"),
                            "lastName", nameParts(fullName).get("lastName")))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex){
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)){
                throw new IllegalStateException("A user with this email already exists in the identity provider.");
            }
            throw new IdentityProviderException("The identity provider could not update the user.", ex);
        } catch (RestClientException | IdentityProviderException ex){
            throw new IdentityProviderException("The identity provider could not update the user.", ex);
        }
    }

    @Override
    public void delete(String email){
        afterCommit(() -> {
            try {
                findUserId(email).ifPresent(this::deleteUser);
            } catch (RuntimeException ex){
                log.error("Could not remove the Keycloak user of {}: {}", email, ex.getMessage());
            }
        });
    }
}
