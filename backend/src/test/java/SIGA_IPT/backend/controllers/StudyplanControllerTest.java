package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanResponseDTO;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.services.StudyplanService;
import SIGA_IPT.backend.support.ControllerTestBase;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyplanController.class)
@Import(SecurityConfig.class)
class StudyplanControllerTest extends ControllerTestBase {

    @MockitoBean
    private StudyplanService studyplanService;

    private static final String BODY = "{\"studyplanIdentifier\":\"2021.3\",\"careerCode\":\"10450\",\"current\":true}";

    private StudyplanResponseDTO plan(){
        return new StudyplanResponseDTO(1L, "2021.3", "10450", StudyplanEntity.StudyplanStatus.CURRENT);
    }

    @Test
    void adminCreatesAPlan() throws Exception {
        when(studyplanService.create(any())).thenReturn(plan());

        post("/api/studyplans", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyplanIdentifier").value("2021.3"))
                .andExpect(jsonPath("$.studyplanStatus").value("CURRENT"));
    }

    @Test
    void studentCannotCreateAPlan() throws Exception {
        post("/api/studyplans", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void creatingWithInvalidDataBecomesBadRequest() throws Exception {
        when(studyplanService.create(any())).thenThrow(new IllegalArgumentException("Indicate whether the study plan is current."));

        post("/api/studyplans", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void anyAuthenticatedUserListsPlans() throws Exception {
        when(studyplanService.getAll()).thenReturn(List.of(plan()));

        get("/api/studyplans", teacher()).andExpect(status().isOk()).andExpect(jsonPath("$[0].careerCode").value("10450"));
    }

    @Test
    void unauthenticatedUsersCannotListPlans() throws Exception {
        get("/api/studyplans", null).andExpect(status().isUnauthorized());
    }

    @Test
    void getByIdReturnsThePlan() throws Exception {
        when(studyplanService.getById(1L)).thenReturn(plan());

        get("/api/studyplans/1", student()).andExpect(status().isOk());
    }

    @Test
    void missingPlanBecomesNotFound() throws Exception {
        when(studyplanService.getById(9L)).thenThrow(new EntityNotFoundException("Study plan not found: 9"));

        get("/api/studyplans/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void adminUpdatesAPlan() throws Exception {
        when(studyplanService.update(eq(1L), any())).thenReturn(plan());

        put("/api/studyplans/1", admin(), BODY).andExpect(status().isOk());
    }

    @Test
    void adminChangesTheStatus() throws Exception {
        when(studyplanService.changeStatus(1L, StudyplanEntity.StudyplanStatus.NOT_CURRENT)).thenReturn(plan());

        put("/api/studyplans/1/status?status=NOT_CURRENT", admin(), null).andExpect(status().isOk());
        verify(studyplanService).changeStatus(1L, StudyplanEntity.StudyplanStatus.NOT_CURRENT);
    }

    @Test
    void anInvalidStatusBecomesBadRequest() throws Exception {
        put("/api/studyplans/1/status?status=SOMETIMES", admin(), null).andExpect(status().isBadRequest());
    }

    @Test
    void adminDeletesAPlan() throws Exception {
        delete("/api/studyplans/1", admin()).andExpect(status().isNoContent());
        verify(studyplanService).delete(1L);
    }

    @Test
    void deletingAPlanWithStudentsBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Cannot delete")).when(studyplanService).delete(1L);

        delete("/api/studyplans/1", admin()).andExpect(status().isConflict());
    }

    @Test
    void teacherCannotDeleteAPlan() throws Exception {
        delete("/api/studyplans/1", teacher()).andExpect(status().isForbidden());
    }
}
