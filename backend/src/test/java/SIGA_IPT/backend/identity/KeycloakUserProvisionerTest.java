package SIGA_IPT.backend.identity;

import SIGA_IPT.backend.exceptions.IdentityProviderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KeycloakUserProvisionerTest {

    private static final String TOKEN_URL = "http://kc/realms/siga/protocol/openid-connect/token";
    private static final String USERS_URL = "http://kc/admin/realms/siga/users";
    private static final String TOKEN_JSON = "{\"access_token\":\"tok\",\"expires_in\":300}";

    private MockRestServiceServer server;
    private KeycloakUserProvisioner provisioner;

    @BeforeEach
    void setUp(){
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        provisioner = new KeycloakUserProvisioner(builder, "http://kc", "siga", "siga-backend", "secret");
    }

    @AfterEach
    void tearDown(){
        if (TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void expectToken(){
        server.expect(once(), requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(java.util.Map.of("grant_type", "client_credentials",
                        "client_id", "siga-backend", "client_secret", "secret")))
                .andRespond(withSuccess(TOKEN_JSON, MediaType.APPLICATION_JSON));
    }

    private void expectSearch(String email, String body){
        server.expect(requestTo(startsWith(USERS_URL + "?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("email", email.replace("@", "%40")))
                .andExpect(queryParam("exact", "true"))
                .andExpect(header("Authorization", "Bearer tok"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void expectRoleAssignment(String role){
        server.expect(requestTo("http://kc/admin/realms/siga/roles/" + role))
                .andRespond(withSuccess("{\"id\":\"r1\",\"name\":\"" + role + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://kc/admin/realms/siga/users/u1/role-mappings/realm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$[0].name").value(role))
                .andRespond(withNoContent());
    }

    private void expectUserCreation(){
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.username").value("a@test.cl"))
                .andExpect(jsonPath("$.email").value("a@test.cl"))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Perez"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.credentials[0].value").value("Initial123"))
                .andExpect(jsonPath("$.credentials[0].temporary").value(true))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u1")));
    }

    private void completeTransaction(int status){
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()){
            if (status == TransactionSynchronization.STATUS_COMMITTED){
                synchronization.afterCommit();
            }
            synchronization.afterCompletion(status);
        }
    }

    @Test
    void createRegistersANewUserWithATemporaryPasswordAndTheRole(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        expectUserCreation();
        expectRoleAssignment("STUDENT");

        provisioner.create("a@test.cl", "Ana Perez", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void aSingleWordNameIsUsedAsFirstAndLastName(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.firstName").value("Madonna"))
                .andExpect(jsonPath("$.lastName").value("Madonna"))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u1")));
        expectRoleAssignment("STUDENT");

        provisioner.create("a@test.cl", "  Madonna ", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void aCompoundSurnameStaysTogether(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("De La Cruz Soto"))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u1")));
        expectRoleAssignment("STUDENT");

        provisioner.create("a@test.cl", "Ana De La Cruz Soto", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void createReusesAnExistingUserAndOnlyEnsuresTheRole(){
        expectToken();
        expectSearch("a@test.cl", "[{\"id\":\"u1\"}]");
        expectRoleAssignment("TEACHER");
        server.expect(never(), requestTo(USERS_URL)).andExpect(method(HttpMethod.POST));

        provisioner.create("a@test.cl", "Ana Perez", "Initial123", "TEACHER");

        server.verify();
    }

    @Test
    void theAdminTokenIsRequestedOnlyOnceWhileItIsValid(){
        expectToken();
        expectSearch("a@test.cl", "[{\"id\":\"u1\"}]");
        expectSearch("b@test.cl", "[]");
        expectRoleAssignment("STUDENT");
        server.expect(requestTo(USERS_URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u2")));
        server.expect(requestTo("http://kc/admin/realms/siga/roles/STUDENT"))
                .andRespond(withSuccess("{\"id\":\"r1\",\"name\":\"STUDENT\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://kc/admin/realms/siga/users/u2/role-mappings/realm"))
                .andRespond(withNoContent());

        provisioner.create("a@test.cl", "Ana", "Initial123", "STUDENT");
        provisioner.create("b@test.cl", "Beto", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void createFailsWhenKeycloakCannotGiveAToken(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.create("a@test.cl", "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createFailsWhenTheTokenResponseHasNoAccessToken(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provisioner.create("a@test.cl", "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createFailsWhenKeycloakDoesNotReturnTheNewUserId(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        server.expect(requestTo(USERS_URL)).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.CREATED));

        assertThatThrownBy(() -> provisioner.create("a@test.cl", "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createRemovesTheNewUserWhenTheRoleCannotBeAssigned(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        expectUserCreation();
        server.expect(requestTo("http://kc/admin/realms/siga/roles/STUDENT")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        assertThatThrownBy(() -> provisioner.create("a@test.cl", "Ana Perez", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);

        server.verify();
    }

    @Test
    void createRemovesTheNewUserWhenTheTransactionRollsBack(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectSearch("a@test.cl", "[]");
        expectUserCreation();
        expectRoleAssignment("STUDENT");
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.create("a@test.cl", "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void createKeepsTheNewUserWhenTheTransactionCommits(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectSearch("a@test.cl", "[]");
        expectUserCreation();
        expectRoleAssignment("STUDENT");
        server.expect(never(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE));

        provisioner.create("a@test.cl", "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        server.verify();
    }

    @Test
    void rollbackCleanupNeverBreaksWhenKeycloakFailsToDelete(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectSearch("a@test.cl", "[]");
        expectUserCreation();
        expectRoleAssignment("STUDENT");
        server.expect(requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withServerError());

        provisioner.create("a@test.cl", "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void updateProfileChangesTheEmailAndTheName(){
        expectToken();
        expectSearch("old@test.cl", "[{\"id\":\"u1\"}]");
        server.expect(requestTo(USERS_URL + "/u1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.email").value("new@test.cl"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andRespond(withNoContent());

        provisioner.updateProfile("old@test.cl", "new@test.cl", "New Name");

        server.verify();
    }

    @Test
    void updateProfileDoesNothingWhenThePersonHasNoKeycloakUser(){
        expectToken();
        expectSearch("old@test.cl", "[]");
        server.expect(never(), requestTo(containsString("/users/"))).andExpect(method(HttpMethod.PUT));

        provisioner.updateProfile("old@test.cl", "new@test.cl", "New Name");

        server.verify();
    }

    @Test
    void updateProfileRejectsAnEmailAlreadyUsedInKeycloak(){
        expectToken();
        expectSearch("old@test.cl", "[{\"id\":\"u1\"}]");
        server.expect(requestTo(USERS_URL + "/u1")).andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> provisioner.updateProfile("old@test.cl", "taken@test.cl", "Name"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already exists");
    }

    @Test
    void updateProfileReportsOtherKeycloakErrors(){
        expectToken();
        expectSearch("old@test.cl", "[{\"id\":\"u1\"}]");
        server.expect(requestTo(USERS_URL + "/u1")).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.updateProfile("old@test.cl", "new@test.cl", "Name"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void updateProfileReportsAnUnavailableKeycloak(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.updateProfile("old@test.cl", "new@test.cl", "Name"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void deleteRemovesTheUserImmediatelyWithoutATransaction(){
        expectToken();
        expectSearch("a@test.cl", "[{\"id\":\"u1\"}]");
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.delete("a@test.cl");

        server.verify();
    }

    @Test
    void deleteWaitsForTheCommitInsideATransaction(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectSearch("a@test.cl", "[{\"id\":\"u1\"}]");
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.delete("a@test.cl");
        assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        server.verify();
    }

    @Test
    void deleteDoesNothingWhenTheTransactionRollsBack(){
        TransactionSynchronizationManager.initSynchronization();

        provisioner.delete("a@test.cl");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void deleteIgnoresPeopleWithoutAKeycloakUser(){
        expectToken();
        expectSearch("a@test.cl", "[]");
        server.expect(never(), requestTo(containsString("/users/"))).andExpect(method(HttpMethod.DELETE));

        provisioner.delete("a@test.cl");

        server.verify();
    }

    @Test
    void deleteNeverThrowsWhenKeycloakFails(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        provisioner.delete("a@test.cl");

        server.verify();
    }
}
