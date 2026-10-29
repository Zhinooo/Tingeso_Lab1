package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentResponseDTO;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.StudentService;
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

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest extends ControllerTestBase {

    @MockitoBean
    private StudentService studentService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"studentRun\":\"1-9\",\"studentFullname\":\"Student\",\"studentEmail\":\"s@siga.cl\",\"careerCode\":\"10450\"}";
    private static final String UPDATE_BODY = "{\"studentFullname\":\"New\",\"studentEmail\":\"s@siga.cl\"}";
    private static final String CAREER_BODY = "{\"careerCode\":\"10451\",\"studyplanIdentifier\":null}";

    private StudentResponseDTO studentDto(){
        return new StudentResponseDTO(1L, "1-9", "Student", "s@siga.cl", "10450", "2021.3", StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void adminCreatesAStudent() throws Exception {
        when(studentService.create(any())).thenReturn(studentDto());

        post("/api/students", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentStatus").value("REGULAR"));
    }

    @Test
    void studentCannotCreateAStudent() throws Exception {
        post("/api/students", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void creatingWithoutCurrentPlanBecomesConflict() throws Exception {
        when(studentService.create(any())).thenThrow(new IllegalStateException("The career has no current study plan."));

        post("/api/students", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void anUnavailableIdentityProviderBecomesBadGateway() throws Exception {
        when(studentService.create(any())).thenThrow(new SIGA_IPT.backend.exceptions.IdentityProviderException("down", null));

        post("/api/students", admin(), BODY)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("identity provider")));
    }

    @Test
    void duplicatedStudentBecomesBadRequest() throws Exception {
        when(studentService.create(any())).thenThrow(new IllegalArgumentException("A student with this RUN already exists"));

        post("/api/students", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminListsStudents() throws Exception {
        when(studentService.getAll()).thenReturn(List.of(studentDto()));

        get("/api/students", admin()).andExpect(status().isOk()).andExpect(jsonPath("$[0].studentRun").value("1-9"));
        get("/api/students", student()).andExpect(status().isForbidden());
        get("/api/students", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void studentReadsHisOwnRecord() throws Exception {
        when(currentUserService.requireStudentId("kc-student")).thenReturn(1L);
        when(studentService.getById(1L)).thenReturn(studentDto());

        get("/api/students/me", student()).andExpect(status().isOk()).andExpect(jsonPath("$.studentId").value(1));
    }

    @Test
    void userWithoutStudentRecordIsForbidden() throws Exception {
        when(currentUserService.requireStudentId(any())).thenThrow(new ForbiddenOperationException("No student record"));

        get("/api/students/me", student()).andExpect(status().isForbidden());
    }

    @Test
    void teacherCannotReadTheStudentMeEndpoint() throws Exception {
        get("/api/students/me", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsAStudentById() throws Exception {
        when(studentService.getById(1L)).thenReturn(studentDto());

        get("/api/students/1", admin()).andExpect(status().isOk());
    }

    @Test
    void studentCannotReadAnotherStudentById() throws Exception {
        get("/api/students/2", student()).andExpect(status().isForbidden());
    }

    @Test
    void missingStudentBecomesNotFound() throws Exception {
        when(studentService.getById(9L)).thenThrow(new EntityNotFoundException("Student not found: 9"));

        get("/api/students/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void adminUpdatesAStudent() throws Exception {
        when(studentService.update(eq(1L), any())).thenReturn(studentDto());

        put("/api/students/1", admin(), UPDATE_BODY).andExpect(status().isOk());
    }

    @Test
    void adminChangesCareerAndPlan() throws Exception {
        when(studentService.changeCareerAndPlan(eq(1L), any())).thenReturn(studentDto());

        put("/api/students/1/career", admin(), CAREER_BODY).andExpect(status().isOk());
    }

    @Test
    void changingCareerWithHistoryBecomesConflict() throws Exception {
        when(studentService.changeCareerAndPlan(eq(1L), any())).thenThrow(new IllegalStateException("has enrollments"));

        put("/api/students/1/career", admin(), CAREER_BODY).andExpect(status().isConflict());
    }

    @Test
    void adminChangesTheStatus() throws Exception {
        when(studentService.changeStatus(1L, StudentEntity.StudentStatus.DEFERRED)).thenReturn(studentDto());

        put("/api/students/1/status?status=DEFERRED", admin(), null).andExpect(status().isOk());
        verify(studentService).changeStatus(1L, StudentEntity.StudentStatus.DEFERRED);
    }

    @Test
    void manualGraduationBecomesBadRequest() throws Exception {
        when(studentService.changeStatus(1L, StudentEntity.StudentStatus.GRADUATED))
                .thenThrow(new IllegalArgumentException("cannot be set manually"));

        put("/api/students/1/status?status=GRADUATED", admin(), null).andExpect(status().isBadRequest());
    }

    @Test
    void studentCannotChangeStatuses() throws Exception {
        put("/api/students/1/status?status=REGULAR", student(), null).andExpect(status().isForbidden());
    }

    @Test
    void adminDeletesAStudent() throws Exception {
        delete("/api/students/1", admin()).andExpect(status().isNoContent());
        verify(studentService).delete(1L);
    }

    @Test
    void deletingAStudentWithHistoryBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("enrollments")).when(studentService).delete(1L);

        delete("/api/students/1", admin()).andExpect(status().isConflict());
    }
}
