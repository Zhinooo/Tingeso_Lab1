package SIGA_IPT.backend.integration;

import SIGA_IPT.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudyplanProcessIT extends IntegrationTestBase {

    private Long createPlan(String careerCode, String identifier, boolean current) throws Exception {
        return idOf(httpPost("/api/studyplans", asAdmin(),
                "{\"studyplanIdentifier\":\"" + identifier + "\",\"careerCode\":\"" + careerCode + "\",\"current\":" + current + "}")
                .andExpect(status().isCreated()), "$.studyplanId");
    }

    @Test
    void successANewCurrentPlanDemotesThePreviousOne() throws Exception {
        World world = newWorld();

        Long newer = createPlan(world.careerCode(), "2031." + next(), true);

        httpGet("/api/studyplans/" + world.planId(), asAdmin()).andExpect(jsonPath("$.studyplanStatus").value("NOT_CURRENT"));
        httpGet("/api/studyplans/" + newer, asAdmin()).andExpect(jsonPath("$.studyplanStatus").value("CURRENT"));
    }

    @Test
    void successANotCurrentPlanKeepsThePreviousOne() throws Exception {
        World world = newWorld();

        Long draft = createPlan(world.careerCode(), "2032." + next(), false);

        httpGet("/api/studyplans/" + world.planId(), asAdmin()).andExpect(jsonPath("$.studyplanStatus").value("CURRENT"));
        httpGet("/api/studyplans/" + draft, asAdmin()).andExpect(jsonPath("$.studyplanStatus").value("NOT_CURRENT"));
    }

    @Test
    void successNewStudentsGetTheCurrentPlanWhileOldOnesKeepTheirs() throws Exception {
        World world = newWorld();
        Person early = newStudent(world);
        Long newer = createPlan(world.careerCode(), "2033." + next(), true);

        Person late = newStudent(world);

        httpGet("/api/students/me", early.token()).andExpect(jsonPath("$.studyplanIdentifier").value("2030." + (world.careerCode().substring(3))));
        httpGet("/api/students/" + late.id(), asAdmin())
                .andExpect(jsonPath("$.studyplanIdentifier").value(org.hamcrest.Matchers.startsWith("2033.")));
        httpGet("/api/studyplans/" + newer, asAdmin()).andExpect(jsonPath("$.studyplanStatus").value("CURRENT"));
    }

    @Test
    void rejectedWhenThePlanIdentifierIsRepeatedInTheCareer() throws Exception {
        World world = newWorld();
        String identifier = "2034." + next();
        createPlan(world.careerCode(), identifier, false);

        httpPost("/api/studyplans", asAdmin(),
                "{\"studyplanIdentifier\":\"" + identifier + "\",\"careerCode\":\"" + world.careerCode() + "\",\"current\":false}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectedWhenTheCurrentFlagIsMissing() throws Exception {
        World world = newWorld();

        httpPost("/api/studyplans", asAdmin(),
                "{\"studyplanIdentifier\":\"2035." + next() + "\",\"careerCode\":\"" + world.careerCode() + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectedWhenAPlanWithStudentsIsDeleted() throws Exception {
        World world = newWorld();
        newStudent(world);

        httpDelete("/api/studyplans/" + world.planId(), asAdmin()).andExpect(status().isConflict());
    }

    @Test
    void rejectedWhenACareerWithPlansIsEditedOrDeleted() throws Exception {
        World world = newWorld();
        java.util.List<Number> careerIds = field(httpGet("/api/careers", asAdmin()).andExpect(status().isOk()),
                "$[?(@.careerCode=='" + world.careerCode() + "')].careerId");
        Long careerId = careerIds.get(0).longValue();

        httpDelete("/api/careers/" + careerId, asAdmin()).andExpect(status().isConflict());
        httpPut("/api/careers/" + careerId, asAdmin(),
                "{\"careerCode\":\"" + world.careerCode() + "\",\"careerName\":\"Renamed\",\"careerDescription\":\"x\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void rejectedWhenAStudentManagesPlans() throws Exception {
        World world = newWorld();
        Person student = newStudent(world);

        httpPost("/api/studyplans", student.token(),
                "{\"studyplanIdentifier\":\"X\",\"careerCode\":\"" + world.careerCode() + "\",\"current\":true}")
                .andExpect(status().isForbidden());
        httpGet("/api/studyplans", student.token()).andExpect(status().isOk());
    }
}
