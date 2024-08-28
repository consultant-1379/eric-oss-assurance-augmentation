/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.api.contract;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INVALID_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ONE_RULE_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.REGISTRATION_DTO_NON_EXISTED_ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.REGISTRATION_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_DTO;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.handler.registration.ApiRegistrationHandler;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for ARDQ Registration API contract tests.
 */
@ActiveProfiles("test")
@SpringBootTest
public class ArdqRegistrationApiBase {
    private AutoCloseable closeable;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @MockBean
    private ApiRegistrationHandler apiRegistrationHandler;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        RestAssuredMockMvc.webAppContextSetup(this.webApplicationContext);

        // GET /ardq/{ardq_id}
        when(apiRegistrationHandler.getArdqRegistrationById(ARDQ_ID)).thenReturn(REGISTRATION_RESPONSE_DTO);
        when(apiRegistrationHandler.getArdqRegistrationById("invalidId")).thenThrow(
                HttpNotFoundRequestProblemException.builder().description("This id 'invalidId' is not found in the current Registration")
                        .build());

        // GET /ardq-ids
        when(apiRegistrationHandler.getAllArdqRegistrationIds()).thenReturn(Optional.of(List.of("cardq", "other-ardq-id")));

        // DELETE /ardq/{ardq_id}
        doNothing().when(apiRegistrationHandler).deleteArdqRegistration("cardq");
        doThrow(HttpNotFoundRequestProblemException.builder()
                .description("This id 'invalidId' is not found in the current Registration")
                .build()).when(apiRegistrationHandler).deleteArdqRegistration("invalidId");

        // POST /ardq
        doNothing().when(apiRegistrationHandler).createArdqRegistration(VALID_REGISTRATION_DTO);

        doThrow(HttpBadRequestProblemException.builder()
                .description("Url [http://eric-oss-cardq] is not available!")
                .build()).when(apiRegistrationHandler).createArdqRegistration(INVALID_REGISTRATION_DTO);

        doThrow(HttpConflictRequestProblemException.builder()
                .description("Provided ARDQ ID: [cardq] already exists!")
                .build()).when(apiRegistrationHandler).createArdqRegistration(ONE_RULE_REGISTRATION_DTO);

        // PUT /ardq
        doNothing().when(apiRegistrationHandler).updateArdqRegistration(VALID_REGISTRATION_DTO);

        doThrow(HttpBadRequestProblemException.builder()
                .description("Url [http://eric-oss-cardq] is not available!")
                .build()).when(apiRegistrationHandler).updateArdqRegistration(INVALID_REGISTRATION_DTO);

        doThrow(HttpNotFoundRequestProblemException.builder()
                .description("ARDQ Registration with ID: [cardq] doesn't exists!")
                .build()).when(apiRegistrationHandler).updateArdqRegistration(REGISTRATION_DTO_NON_EXISTED_ARDQ_ID);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }
}