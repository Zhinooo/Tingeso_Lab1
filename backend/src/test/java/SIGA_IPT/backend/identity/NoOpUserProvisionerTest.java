package SIGA_IPT.backend.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpUserProvisionerTest {

    @Test
    void doesNothingAndNeverFails(){
        NoOpUserProvisioner provisioner = new NoOpUserProvisioner();

        assertThatCode(() -> {
            assertThat(provisioner.create("12345678-9", "a@test.cl", "Ana", "Initial123", "STUDENT")).isNull();
            provisioner.updateProfile("u1", "b@test.cl", "Ana");
            provisioner.delete("u1");
        }).doesNotThrowAnyException();
    }
}
