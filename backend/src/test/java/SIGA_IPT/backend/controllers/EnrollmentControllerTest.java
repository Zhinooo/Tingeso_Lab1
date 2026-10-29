package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentRequestDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.EnrollmentService;
import SIGA_IPT.backend.support.ControllerTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class)
class EnrollmentControllerTest extends ControllerTestBase {

    @MockitoBean
    private EnrollmentService enrollmentService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"academicPeriodId\":1,\"courseIds\":[1,2,3]}";

    @BeforeEach
    void setUp(){
        when(currentUserService.requireStudentId("kc-student")).thenReturn(1L);
        when(currentUserService.requireTeacherId("kc-teacher")).thenReturn(7L);
    }

    private EnrollmentResponseDTO enrollment(){
        return new EnrollmentResponseDTO(1L, 1L, "1-9", 1L, "TPI101", "Programming", 1L, 1L, 2030, 1,
                LocalDate.of(2030, 3, 1), null, null);
    }

    @Test
    void studentEnrollsUsingTheIdFromHisToken() throws Exception {
        when(enrollmentService.create(any(), any())).thenReturn(List.of(enrollment()));

        post("/api/enrollments", student(), "{\"studentId\":99,\"academicPeriodId\":1,\"courseIds\":[1,2,3]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].courseCode").value("TPI101"));

        ArgumentCaptor<EnrollmentRequestDTO> dto = ArgumentCaptor.forClass(EnrollmentRequestDTO.class);
        verify(enrollmentService).create(org.mockito.ArgumentMatchers.eq(1L), dto.capture());
        assertThat(dto.getValue().courseIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void adminCannotEnroll() throws Exception {
        post("/api/enrollments", admin(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void teacherCannotEnroll() throws Exception {
        post("/api/enrollments", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void enrollmentRuleViolationsBecomeConflict() throws Exception {
        when(enrollmentService.create(any(), any())).thenThrow(new IllegalStateException("Schedule conflict"));

        post("/api/enrollments", student(), BODY)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Schedule conflict"));
    }

    @Test
    void invalidEnrollmentBecomesBadRequest() throws Exception {
        when(enrollmentService.create(any(), any())).thenThrow(new IllegalArgumentException("At least one course is required."));

        post("/api/enrollments", student(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void userWithoutStudentRecordCannotEnroll() throws Exception {
        when(currentUserService.requireStudentId(any())).thenThrow(new ForbiddenOperationException("No student record"));

        post("/api/enrollments", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminListsAllEnrollments() throws Exception {
        when(enrollmentService.getAll()).thenReturn(List.of(enrollment()));

        get("/api/enrollments", admin()).andExpect(status().isOk());
        get("/api/enrollments", student()).andExpect(status().isForbidden());
        get("/api/enrollments", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsAnEnrollmentById() throws Exception {
        when(enrollmentService.getById(1L)).thenReturn(enrollment());

        get("/api/enrollments/1", admin()).andExpect(status().isOk());
    }

    @Test
    void adminReadsEnrollmentsOfAStudentAndTheirHistory() throws Exception {
        when(enrollmentService.getByStudent(1L)).thenReturn(List.of(enrollment()));
        when(enrollmentService.getAcademicHistory(1L)).thenReturn(List.of(enrollment()));

        get("/api/enrollments/student/1", admin()).andExpect(status().isOk());
        get("/api/enrollments/student/1/history", admin()).andExpect(status().isOk());
        get("/api/enrollments/student/1", student()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsEnrollmentsByPeriodAndSection() throws Exception {
        when(enrollmentService.getByAcademicPeriod(1L)).thenReturn(List.of(enrollment()));
        when(enrollmentService.getBySection(1L)).thenReturn(List.of(enrollment()));

        get("/api/enrollments/academic-period/1", admin()).andExpect(status().isOk());
        get("/api/enrollments/section/1", admin()).andExpect(status().isOk());
        get("/api/enrollments/section/1", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void studentReadsHisOwnEnrollmentsAndHistory() throws Exception {
        when(enrollmentService.getByStudent(1L)).thenReturn(List.of(enrollment()));
        when(enrollmentService.getAcademicHistory(1L)).thenReturn(List.of(enrollment()));

        get("/api/enrollments/me", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].studentId").value(1));
        get("/api/enrollments/me/history", student()).andExpect(status().isOk());
    }

    @Test
    void teacherCannotReadTheStudentMeEndpoints() throws Exception {
        get("/api/enrollments/me", teacher()).andExpect(status().isForbidden());
        get("/api/enrollments/me/history", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void studentReadsHisAvailableOffer() throws Exception {
        AvailableCourseDTO offer = new AvailableCourseDTO(1L, "TPI101", "Programming", 1, 1L, "Teacher One",
                List.of("L1", "W1"), 30, 25);
        when(enrollmentService.getAvailableOffer(1L, 1L)).thenReturn(List.of(offer));

        get("/api/enrollments/me/available?academicPeriodId=1", student())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availableSeats").value(25));
    }

    @Test
    void availableOfferRequiresThePeriod() throws Exception {
        get("/api/enrollments/me/available", student()).andExpect(status().isBadRequest());
    }

    @Test
    void teacherReadsTheEnrollmentsOfHisSection() throws Exception {
        when(enrollmentService.getBySectionForTeacher(5L, 7L)).thenReturn(List.of(enrollment()));

        get("/api/enrollments/me/section/5", teacher()).andExpect(status().isOk());
    }

    @Test
    void teacherCannotReadSectionsOfAnotherTeacher() throws Exception {
        when(enrollmentService.getBySectionForTeacher(5L, 7L)).thenThrow(new ForbiddenOperationException("Only the teacher in charge"));

        get("/api/enrollments/me/section/5", teacher())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void studentCannotReadSectionEnrollments() throws Exception {
        get("/api/enrollments/me/section/5", student()).andExpect(status().isForbidden());
    }

    @Test
    void studentCancelsHisEnrollment() throws Exception {
        delete("/api/enrollments/3", student()).andExpect(status().isNoContent());
        verify(enrollmentService).cancel(3L, 1L);
    }

    @Test
    void cancellingAnotherStudentsEnrollmentIsForbidden() throws Exception {
        org.mockito.Mockito.doThrow(new ForbiddenOperationException("own student")).when(enrollmentService).cancel(3L, 1L);

        delete("/api/enrollments/3", student()).andExpect(status().isForbidden());
    }

    @Test
    void cancellingAGradedEnrollmentBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("already has a final grade")).when(enrollmentService).cancel(3L, 1L);

        delete("/api/enrollments/3", student()).andExpect(status().isConflict());
    }

    @Test
    void adminAndTeacherCannotCancelEnrollments() throws Exception {
        delete("/api/enrollments/3", admin()).andExpect(status().isForbidden());
        delete("/api/enrollments/3", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void enrollmentResultIsExposedWhenGraded() throws Exception {
        EnrollmentResponseDTO graded = new EnrollmentResponseDTO(1L, 1L, "1-9", 1L, "TPI101", "Programming", 1L, 1L, 2030, 1,
                LocalDate.of(2030, 3, 1), 5.5, EnrollmentEntity.EnrollmentResult.APPROVED);
        when(enrollmentService.getById(1L)).thenReturn(graded);

        get("/api/enrollments/1", admin())
                .andExpect(jsonPath("$.finalGrade").value(5.5))
                .andExpect(jsonPath("$.result").value("APPROVED"));
    }
}
