package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionResponseDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.TuitionService;
import SIGA_IPT.backend.support.ControllerTestBase;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TuitionController.class)
@Import(SecurityConfig.class)
class TuitionControllerTest extends ControllerTestBase {

    @MockitoBean
    private TuitionService tuitionService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"studentId\":1,\"academicPeriodId\":1}";

    private TuitionResponseDTO tuition(){
        return new TuitionResponseDTO(1L, 1L, "1-9", 1L, 2030, 1, LocalDate.of(2030, 3, 1));
    }

    @Test
    void adminRegistersATuition() throws Exception {
        when(tuitionService.create(any())).thenReturn(tuition());

        post("/api/tuitions", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentRun").value("1-9"));
    }

    @Test
    void studentCannotRegisterATuition() throws Exception {
        post("/api/tuitions", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void registeringInAClosedPeriodBecomesConflict() throws Exception {
        when(tuitionService.create(any())).thenThrow(new IllegalStateException("the academic period is closed"));

        post("/api/tuitions", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void duplicatedTuitionBecomesBadRequest() throws Exception {
        when(tuitionService.create(any())).thenThrow(new IllegalArgumentException("already has a tuition"));

        post("/api/tuitions", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminListsTuitions() throws Exception {
        when(tuitionService.getAll()).thenReturn(List.of(tuition()));

        get("/api/tuitions", admin()).andExpect(status().isOk()).andExpect(jsonPath("$[0].tuitionId").value(1));
        get("/api/tuitions", student()).andExpect(status().isForbidden());
    }

    @Test
    void studentReadsHisOwnTuitions() throws Exception {
        when(currentUserService.requireStudentId("student@siga.cl")).thenReturn(1L);
        when(tuitionService.getByStudent(1L)).thenReturn(List.of(tuition()));

        get("/api/tuitions/me", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].studentId").value(1));
    }

    @Test
    void teacherCannotReadTheTuitionMeEndpoint() throws Exception {
        get("/api/tuitions/me", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsATuitionById() throws Exception {
        when(tuitionService.getById(1L)).thenReturn(tuition());

        get("/api/tuitions/1", admin()).andExpect(status().isOk());
    }

    @Test
    void missingTuitionBecomesNotFound() throws Exception {
        when(tuitionService.getById(9L)).thenThrow(new EntityNotFoundException("Tuition not found: 9"));

        get("/api/tuitions/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void adminReadsTuitionsByStudent() throws Exception {
        when(tuitionService.getByStudent(1L)).thenReturn(List.of(tuition()));

        get("/api/tuitions/student/1", admin()).andExpect(status().isOk());
        get("/api/tuitions/student/1", student()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsTuitionsByPeriod() throws Exception {
        when(tuitionService.getByAcademicPeriod(1L)).thenReturn(List.of(tuition()));

        get("/api/tuitions/academic-period/1", admin()).andExpect(status().isOk());
    }

    @Test
    void adminDeletesATuition() throws Exception {
        delete("/api/tuitions/1", admin()).andExpect(status().isNoContent());
        verify(tuitionService).delete(1L);
    }

    @Test
    void deletingATuitionWithEnrollmentsBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("enrollments")).when(tuitionService).delete(1L);

        delete("/api/tuitions/1", admin()).andExpect(status().isConflict());
    }
}
