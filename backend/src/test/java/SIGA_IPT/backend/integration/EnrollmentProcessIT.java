package SIGA_IPT.backend.integration;

import SIGA_IPT.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentProcessIT extends IntegrationTestBase {

    private World worldWithThreeCourses(Long[] courseIds) throws Exception {
        World world = newWorld();
        String[] codes = {"A" + next(), "B" + next(), "C" + next()};
        for (int i = 0; i < codes.length; i++){
            courseIds[i] = newCourse(world, codes[i], 1);
            newSection(world, courseIds[i], newTeacher(), 30, new String[]{"L", "M", "W"}[i], 1, new String[]{"J", "V", "L"}[i], 2);
        }
        return world;
    }

    @Test
    void successStudentEnrollsInThreeCourses() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);
        payTuition(student, world);

        enroll(student, world, courses)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].studentId").value(student.id()));

        httpGet("/api/enrollments/me", student.token()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void successOfferShrinksAfterEnrollingAndCancellingRestoresIt() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);
        payTuition(student, world);
        String offerUrl = "/api/enrollments/me/available?academicPeriodId=" + world.periodId();

        httpGet(offerUrl, student.token()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
        enroll(student, world, courses).andExpect(status().isCreated());
        httpGet(offerUrl, student.token()).andExpect(jsonPath("$", hasSize(0)));

        Long enrollmentId = idOf(httpGet("/api/enrollments/me", student.token()), "$[0].enrollmentId");
        httpDelete("/api/enrollments/" + enrollmentId, student.token()).andExpect(status().isNoContent());
        httpGet(offerUrl, student.token()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void successStudentCompletesTheSelectionInASecondRequest() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);
        payTuition(student, world);

        enroll(student, world, courses[0], courses[1], courses[2]).andExpect(status().isCreated());
        Long fourth = newCourse(world, "D" + next(), 1);
        newSection(world, fourth, newTeacher(), 30, "V", 3, "J", 3);
        enroll(student, world, fourth).andExpect(status().isCreated());

        httpGet("/api/enrollments/me", student.token()).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void rejectedWithoutTuition() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);

        enroll(student, world, courses)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The student has no tuition in this academic period."));
        httpGet("/api/enrollments/me", student.token()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rejectedWhenSchedulesCollide() throws Exception {
        World world = newWorld();
        Long first = newCourse(world, "A" + next(), 1);
        Long second = newCourse(world, "B" + next(), 1);
        Long third = newCourse(world, "C" + next(), 1);
        newSection(world, first, newTeacher(), 30, "L", 1, "W", 1);
        newSection(world, second, newTeacher(), 30, "L", 1, "J", 2);
        newSection(world, third, newTeacher(), 30, "M", 3, "V", 3);
        Person student = newStudent(world);
        payTuition(student, world);

        enroll(student, world, first, second, third)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Schedule conflict")));
        httpGet("/api/enrollments/me", student.token()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rejectedWhenThePrerequisiteIsNotApproved() throws Exception {
        World world = newWorld();
        String baseCode = "BASE" + next();
        Long base = newCourse(world, baseCode, 1);
        Long advanced = newCourse(world, "ADV" + next(), 2, baseCode);
        Long other = newCourse(world, "OTH" + next(), 1);
        Long another = newCourse(world, "ANO" + next(), 1);
        newSection(world, base, newTeacher(), 30, "L", 1, "W", 1);
        newSection(world, advanced, newTeacher(), 30, "M", 2, "J", 2);
        newSection(world, other, newTeacher(), 30, "V", 3, "L", 3);
        newSection(world, another, newTeacher(), 30, "M", 4, "J", 4);
        Person student = newStudent(world);
        payTuition(student, world);

        enroll(student, world, advanced, other, another)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("prerequisite")));
    }

    @Test
    void rejectedWhenTheSectionIsFull() throws Exception {
        World world = newWorld();
        Long tight = newCourse(world, "T" + next(), 1);
        Long second = newCourse(world, "S" + next(), 1);
        Long third = newCourse(world, "R" + next(), 1);
        newSection(world, tight, newTeacher(), 1, "L", 1, "W", 1);
        newSection(world, second, newTeacher(), 30, "M", 2, "J", 2);
        newSection(world, third, newTeacher(), 30, "V", 3, "L", 3);
        Person first = newStudent(world);
        Person late = newStudent(world);
        payTuition(first, world);
        payTuition(late, world);

        enroll(first, world, tight, second, third).andExpect(status().isCreated());
        enroll(late, world, tight, second, third)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no available capacity")));
    }

    @Test
    void rejectedWhenAnAdminTriesToEnroll() throws Exception {
        World world = newWorld();

        httpPost("/api/enrollments", asAdmin(), "{\"academicPeriodId\":" + world.periodId() + ",\"courseIds\":[1,2,3]}")
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectedWhenTheStudentIsNotEnoughCourses() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);
        payTuition(student, world);

        enroll(student, world, courses[0])
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("at least 3")));
    }

    @Test
    void successOnlyOneOfTwoConcurrentStudentsGetsTheLastSeat() throws Exception {
        World world = newWorld();
        Long contested = newCourse(world, "HOT" + next(), 1);
        Long second = newCourse(world, "S" + next(), 1);
        Long third = newCourse(world, "R" + next(), 1);
        newSection(world, contested, newTeacher(), 1, "L", 1, "W", 1);
        newSection(world, second, newTeacher(), 30, "M", 2, "J", 2);
        newSection(world, third, newTeacher(), 30, "V", 3, "L", 3);
        Person one = newStudent(world);
        Person two = newStudent(world);
        payTuition(one, world);
        payTuition(two, world);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Callable<Integer>> attempts = new ArrayList<>();
        for (Person student : List.of(one, two)){
            attempts.add(() -> {
                start.await();
                return enroll(student, world, contested, second, third).andReturn().getResponse().getStatus();
            });
        }
        List<Future<Integer>> futures = new ArrayList<>();
        for (Callable<Integer> attempt : attempts){
            futures.add(pool.submit(attempt));
        }
        start.countDown();
        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> future : futures){
            statuses.add(future.get());
        }
        pool.shutdown();

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    @Test
    void aStudentOnlySeesHisOwnEnrollments() throws Exception {
        Long[] courses = new Long[3];
        World world = worldWithThreeCourses(courses);
        Person student = newStudent(world);
        Person other = newStudent(world);
        payTuition(student, world);
        enroll(student, world, courses).andExpect(status().isCreated());

        httpGet("/api/enrollments/me", other.token()).andExpect(jsonPath("$", hasSize(0)));
        httpGet("/api/enrollments/student/" + student.id(), other.token()).andExpect(status().isForbidden());
        httpGet("/api/enrollments/student/" + student.id(), asAdmin()).andExpect(jsonPath("$", hasSize(3)));
        httpGet("/api/enrollments/me", other.token()).andExpect(jsonPath("$[*].studentId", not(hasItem(student.id().intValue()))));
    }
}
