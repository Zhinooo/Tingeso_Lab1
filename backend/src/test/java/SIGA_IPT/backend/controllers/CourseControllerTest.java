package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.CourseDTOs.CourseResponseDTO;
import SIGA_IPT.backend.services.CourseService;
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

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTest extends ControllerTestBase {

    @MockitoBean
    private CourseService courseService;

    private static final String BODY = "{\"courseCode\":\"TPI101\",\"courseName\":\"Programming\",\"semester\":1,"
            + "\"theoryHours\":2,\"exerciseHours\":2,\"labHours\":2,\"sct\":6,\"studyplanId\":1,\"prerequisiteCodes\":[]}";

    private CourseResponseDTO course(){
        return new CourseResponseDTO(1L, "TPI101", "Programming", 1, 2, 2, 2, 6, 1L, "2021.3", List.of());
    }

    @Test
    void adminCreatesACourse() throws Exception {
        when(courseService.create(any())).thenReturn(course());

        post("/api/courses", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("TPI101"))
                .andExpect(jsonPath("$.studyplanIdentifier").value("2021.3"));
    }

    @Test
    void teacherCannotCreateACourse() throws Exception {
        post("/api/courses", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void invalidCoursesBecomeBadRequest() throws Exception {
        when(courseService.create(any())).thenThrow(new IllegalArgumentException("The SCT is required."));

        post("/api/courses", admin(), BODY).andExpect(status().isBadRequest());
    }

    @Test
    void unknownPlanBecomesNotFound() throws Exception {
        when(courseService.create(any())).thenThrow(new EntityNotFoundException("Study plan not found: 9"));

        post("/api/courses", admin(), BODY).andExpect(status().isNotFound());
    }

    @Test
    void anyAuthenticatedUserListsCourses() throws Exception {
        when(courseService.getAll()).thenReturn(List.of(course()));

        get("/api/courses", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].sct").value(6));
    }

    @Test
    void getByIdReturnsTheCourse() throws Exception {
        when(courseService.getById(1L)).thenReturn(course());

        get("/api/courses/1", teacher()).andExpect(status().isOk());
    }

    @Test
    void getByStudyplanReturnsItsCourses() throws Exception {
        when(courseService.getByStudyplan(1L)).thenReturn(List.of(course()));

        get("/api/courses/studyplan/1", student()).andExpect(status().isOk()).andExpect(jsonPath("$[0].courseId").value(1));
    }

    @Test
    void unauthenticatedUsersCannotListCourses() throws Exception {
        get("/api/courses", null).andExpect(status().isUnauthorized());
    }

    @Test
    void adminUpdatesACourse() throws Exception {
        when(courseService.update(eq(1L), any())).thenReturn(course());

        put("/api/courses/1", admin(), BODY).andExpect(status().isOk());
    }

    @Test
    void updatingACourseWithHistoryBecomesConflict() throws Exception {
        when(courseService.update(eq(1L), any())).thenThrow(new IllegalStateException("part of the academic history"));

        put("/api/courses/1", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void studentCannotUpdateACourse() throws Exception {
        put("/api/courses/1", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void adminDeletesACourse() throws Exception {
        delete("/api/courses/1", admin()).andExpect(status().isNoContent());
        verify(courseService).delete(1L);
    }

    @Test
    void deletingAPrerequisiteBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("prerequisite")).when(courseService).delete(1L);

        delete("/api/courses/1", admin()).andExpect(status().isConflict());
    }
}
