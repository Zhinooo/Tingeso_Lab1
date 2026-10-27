package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.GradeService;
import SIGA_IPT.backend.support.ControllerTestBase;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GradeController.class)
@Import(SecurityConfig.class)
class GradeControllerTest extends ControllerTestBase {

    @MockitoBean
    private GradeService gradeService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"finalGrade\":5.5}";

    @BeforeEach
    void setUp(){
        when(currentUserService.requireTeacherId("teacher@siga.cl")).thenReturn(7L);
    }

    private EnrollmentResponseDTO graded(){
        return new EnrollmentResponseDTO(1L, 1L, "1-9", 1L, "TPI101", "Programming", 1L, 1L, 2030, 1,
                LocalDate.of(2030, 3, 1), 5.5, EnrollmentEntity.EnrollmentResult.APPROVED);
    }

    @Test
    void teacherRegistersAGradeUsingTheIdFromHisToken() throws Exception {
        when(gradeService.registerGrade(eq(1L), eq(7L), any())).thenReturn(graded());

        put("/api/enrollments/1/grade?teacherId=99", teacher(), BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("APPROVED"));
        verify(gradeService).registerGrade(eq(1L), eq(7L), any());
    }

    @Test
    void adminCannotRegisterGrades() throws Exception {
        put("/api/enrollments/1/grade", admin(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void studentCannotRegisterGrades() throws Exception {
        put("/api/enrollments/1/grade", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUsersCannotRegisterGrades() throws Exception {
        put("/api/enrollments/1/grade", null, BODY).andExpect(status().isUnauthorized());
    }

    @Test
    void gradingAnotherTeachersSectionIsForbidden() throws Exception {
        when(gradeService.registerGrade(eq(1L), eq(7L), any())).thenThrow(new ForbiddenOperationException("Only the teacher in charge"));

        put("/api/enrollments/1/grade", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void gradeOutOfRangeBecomesBadRequest() throws Exception {
        when(gradeService.registerGrade(eq(1L), eq(7L), any())).thenThrow(new IllegalArgumentException("between 1.0 and 7.0"));

        put("/api/enrollments/1/grade", teacher(), "{\"finalGrade\":8.0}").andExpect(status().isBadRequest());
    }

    @Test
    void gradingInAClosedPeriodBecomesConflict() throws Exception {
        when(gradeService.registerGrade(eq(1L), eq(7L), any())).thenThrow(new IllegalStateException("closed"));

        put("/api/enrollments/1/grade", teacher(), BODY).andExpect(status().isConflict());
    }

    @Test
    void gradingAMissingEnrollmentBecomesNotFound() throws Exception {
        when(gradeService.registerGrade(eq(9L), eq(7L), any())).thenThrow(new EntityNotFoundException("Enrollment not found: 9"));

        put("/api/enrollments/9/grade", teacher(), BODY).andExpect(status().isNotFound());
    }

    @Test
    void malformedJsonBecomesBadRequest() throws Exception {
        put("/api/enrollments/1/grade", teacher(), "{not json").andExpect(status().isBadRequest());
    }
}
