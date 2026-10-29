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
    private static final String RUN = "12345678-9";
    private static final String EMAIL = "a@test.cl";

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

    private void expectSearch(String field, String value, String body){
        server.expect(requestTo(startsWith(USERS_URL + "?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam(field, value.replace("@", "%40")))
                .andExpect(queryParam("exact", "true"))
                .andExpect(header("Authorization", "Bearer tok"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void expectNoExistingUser(){
        expectSearch("email", EMAIL, "[]");
        expectSearch("username", RUN, "[]");
    }

    private void expectRoleAssignment(String userId, String role){
        server.expect(requestTo("http://kc/admin/realms/siga/roles/" + role))
                .andRespond(withSuccess("{\"id\":\"r1\",\"name\":\"" + role + "\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USERS_URL + "/" + userId + "/role-mappings/realm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$[0].name").value(role))
                .andRespond(withNoContent());
    }

    private void expectUserCreation(){
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.username").value(RUN))
                .andExpect(jsonPath("$.email").value(EMAIL))
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
    void createRegistersANewUserWithTheRunAsUsernameATemporaryPasswordAndTheRole(){
        expectToken();
        expectNoExistingUser();
        expectUserCreation();
        expectRoleAssignment("u1", "STUDENT");

        String userId = provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT");

        assertThat(userId).isEqualTo("u1");
        server.verify();
    }

    @Test
    void aSingleWordNameIsUsedAsFirstAndLastName(){
        expectToken();
        expectNoExistingUser();
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.firstName").value("Madonna"))
                .andExpect(jsonPath("$.lastName").value("Madonna"))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u1")));
        expectRoleAssignment("u1", "STUDENT");

        provisioner.create(RUN, EMAIL, "  Madonna ", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void aCompoundSurnameStaysTogether(){
        expectToken();
        expectNoExistingUser();
        server.expect(requestTo(USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("De La Cruz Soto"))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u1")));
        expectRoleAssignment("u1", "STUDENT");

        provisioner.create(RUN, EMAIL, "Ana De La Cruz Soto", "Initial123", "STUDENT");

        server.verify();
    }

    @Test
    void createReusesAUserWithTheSameEmailAndOnlyEnsuresTheRole(){
        expectToken();
        expectSearch("email", EMAIL, "[{\"id\":\"u1\"}]");
        expectRoleAssignment("u1", "TEACHER");
        server.expect(never(), requestTo(USERS_URL)).andExpect(method(HttpMethod.POST));

        String userId = provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "TEACHER");

        assertThat(userId).isEqualTo("u1");
        server.verify();
    }

    @Test
    void createReusesAUserWithTheSameRunWhenTheEmailIsNew(){
        expectToken();
        expectSearch("email", EMAIL, "[]");
        expectSearch("username", RUN, "[{\"id\":\"u1\"}]");
        expectRoleAssignment("u1", "TEACHER");
        server.expect(never(), requestTo(USERS_URL)).andExpect(method(HttpMethod.POST));

        String userId = provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "TEACHER");

        assertThat(userId).isEqualTo("u1");
        server.verify();
    }

    @Test
    void theAdminTokenIsRequestedOnlyOnceWhileItIsValid(){
        expectToken();
        expectSearch("email", EMAIL, "[{\"id\":\"u1\"}]");
        expectRoleAssignment("u1", "STUDENT");
        expectSearch("email", "b@test.cl", "[]");
        expectSearch("username", "87654321-k", "[]");
        server.expect(requestTo(USERS_URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withCreatedEntity(URI.create(USERS_URL + "/u2")));
        expectRoleAssignment("u2", "STUDENT");

        assertThat(provisioner.create(RUN, EMAIL, "Ana", "Initial123", "STUDENT")).isEqualTo("u1");
        assertThat(provisioner.create("87654321-k", "b@test.cl", "Beto", "Initial123", "STUDENT")).isEqualTo("u2");

        server.verify();
    }

    @Test
    void createFailsWhenKeycloakCannotGiveAToken(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.create(RUN, EMAIL, "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createFailsWhenTheTokenResponseHasNoAccessToken(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provisioner.create(RUN, EMAIL, "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createFailsWhenKeycloakDoesNotReturnTheNewUserId(){
        expectToken();
        expectNoExistingUser();
        server.expect(requestTo(USERS_URL)).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.CREATED));

        assertThatThrownBy(() -> provisioner.create(RUN, EMAIL, "Ana", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void createRemovesTheNewUserWhenTheRoleCannotBeAssigned(){
        expectToken();
        expectNoExistingUser();
        expectUserCreation();
        server.expect(requestTo("http://kc/admin/realms/siga/roles/STUDENT")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        assertThatThrownBy(() -> provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT"))
                .isInstanceOf(IdentityProviderException.class);

        server.verify();
    }

    @Test
    void createRemovesTheNewUserWhenTheTransactionRollsBack(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectNoExistingUser();
        expectUserCreation();
        expectRoleAssignment("u1", "STUDENT");
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void createKeepsTheNewUserWhenTheTransactionCommits(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectNoExistingUser();
        expectUserCreation();
        expectRoleAssignment("u1", "STUDENT");
        server.expect(never(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE));

        provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        server.verify();
    }

    @Test
    void createKeepsAReusedUserWhenTheTransactionRollsBack(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectSearch("email", EMAIL, "[{\"id\":\"u1\"}]");
        expectRoleAssignment("u1", "STUDENT");
        server.expect(never(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE));

        provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void rollbackCleanupNeverBreaksWhenKeycloakFailsToDelete(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        expectNoExistingUser();
        expectUserCreation();
        expectRoleAssignment("u1", "STUDENT");
        server.expect(requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withServerError());

        provisioner.create(RUN, EMAIL, "Ana Perez", "Initial123", "STUDENT");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void updateProfileChangesTheEmailAndTheNameOfTheLinkedUser(){
        expectToken();
        server.expect(requestTo(USERS_URL + "/u1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.email").value("new@test.cl"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andRespond(withNoContent());
        server.expect(never(), requestTo(startsWith(USERS_URL + "?")));

        provisioner.updateProfile("u1", "new@test.cl", "New Name");

        server.verify();
    }

    @Test
    void updateProfileDoesNothingWithoutALinkedUser(){
        provisioner.updateProfile(null, "new@test.cl", "New Name");

        server.verify();
    }

    @Test
    void updateProfileRejectsAnEmailAlreadyUsedInKeycloak(){
        expectToken();
        server.expect(requestTo(USERS_URL + "/u1")).andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> provisioner.updateProfile("u1", "taken@test.cl", "Name"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already exists");
    }

    @Test
    void updateProfileReportsOtherKeycloakErrors(){
        expectToken();
        server.expect(requestTo(USERS_URL + "/u1")).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.updateProfile("u1", "new@test.cl", "Name"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void updateProfileReportsAnUnavailableKeycloak(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> provisioner.updateProfile("u1", "new@test.cl", "Name"))
                .isInstanceOf(IdentityProviderException.class);
    }

    @Test
    void deleteRemovesTheLinkedUserImmediatelyWithoutATransaction(){
        expectToken();
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.delete("u1");

        server.verify();
    }

    @Test
    void deleteWaitsForTheCommitInsideATransaction(){
        TransactionSynchronizationManager.initSynchronization();
        expectToken();
        server.expect(once(), requestTo(USERS_URL + "/u1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());

        provisioner.delete("u1");
        assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        server.verify();
    }

    @Test
    void deleteDoesNothingWhenTheTransactionRollsBack(){
        TransactionSynchronizationManager.initSynchronization();

        provisioner.delete("u1");
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        server.verify();
    }

    @Test
    void deleteDoesNothingWithoutALinkedUser(){
        TransactionSynchronizationManager.initSynchronization();

        provisioner.delete(null);

        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        server.verify();
    }

    @Test
    void deleteNeverThrowsWhenKeycloakFails(){
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        provisioner.delete("u1");

        server.verify();
    }
}
