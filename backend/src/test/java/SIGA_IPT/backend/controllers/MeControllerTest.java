package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.UserDTOs.MeResponseDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.support.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
class MeControllerTest extends ControllerTestBase {

    @MockitoBean
    private CurrentUserService currentUserService;

    @Test
    void returnsTheProfileOfTheAuthenticatedUserWithoutTheRolePrefix() throws Exception {
        when(currentUserService.getMe("student@siga.cl", "student", List.of("STUDENT")))
                .thenReturn(new MeResponseDTO("student@siga.cl", "student", "Student One", List.of("STUDENT"), 5L, null));

        get("/api/me", student())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Student One"))
                .andExpect(jsonPath("$.studentId").value(5))
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
    }

    @Test
    void everyRoleCanReadItsProfile() throws Exception {
        when(currentUserService.getMe("admin@siga.cl", "admin", List.of("ADMIN")))
                .thenReturn(new MeResponseDTO("admin@siga.cl", "admin", "admin", List.of("ADMIN"), null, null));

        get("/api/me", admin()).andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("ADMIN"));
        get("/api/me", teacher()).andExpect(status().isOk());
    }

    @Test
    void requiresAuthentication() throws Exception {
        get("/api/me", null).andExpect(status().isUnauthorized());
    }
}
