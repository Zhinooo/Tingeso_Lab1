package SIGA_IPT.backend.support;

import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "keycloak.admin.enabled=false")
public abstract class IntegrationTestBase extends MySqlContainerBase {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final AtomicInteger YEARS = new AtomicInteger(3000);

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;

    public record Person(Long id, String email, RequestPostProcessor token) {
    }

    public record World(String careerCode, Long planId, Long periodId) {
    }

    protected static int next(){
        return SEQUENCE.incrementAndGet();
    }

    protected static RequestPostProcessor asAdmin(){
        return asUser("ADMIN", "it-admin", "admin@siga.cl");
    }

    protected static RequestPostProcessor asUser(String role, String subject, String email){
        return jwt().jwt(token -> token.subject(subject).claim("email", email)).authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    protected String read(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    protected <T> T field(ResultActions result, String path) throws Exception {
        return JsonPath.read(read(result), path);
    }

    protected Long idOf(ResultActions result, String path) throws Exception {
        Number value = field(result, path);
        return value.longValue();
    }

    protected ResultActions send(MockHttpServletRequestBuilder builder, RequestPostProcessor auth, String json) throws Exception {
        builder.with(auth);
        if (json != null){
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(builder);
    }

    protected ResultActions httpGet(String url, RequestPostProcessor auth) throws Exception {
        return send(get(url), auth, null);
    }

    protected ResultActions httpPost(String url, RequestPostProcessor auth, String json) throws Exception {
        return send(post(url), auth, json);
    }

    protected ResultActions httpPut(String url, RequestPostProcessor auth, String json) throws Exception {
        return send(put(url), auth, json);
    }

    protected ResultActions httpDelete(String url, RequestPostProcessor auth) throws Exception {
        return send(delete(url), auth, null);
    }

    protected World newWorld() throws Exception {
        int n = next();
        String careerCode = "IT-" + n;
        httpPost("/api/careers", asAdmin(),
                "{\"careerCode\":\"" + careerCode + "\",\"careerName\":\"Career " + n + "\",\"careerDescription\":\"Integration\"}")
                .andExpect(status().isCreated());
        Long planId = idOf(httpPost("/api/studyplans", asAdmin(),
                "{\"studyplanIdentifier\":\"2030." + n + "\",\"careerCode\":\"" + careerCode + "\",\"current\":true}")
                .andExpect(status().isCreated()), "$.studyplanId");
        Long periodId = idOf(httpPost("/api/academic-periods", asAdmin(),
                "{\"academicYear\":" + YEARS.incrementAndGet() + ",\"academicSemester\":1}")
                .andExpect(status().isCreated()), "$.academicPeriodId");
        return new World(careerCode, planId, periodId);
    }

    protected Long newCourse(World world, String code, int semester, String... prerequisites) throws Exception {
        String prerequisiteJson = prerequisites.length == 0 ? "[]"
                : "[\"" + String.join("\",\"", List.of(prerequisites)) + "\"]";
        return idOf(httpPost("/api/courses", asAdmin(),
                "{\"courseCode\":\"" + code + "\",\"courseName\":\"Course " + code + "\",\"semester\":" + semester
                        + ",\"theoryHours\":2,\"exerciseHours\":2,\"labHours\":0,\"sct\":5,\"studyplanId\":" + world.planId()
                        + ",\"prerequisiteCodes\":" + prerequisiteJson + "}")
                .andExpect(status().isCreated()), "$.courseId");
    }

    protected Person newTeacher() throws Exception {
        int n = next();
        String email = "it.teacher" + n + "@siga.cl";
        Long id = idOf(httpPost("/api/teachers", asAdmin(),
                "{\"teacherRun\":\"" + String.format("2%07d-1", n) + "\",\"teacherFullname\":\"Teacher " + n + "\",\"teacherEmail\":\"" + email
                        + "\",\"teacherProtitle\":\"Engineer\",\"teacherDegree\":\"Master\",\"password\":\"Initial123\"}")
                .andExpect(status().isCreated()), "$.teacherId");
        String subject = "it-teacher-" + n;
        TeacherEntity teacher = teacherRepository.findById(id).orElseThrow();
        teacher.setKeycloakUserId(subject);
        teacherRepository.save(teacher);
        return new Person(id, email, asUser("TEACHER", subject, email));
    }

    protected Person newStudent(World world) throws Exception {
        int n = next();
        String email = "it.student" + n + "@siga.cl";
        Long id = idOf(httpPost("/api/students", asAdmin(),
                "{\"studentRun\":\"" + String.format("3%07d-k", n) + "\",\"studentFullname\":\"Student " + n + "\",\"studentEmail\":\"" + email
                        + "\",\"careerCode\":\"" + world.careerCode() + "\",\"password\":\"Initial123\"}")
                .andExpect(status().isCreated()), "$.studentId");
        String subject = "it-student-" + n;
        StudentEntity student = studentRepository.findById(id).orElseThrow();
        student.setKeycloakUserId(subject);
        studentRepository.save(student);
        return new Person(id, email, asUser("STUDENT", subject, email));
    }

    protected void payTuition(Person student, World world) throws Exception {
        httpPost("/api/tuitions", asAdmin(), "{\"studentId\":" + student.id() + ",\"academicPeriodId\":" + world.periodId() + "}")
                .andExpect(status().isCreated());
    }

    protected Long newSection(World world, Long courseId, Person teacher, int capacity, String day1, int module1, String day2, int module2) throws Exception {
        return idOf(httpPost("/api/sections", asAdmin(),
                "{\"courseId\":" + courseId + ",\"academicPeriodId\":" + world.periodId() + ",\"teacherId\":" + teacher.id()
                        + ",\"sectionCapacity\":" + capacity + ",\"scheduleBlocks\":[{\"scheduleDay\":\"" + day1 + "\",\"module\":" + module1
                        + "},{\"scheduleDay\":\"" + day2 + "\",\"module\":" + module2 + "}]}")
                .andExpect(status().isCreated()), "$.sectionId");
    }

    protected Long newSection(World world, Long courseId, Person teacher) throws Exception {
        return newSection(world, courseId, teacher, 30, "L", 1, "W", 1);
    }

    protected ResultActions enroll(Person student, World world, Long... courseIds) throws Exception {
        String ids = String.join(",", java.util.Arrays.stream(courseIds).map(String::valueOf).toList());
        return httpPost("/api/enrollments", student.token(),
                "{\"academicPeriodId\":" + world.periodId() + ",\"courseIds\":[" + ids + "]}");
    }

    protected Long enrollmentIdOf(Person student, String courseCode) throws Exception {
        String body = read(httpGet("/api/enrollments/me", student.token()).andExpect(status().isOk()));
        List<Number> ids = JsonPath.read(body, "$[?(@.courseCode=='" + courseCode + "')].enrollmentId");
        return ids.get(0).longValue();
    }

    protected ResultActions grade(Person teacher, Long enrollmentId, double finalGrade) throws Exception {
        return httpPut("/api/enrollments/" + enrollmentId + "/grade", teacher.token(), "{\"finalGrade\":" + finalGrade + "}");
    }
}
