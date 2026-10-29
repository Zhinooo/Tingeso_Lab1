package SIGA_IPT.backend.identity;

public interface UserProvisioner {

    String create(String username, String email, String fullName, String password, String role);

    void updateProfile(String keycloakUserId, String newEmail, String fullName);

    void delete(String keycloakUserId);
}
