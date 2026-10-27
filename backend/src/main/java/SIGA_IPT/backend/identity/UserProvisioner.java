package SIGA_IPT.backend.identity;

public interface UserProvisioner {

    void create(String email, String fullName, String password, String role);

    void updateProfile(String currentEmail, String newEmail, String fullName);

    void delete(String email);
}
