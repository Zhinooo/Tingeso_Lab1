package SIGA_IPT.backend;

import SIGA_IPT.backend.identity.KeycloakUserProvisioner;
import SIGA_IPT.backend.identity.UserProvisioner;
import SIGA_IPT.backend.support.MySqlContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BackendApplicationTests extends MySqlContainerBase {

	@Autowired
	private UserProvisioner userProvisioner;

	@Test
	void contextLoads() {
	}

	@Test
	void theKeycloakProvisionerIsWiredByDefault() {
		assertThat(userProvisioner).isInstanceOf(KeycloakUserProvisioner.class);
	}

}
