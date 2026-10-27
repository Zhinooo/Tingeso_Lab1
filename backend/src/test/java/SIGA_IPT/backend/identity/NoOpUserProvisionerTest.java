package SIGA_IPT.backend.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpUserProvisionerTest {

    @Test
    void doesNothingAndNeverFails(){
        NoOpUserProvisioner provisioner = new NoOpUserProvisioner();

        assertThatCode(() -> {
            provisioner.create("a@test.cl", "Ana", "Initial123", "STUDENT");
            provisioner.updateProfile("a@test.cl", "b@test.cl", "Ana");
            provisioner.delete("b@test.cl");
        }).doesNotThrowAnyException();
    }
}
