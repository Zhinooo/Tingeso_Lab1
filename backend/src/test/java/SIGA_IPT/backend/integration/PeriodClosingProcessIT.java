package SIGA_IPT.backend.integration;

import SIGA_IPT.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PeriodClosingProcessIT extends IntegrationTestBase {

    private final String[] codes = new String[3];
    private final Long[] courses = new Long[3];
    private final Person[] teachers = new Person[3];

    private World worldWithPlanOfThreeCourses() throws Exception {
        World world = newWorld();
        for (int i = 0; i < 3; i++){
            codes[i] = "K" + i + "-" + next();
            courses[i] = newCourse(world, codes[i], 1);
            teachers[i] = newTeacher();
            newSection(world, courses[i], teachers[i], 30, new String[]{"L", "M", "W"}[i], 1, new String[]{"J", "V", "L"}[i], 2);
        }
        return world;
    }

    private void gradeAll(Person student, double... grades) throws Exception {
        for (int i = 0; i < 3; i++){
            grade(teachers[i], enrollmentIdOf(student, codes[i]), grades[i]).andExpect(status().isOk());
        }
    }

    private Person enrolledStudent(World world) throws Exception {
        Person student = newStudent(world);
        payTuition(student, world);
        enroll(student, world, courses).andExpect(status().isCreated());
        return student;
    }

    @Test
    void successClosingMovesTheEnrollmentsToTheAcademicHistory() throws Exception {
        World world = worldWithPlanOfThreeCourses();
        Person student = enrolledStudent(world);
        gradeAll(student, 5.0, 3.0, 6.0);

        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicPeriodStatus").value("CLOSED"));

        httpGet("/api/enrollments/me/history", student.token()).andExpect(jsonPath("$", hasSize(3)));
        httpGet("/api/students/me", student.token()).andExpect(jsonPath("$.studentStatus").value("REGULAR"));
    }

    @Test
    void successClosingAPeriodWithoutEnrollments() throws Exception {
        World world = newWorld();

        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.academicPeriodStatus").value("CLOSED"));
    }

    @Test
    void successApprovingTheWholePlanGraduatesTheStudent() throws Exception {
        World world = worldWithPlanOfThreeCourses();
        Person student = enrolledStudent(world);
        gradeAll(student, 4.0, 5.5, 7.0);

        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null).andExpect(status().isOk());

        httpGet("/api/students/me", student.token()).andExpect(jsonPath("$.studentStatus").value("GRADUATED"));
    }

    @Test
    void successFailingTheSameCourseTwiceExpelsTheStudent() throws Exception {
        World first = worldWithPlanOfThreeCourses();
        Person student = enrolledStudent(first);
        gradeAll(student, 3.0, 5.0, 5.0);
        httpPut("/api/academic-periods/" + first.periodId() + "/close", asAdmin(), null).andExpect(status().isOk());
        httpGet("/api/students/me", student.token()).andExpect(jsonPath("$.studentStatus").value("REGULAR"));

        Long secondPeriod = idOf(httpPost("/api/academic-periods", asAdmin(),
                "{\"academicYear\":" + (4000 + next()) + ",\"academicSemester\":2}").andExpect(status().isCreated()), "$.academicPeriodId");
        World second = new World(first.careerCode(), first.planId(), secondPeriod);
        newSection(second, courses[0], teachers[0], 30, "L", 1, "W", 1);
        payTuition(student, second);
        enroll(student, second, courses[0]).andExpect(status().isCreated());
        grade(teachers[0], latestEnrollmentId(student, secondPeriod), 2.0).andExpect(status().isOk());

        httpPut("/api/academic-periods/" + secondPeriod + "/close", asAdmin(), null).andExpect(status().isOk());

        httpGet("/api/students/me", student.token()).andExpect(jsonPath("$.studentStatus").value("EXPELLED"));
    }

    private Long latestEnrollmentId(Person student, Long periodId) throws Exception {
        String body = read(httpGet("/api/enrollments/me", student.token()));
        java.util.List<Number> ids = com.jayway.jsonpath.JsonPath.read(body, "$[?(@.academicPeriodId==" + periodId + ")].enrollmentId");
        return ids.get(0).longValue();
    }

    @Test
    void rejectedWhileGradesArePending() throws Exception {
        World world = worldWithPlanOfThreeCourses();
        Person student = enrolledStudent(world);
        grade(teachers[0], enrollmentIdOf(student, codes[0]), 5.0).andExpect(status().isOk());

        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(codes[1])));
        httpGet("/api/academic-periods/" + world.periodId(), asAdmin()).andExpect(jsonPath("$.academicPeriodStatus").value("OPEN"));
    }

    @Test
    void rejectedWhenThePeriodIsAlreadyClosed() throws Exception {
        World world = newWorld();
        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null).andExpect(status().isOk());

        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The academic period is already closed."));
    }

    @Test
    void rejectedWhenANonAdminTriesToClose() throws Exception {
        World world = newWorld();
        Person teacher = newTeacher();
        Person student = newStudent(world);

        httpPut("/api/academic-periods/" + world.periodId() + "/close", teacher.token(), null).andExpect(status().isForbidden());
        httpPut("/api/academic-periods/" + world.periodId() + "/close", student.token(), null).andExpect(status().isForbidden());
    }

    @Test
    void rejectedGradingAndCancellingAfterTheClosing() throws Exception {
        World world = worldWithPlanOfThreeCourses();
        Person student = enrolledStudent(world);
        gradeAll(student, 5.0, 5.0, 3.0);
        Long enrollmentId = enrollmentIdOf(student, codes[2]);
        httpPut("/api/academic-periods/" + world.periodId() + "/close", asAdmin(), null).andExpect(status().isOk());

        grade(teachers[2], enrollmentId, 7.0).andExpect(status().isConflict());
        httpDelete("/api/enrollments/" + enrollmentId, student.token()).andExpect(status().isConflict());
        httpGet("/api/enrollments/" + enrollmentId, asAdmin()).andExpect(jsonPath("$.finalGrade").value(3.0));
    }

    @Test
    void rejectedWhenThePeriodDoesNotExist() throws Exception {
        httpPut("/api/academic-periods/999999/close", asAdmin(), null).andExpect(status().isNotFound());
    }
}
