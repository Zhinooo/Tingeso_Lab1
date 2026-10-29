package SIGA_IPT.backend.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "keycloak.admin.enabled", havingValue = "false")
public class NoOpUserProvisioner implements UserProvisioner {

    @Override
    public String create(String username, String email, String fullName, String password, String role){
        return null;
    }

    @Override
    public void updateProfile(String keycloakUserId, String newEmail, String fullName){
    }

    @Override
    public void delete(String keycloakUserId){
    }
}
