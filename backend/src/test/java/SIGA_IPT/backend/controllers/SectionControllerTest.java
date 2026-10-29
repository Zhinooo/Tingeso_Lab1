package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionResponseDTO;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.SectionService;
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

@WebMvcTest(SectionController.class)
@Import(SecurityConfig.class)
class SectionControllerTest extends ControllerTestBase {

    @MockitoBean
    private SectionService sectionService;
    @MockitoBean
    private CurrentUserService currentUserService;

    private static final String BODY = "{\"courseId\":1,\"academicPeriodId\":1,\"teacherId\":1,\"sectionCapacity\":30,"
            + "\"scheduleBlocks\":[{\"scheduleDay\":\"L\",\"module\":1},{\"scheduleDay\":\"W\",\"module\":1}]}";
    private static final String UPDATE_BODY = "{\"teacherId\":1,\"sectionCapacity\":40,"
            + "\"scheduleBlocks\":[{\"scheduleDay\":\"L\",\"module\":1},{\"scheduleDay\":\"W\",\"module\":1}]}";

    private SectionResponseDTO section(){
        return new SectionResponseDTO(1L, 1L, "TPI101", "Programming", 1L, 2030, 1, 1L, "Teacher One", 30, List.of("L1", "W1"));
    }

    @Test
    void adminCreatesASection() throws Exception {
        when(sectionService.create(any())).thenReturn(section());

        post("/api/sections", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleBlocks[0]").value("L1"));
    }

    @Test
    void teacherCannotCreateASection() throws Exception {
        post("/api/sections", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void teacherConflictBecomesConflict() throws Exception {
        when(sectionService.create(any())).thenThrow(new IllegalStateException("Schedule conflict"));

        post("/api/sections", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void invalidSectionBecomesBadRequest() throws Exception {
        when(sectionService.create(any())).thenThrow(new IllegalArgumentException("capacity"));

        post("/api/sections", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void anyAuthenticatedUserListsSections() throws Exception {
        when(sectionService.getAll()).thenReturn(List.of(section()));

        get("/api/sections", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].courseCode").value("TPI101"));
        get("/api/sections", null).andExpect(status().isUnauthorized());
    }

    @Test
    void teacherReadsHisOwnSections() throws Exception {
        when(currentUserService.requireTeacherId("kc-teacher")).thenReturn(1L);
        when(sectionService.getByTeacher(1L)).thenReturn(List.of(section()));

        get("/api/sections/me", teacher()).andExpect(status().isOk()).andExpect(jsonPath("$[0].teacherId").value(1));
    }

    @Test
    void userWithoutTeacherRecordIsForbidden() throws Exception {
        when(currentUserService.requireTeacherId(any())).thenThrow(new ForbiddenOperationException("No teacher record"));

        get("/api/sections/me", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadTheSectionMeEndpoint() throws Exception {
        get("/api/sections/me", student()).andExpect(status().isForbidden());
    }

    @Test
    void getByIdReturnsTheSection() throws Exception {
        when(sectionService.getById(1L)).thenReturn(section());

        get("/api/sections/1", student()).andExpect(status().isOk());
    }

    @Test
    void missingSectionBecomesNotFound() throws Exception {
        when(sectionService.getById(9L)).thenThrow(new EntityNotFoundException("Section not found: 9"));

        get("/api/sections/9", admin()).andExpect(status().isNotFound());
    }

    @Test
    void sectionsByPeriodAreVisibleToEveryAuthenticatedUser() throws Exception {
        when(sectionService.getByAcademicPeriod(1L)).thenReturn(List.of(section()));

        get("/api/sections/academic-period/1", student()).andExpect(status().isOk());
    }

    @Test
    void sectionsByTeacherIdAreOnlyForAdmin() throws Exception {
        when(sectionService.getByTeacher(1L)).thenReturn(List.of(section()));

        get("/api/sections/teacher/1", admin()).andExpect(status().isOk());
        get("/api/sections/teacher/1", teacher()).andExpect(status().isForbidden());
        get("/api/sections/teacher/1", student()).andExpect(status().isForbidden());
    }

    @Test
    void adminUpdatesASection() throws Exception {
        when(sectionService.update(eq(1L), any())).thenReturn(section());

        put("/api/sections/1", admin(), UPDATE_BODY).andExpect(status().isOk());
    }

    @Test
    void updatingASectionWithStudentsBecomesConflict() throws Exception {
        when(sectionService.update(eq(1L), any())).thenThrow(new IllegalStateException("enrolled students"));

        put("/api/sections/1", admin(), UPDATE_BODY).andExpect(status().isConflict());
    }

    @Test
    void adminDeletesASection() throws Exception {
        delete("/api/sections/1", admin()).andExpect(status().isNoContent());
        verify(sectionService).delete(1L);
    }

    @Test
    void teacherCannotDeleteASection() throws Exception {
        delete("/api/sections/1", teacher()).andExpect(status().isForbidden());
    }
}
