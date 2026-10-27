package SIGA_IPT.backend.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "keycloak.admin.enabled", havingValue = "false")
public class NoOpUserProvisioner implements UserProvisioner {

    @Override
    public void create(String email, String fullName, String password, String role){
    }

    @Override
    public void updateProfile(String currentEmail, String newEmail, String fullName){
    }

    @Override
    public void delete(String email){
    }
}
