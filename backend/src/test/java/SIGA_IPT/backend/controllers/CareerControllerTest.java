package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.config.SecurityConfig;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.services.CareerService;
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

@WebMvcTest(CareerController.class)
@Import(SecurityConfig.class)
class CareerControllerTest extends ControllerTestBase {

    @MockitoBean
    private CareerService careerService;

    private static final String BODY = "{\"careerCode\":\"10450\",\"careerName\":\"Programmer\",\"careerDescription\":\"Desc\"}";

    private CareerResponseDTO career(){
        return new CareerResponseDTO(1L, "10450", "Programmer", "Desc", CareerEntity.CareerStatus.ACTIVE);
    }

    @Test
    void adminCreatesACareer() throws Exception {
        when(careerService.create(any(CareerRequestDTO.class))).thenReturn(career());

        post("/api/careers", admin(), BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careerCode").value("10450"))
                .andExpect(jsonPath("$.careerStatus").value("ACTIVE"));
    }

    @Test
    void teacherCannotCreateACareer() throws Exception {
        post("/api/careers", teacher(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void requestsWithoutTokenAreUnauthorized() throws Exception {
        post("/api/careers", null, BODY).andExpect(status().isUnauthorized());
        get("/api/careers", null).andExpect(status().isUnauthorized());
    }

    @Test
    void validationErrorsBecomeBadRequest() throws Exception {
        when(careerService.create(any())).thenThrow(new IllegalArgumentException("The career code is required."));

        post("/api/careers", admin(), BODY)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The career code is required."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void anyAuthenticatedUserCanListCareers() throws Exception {
        when(careerService.getAll()).thenReturn(List.of(career()));

        get("/api/careers", student())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].careerName").value("Programmer"));
    }

    @Test
    void getByIdReturnsTheCareer() throws Exception {
        when(careerService.getById(1L)).thenReturn(career());

        get("/api/careers/1", teacher()).andExpect(status().isOk()).andExpect(jsonPath("$.careerId").value(1));
    }

    @Test
    void missingCareerBecomesNotFound() throws Exception {
        when(careerService.getById(9L)).thenThrow(new EntityNotFoundException("Career not found: 9"));

        get("/api/careers/9", admin())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Career not found: 9"));
    }

    @Test
    void adminUpdatesACareer() throws Exception {
        when(careerService.update(eq(1L), any())).thenReturn(career());

        put("/api/careers/1", admin(), BODY).andExpect(status().isOk());
    }

    @Test
    void updatingABlockedCareerBecomesConflict() throws Exception {
        when(careerService.update(eq(1L), any())).thenThrow(new IllegalStateException("Cannot edit"));

        put("/api/careers/1", admin(), BODY).andExpect(status().isConflict());
    }

    @Test
    void studentCannotUpdateACareer() throws Exception {
        put("/api/careers/1", student(), BODY).andExpect(status().isForbidden());
    }

    @Test
    void adminChangesTheStatus() throws Exception {
        when(careerService.changeStatus(1L, CareerEntity.CareerStatus.INACTIVE)).thenReturn(career());

        put("/api/careers/1/status?status=INACTIVE", admin(), null).andExpect(status().isOk());
        verify(careerService).changeStatus(1L, CareerEntity.CareerStatus.INACTIVE);
    }

    @Test
    void anInvalidStatusValueBecomesBadRequest() throws Exception {
        put("/api/careers/1/status?status=WHATEVER", admin(), null).andExpect(status().isBadRequest());
    }

    @Test
    void aMissingStatusParameterBecomesBadRequest() throws Exception {
        put("/api/careers/1/status", admin(), null).andExpect(status().isBadRequest());
    }

    @Test
    void adminDeletesACareer() throws Exception {
        delete("/api/careers/1", admin()).andExpect(status().isNoContent());
        verify(careerService).delete(1L);
    }

    @Test
    void deletingABlockedCareerBecomesConflict() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Cannot delete")).when(careerService).delete(1L);

        delete("/api/careers/1", admin()).andExpect(status().isConflict());
    }

    @Test
    void teacherCannotDeleteACareer() throws Exception {
        delete("/api/careers/1", teacher()).andExpect(status().isForbidden());
    }

    @Test
    void anUnsupportedMethodKeepsItsOwnStatus() throws Exception {
        call(org.springframework.http.HttpMethod.PATCH, "/api/careers/1", admin(), BODY)
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unexpectedErrorsBecomeInternalServerError() throws Exception {
        when(careerService.getAll()).thenThrow(new RuntimeException("boom"));

        get("/api/careers", admin()).andExpect(status().isInternalServerError());
    }
}
