package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.services.AcademicPeriodClosingService;
import SIGA_IPT.backend.services.AcademicPeriodService;
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

@WebMvcTest({AcademicPeriodController.class, AcademicPeriodClosingController.class})
@Import(SecurityConfig.class)
class AcademicPeriodControllerTest extends ControllerTestBase {

    @MockitoBean
    private AcademicPeriodService academicPeriodService;
    @MockitoBean
    private AcademicPeriodClosingService closingService;

    private static final String BODY = "{\"academicYear\":2030,\"academicSemester\":1}";

    private AcademicPeriodResponseDTO period(AcademicPeriodEntity.AcademicPeriodStatus status){
        return new AcademicPeriodResponseDTO(1L, 2030, 1, status);
    }

    @Test
    void adminCreatesAPeriod() throws Exception {
        when(academicPeriodService.create(any())).thenReturn(period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        post("/api/academic-periods", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.academicPeriodStatus").value("OPEN"));
    }

    @Test
    void teacherCannotCreateAPeriod() throws Exception {
        post("/api/academic-periods", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void duplicatedPeriodBecomesBadRequest() throws Exception {
        when(academicPeriodService.create(any())).thenThrow(new IllegalArgumentException("This academic period already exists"));

        post("/api/academic-periods", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void anyAuthenticatedUserListsPeriods() throws Exception {
        when(academicPeriodService.getAll()).thenReturn(List.of(period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN)));

        get("/api/academic-periods", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].academicYear").value(2030));
    }

    @Test
    void getByIdReturnsThePeriod() throws Exception {
        when(academicPeriodService.getById(1L)).thenReturn(period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        get("/api/academic-periods/1", teacher()).andExpect(status().isOk());
    }

    @Test
    void missingPeriodBecomesNotFound() throws Exception {
        when(academicPeriodService.getById(9L)).thenThrow(new EntityNotFoundException("Academic period not found: 9"));

        get("/api/academic-periods/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void adminUpdatesAPeriod() throws Exception {
        when(academicPeriodService.update(eq(1L), any())).thenReturn(period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        put("/api/academic-periods/1", admin(), BODY).andExpect(status().isOk());
    }

    @Test
    void updatingAClosedPeriodBecomesConflict() throws Exception {
        when(academicPeriodService.update(eq(1L), any())).thenThrow(new IllegalStateException("closed"));

        put("/api/academic-periods/1", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void adminDeletesAPeriod() throws Exception {
        delete("/api/academic-periods/1", admin()).andExpect(status().isNoContent());
        verify(academicPeriodService).delete(1L);
    }

    @Test
    void studentCannotDeleteAPeriod() throws Exception {
        delete("/api/academic-periods/1", student()).andExpect(status().isForbidden());
    }

    @Test
    void adminClosesAPeriod() throws Exception {
        when(closingService.close(1L)).thenReturn(period(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED));

        put("/api/academic-periods/1/close", admin(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicPeriodStatus").value("CLOSED"));
    }

    @Test
    void closingWithPendingGradesBecomesConflict() throws Exception {
        when(closingService.close(1L)).thenThrow(new IllegalStateException("pending final grades"));

        put("/api/academic-periods/1/close", admin(), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("pending final grades"));
    }

    @Test
    void teacherCannotCloseAPeriod() throws Exception {
        put("/api/academic-periods/1/close", teacher(), null).andExpect(status().isForbidden());
    }

    @Test
    void studentCannotCloseAPeriod() throws Exception {
        put("/api/academic-periods/1/close", student(), null).andExpect(status().isForbidden());
    }
}
