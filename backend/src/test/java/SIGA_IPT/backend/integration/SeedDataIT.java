package SIGA_IPT.backend.integration;

import SIGA_IPT.backend.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.seed.enabled=true")
class SeedDataIT extends IntegrationTestBase {

    @Test
    void loadsTheThreeCareersOfTheAppendixWithTheirCurrentPlans() throws Exception {
        httpGet("/api/careers", asAdmin())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].careerCode", hasItems("10450", "10451", "10452")));
        httpGet("/api/studyplans", asAdmin())
                .andExpect(jsonPath("$[?(@.careerCode=='10450')].studyplanIdentifier", hasItem("2021.3")))
                .andExpect(jsonPath("$[?(@.careerCode=='10451')].studyplanIdentifier", hasItem("2022.3")))
                .andExpect(jsonPath("$[?(@.careerCode=='10452')].studyplanIdentifier", hasItem("2021.3")))
                .andExpect(jsonPath("$[?(@.careerCode=='10450')].studyplanStatus", hasItem("CURRENT")));
    }

    @Test
    void loads24CoursesPerCareer() throws Exception {
        for (String prefix : new String[]{"TAP", "TAD", "TCS"}){
            httpGet("/api/courses", asAdmin())
                    .andExpect(jsonPath("$[?(@.courseCode =~ /" + prefix + ".*/)]", hasSize(24)));
        }
    }

    @Test
    void resolvesThePrerequisitesByCode() throws Exception {
        httpGet("/api/courses", asAdmin())
                .andExpect(jsonPath("$[?(@.courseCode=='TAP405')].prerequisiteCodes[*]", hasItems("TAP301", "TAP302", "TAP306")))
                .andExpect(jsonPath("$[?(@.courseCode=='TAD406')].prerequisiteCodes[*]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.courseCode=='TCS102')].courseName", hasItem("Fundamentos de Sistemas Computacionales")));
    }
}
