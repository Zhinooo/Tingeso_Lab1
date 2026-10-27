package SIGA_IPT.backend.integration;

import SIGA_IPT.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradeProcessIT extends IntegrationTestBase {

    private World world;
    private Person teacher;
    private Person student;
    private String courseCode;
    private Long sectionId;

    @BeforeEach
    void setUp() throws Exception {
        world = newWorld();
        teacher = newTeacher();
        student = newStudent(world);
        courseCode = "G" + next();
        Long course = newCourse(world, courseCode, 1);
        sectionId = newSection(world, course, teacher, 30, "L", 1, "W", 1);
        Long second = newCourse(world, "G2" + next(), 1);
        Long third = newCourse(world, "G3" + next(), 1);
        newSection(world, second, newTeacher(), 30, "M", 2, "J", 2);
        newSection(world, third, newTeacher(), 30, "V", 3, "L", 3);
        payTuition(student, world);
        enroll(student, world, course, second, third).andExpect(status().isCreated());
    }

    @Test
    void successTeacherRegistersAnApprovingGrade() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);

        grade(teacher, enrollmentId, 5.5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalGrade").value(5.5))
                .andExpect(jsonPath("$.result").value("APPROVED"));
        httpGet("/api/enrollments/me", student.token())
                .andExpect(jsonPath("$[?(@.courseCode=='" + courseCode + "')].result").value("APPROVED"));
    }

    @Test
    void successTeacherRegistersAFailingGrade() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);

        grade(teacher, enrollmentId, 3.9)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("FAILED"));
    }

    @Test
    void successTeacherCorrectsAPreviousGradeAndSeesHisSection() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);
        grade(teacher, enrollmentId, 3.0).andExpect(jsonPath("$.result").value("FAILED"));

        grade(teacher, enrollmentId, 4.0).andExpect(jsonPath("$.result").value("APPROVED"));

        httpGet("/api/enrollments/me/section/" + sectionId, teacher.token())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].finalGrade").value(4.0));
    }

    @Test
    void rejectedWhenAnotherTeacherGrades() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);
        Person intruder = newTeacher();

        grade(intruder, enrollmentId, 6.0).andExpect(status().isForbidden());
        httpGet("/api/enrollments/me/section/" + sectionId, intruder.token()).andExpect(status().isForbidden());
        httpGet("/api/enrollments/" + enrollmentId, asAdmin()).andExpect(jsonPath("$.finalGrade").doesNotExist());
    }

    @Test
    void rejectedWhenTheGradeIsOutOfRange() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);

        grade(teacher, enrollmentId, 7.5).andExpect(status().isBadRequest());
        grade(teacher, enrollmentId, 0.5).andExpect(status().isBadRequest());
        httpPut("/api/enrollments/" + enrollmentId + "/grade", teacher.token(), "{}").andExpect(status().isBadRequest());
    }

    @Test
    void rejectedWhenAStudentOrAdminTriesToGrade() throws Exception {
        Long enrollmentId = enrollmentIdOf(student, courseCode);

        grade(student, enrollmentId, 7.0).andExpect(status().isForbidden());
        httpPut("/api/enrollments/" + enrollmentId + "/grade", asAdmin(), "{\"finalGrade\":7.0}").andExpect(status().isForbidden());
    }

    @Test
    void rejectedWhenTheEnrollmentDoesNotExist() throws Exception {
        grade(teacher, 999999L, 5.0).andExpect(status().isNotFound());
    }
}
