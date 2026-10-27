package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherResponseDTO;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.TeacherService;
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

@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest extends ControllerTestBase {

    @MockitoBean
    private TeacherService teacherService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"teacherRun\":\"11-1\",\"teacherFullname\":\"Teacher\",\"teacherEmail\":\"t@siga.cl\","
            + "\"teacherProtitle\":\"Eng\",\"teacherDegree\":\"MSc\"}";
    private static final String UPDATE_BODY = "{\"teacherFullname\":\"New\",\"teacherEmail\":\"t@siga.cl\","
            + "\"teacherProtitle\":\"Eng\",\"teacherDegree\":\"PhD\"}";

    private TeacherResponseDTO teacherDto(){
        return new TeacherResponseDTO(1L, "11-1", "Teacher", "t@siga.cl", "Eng", "MSc", TeacherEntity.TeacherStatus.ACTIVE);
    }

    @Test
    void adminCreatesATeacher() throws Exception {
        when(teacherService.create(any())).thenReturn(teacherDto());

        post("/api/teachers", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teacherStatus").value("ACTIVE"));
    }

    @Test
    void teacherCannotCreateATeacher() throws Exception {
        post("/api/teachers", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void duplicatedTeacherBecomesBadRequest() throws Exception {
        when(teacherService.create(any())).thenThrow(new IllegalArgumentException("already exists"));

        post("/api/teachers", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminListsTeachers() throws Exception {
        when(teacherService.getAll()).thenReturn(List.of(teacherDto()));

        get("/api/teachers", admin()).andExpect(status().isOk()).andExpect(jsonPath("$[0].teacherRun").value("11-1"));
        get("/api/teachers", teacher()).andExpect(status().isForbidden());
        get("/api/teachers", student()).andExpect(status().isForbidden());
    }

    @Test
    void teacherReadsHisOwnRecord() throws Exception {
        when(currentUserService.requireTeacherId("teacher@siga.cl")).thenReturn(1L);
        when(teacherService.getById(1L)).thenReturn(teacherDto());

        get("/api/teachers/me", teacher()).andExpect(status().isOk()).andExpect(jsonPath("$.teacherId").value(1));
    }

    @Test
    void userWithoutTeacherRecordIsForbidden() throws Exception {
        when(currentUserService.requireTeacherId(any())).thenThrow(new ForbiddenOperationException("No teacher record"));

        get("/api/teachers/me", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadTheTeacherMeEndpoint() throws Exception {
        get("/api/teachers/me", student()).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsATeacherById() throws Exception {
        when(teacherService.getById(1L)).thenReturn(teacherDto());

        get("/api/teachers/1", admin()).andExpect(status().isOk());
    }

    @Test
    void missingTeacherBecomesNotFound() throws Exception {
        when(teacherService.getById(9L)).thenThrow(new EntityNotFoundException("Teacher not found: 9"));

        get("/api/teachers/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void adminUpdatesATeacher() throws Exception {
        when(teacherService.update(eq(1L), any())).thenReturn(teacherDto());

        put("/api/teachers/1", admin(), UPDATE_BODY).andExpect(status().isOk());
    }

    @Test
    void adminChangesTheStatus() throws Exception {
        when(teacherService.changeStatus(1L, TeacherEntity.TeacherStatus.INACTIVE)).thenReturn(teacherDto());

        put("/api/teachers/1/status?status=INACTIVE", admin(), null).andExpect(status().isOk());
        verify(teacherService).changeStatus(1L, TeacherEntity.TeacherStatus.INACTIVE);
    }

    @Test
    void deactivatingATeacherWithOpenSectionsBecomesConflict() throws Exception {
        when(teacherService.changeStatus(1L, TeacherEntity.TeacherStatus.INACTIVE))
                .thenThrow(new IllegalStateException("has sections in an open academic period"));

        put("/api/teachers/1/status?status=INACTIVE", admin(), null).andExpect(status().isConflict());
    }

    @Test
    void adminDeletesATeacher() throws Exception {
        delete("/api/teachers/1", admin()).andExpect(status().isNoContent());
        verify(teacherService).delete(1L);
    }

    @Test
    void studentCannotDeleteATeacher() throws Exception {
        delete("/api/teachers/1", student()).andExpect(status().isForbidden());
    }
}
