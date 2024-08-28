/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.controller;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.REGISTRATION_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_WITH_ARDQTYPE_DTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.CoreApplication;
import com.ericsson.oss.air.aas.handler.registration.ApiRegistrationHandler;
import com.ericsson.oss.air.aas.handler.registration.CreateArdqRegistrationHandler;
import com.ericsson.oss.air.aas.handler.registration.DeleteArdqRegistrationHandler;
import com.ericsson.oss.air.aas.handler.registration.UpdateArdqRegistrationHandler;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = { CoreApplication.class, ArdqRestController.class })
@ActiveProfiles("test")
public class ArdqRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @SpyBean
    private ArdqRegistrationDao ardqRegistrationDao;

    @SpyBean
    private SchemaDao schemaDao;

    @SpyBean
    private ApiRegistrationHandler registrationHandler;

    @SpyBean
    private CreateArdqRegistrationHandler createArdqRegistrationHandler;

    @SpyBean
    private DeleteArdqRegistrationHandler deleteArdqRegistrationHandler;

    @SpyBean
    private UpdateArdqRegistrationHandler updateArdqRegistrationHandler;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void retrieveOneArdqRegistration_NotFound() throws Exception {
        final MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/v1/augmentation/registration/ardq/cardq"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void retrieveOneArdqRegistration_Ok() throws Exception {
        doReturn(Optional.of(new ArdqRegistrationDto().ardqId("cardq"))).when(ardqRegistrationDao).findByArdqId("cardq");
        mvc.perform(get("/v1/augmentation/registration/ardq/cardq"))
                .andExpect(status().isOk());
    }

    @Test
    void retrieveOneArdqRegistration_OkWithTrailingSlash() throws Exception {
        doReturn(Optional.of(new ArdqRegistrationDto().ardqId("cardq"))).when(ardqRegistrationDao).findByArdqId("cardq");
        mvc.perform(get("/v1/augmentation/registration/ardq/cardq/"))
                .andExpect(status().isOk());
    }

    @Test
    void retrieveOneArdqRegistration_withSchemaMappings() throws Exception {
        doReturn(Optional.of(VALID_REGISTRATION_DTO)).when(ardqRegistrationDao).findByArdqId("cardq");
        doReturn(List.of(new IoSchema("cardq", INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE))).when(this.schemaDao).getIOSchemas("cardq");
        assertEquals(REGISTRATION_RESPONSE_DTO, registrationHandler.getArdqRegistrationById("cardq"));
        mvc.perform(get("/v1/augmentation/registration/ardq/cardq"))
                .andExpect(status().isOk());
    }

    @Test
    void retrieveAllArdqRegistrationIds_noRegistrationsSubmitted_getEmptyList() throws Exception {
        doReturn(getDummyArdqRegistrationsWithArdqIds(List.of())).when(ardqRegistrationDao).getAllArdqRegistrations();

        mvc.perform(get("/v1/augmentation/registration/ardq-ids"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"))
                .andExpect(content().json(List.of().toString()));

        verify(ardqRegistrationDao, times(1)).getAllArdqRegistrations();
    }

    @Test
    void retrieveAllArdqRegistrationIds_noRegistrationsSubmitted_getEmptyListWithTrailingSlash() throws Exception {
        doReturn(getDummyArdqRegistrationsWithArdqIds(List.of())).when(ardqRegistrationDao).getAllArdqRegistrations();

        mvc.perform(get("/v1/augmentation/registration/ardq-ids/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"))
                .andExpect(content().json(List.of().toString()));

        verify(ardqRegistrationDao, times(1)).getAllArdqRegistrations();
    }

    @Test
    void retrieveAllArdqRegistrationIds_oneRegistrationSubmitted_getListWithOneArdqId() throws Exception {
        doReturn(getDummyArdqRegistrationsWithArdqIds(List.of("testing-123"))).when(ardqRegistrationDao).getAllArdqRegistrations();

        mvc.perform(get("/v1/augmentation/registration/ardq-ids"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(List.of("testing-123").toString()));

        verify(ardqRegistrationDao, times(1)).getAllArdqRegistrations();
    }

    @Test
    void retrieveAllArdqRegistrationIds_twoRegistrationsSubmitted_getListWithTwoArdqIds() throws Exception {
        doReturn(getDummyArdqRegistrationsWithArdqIds(List.of("testing-123", "testing-abc")))
                .when(ardqRegistrationDao).getAllArdqRegistrations();

        mvc.perform(get("/v1/augmentation/registration/ardq-ids"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(List.of("testing-123", "testing-abc").toString()));

        verify(ardqRegistrationDao, times(1)).getAllArdqRegistrations();
    }

    @Test
    void deleteArdqRegistration_NoContent() throws Exception {
        doNothing().when(deleteArdqRegistrationHandler).handle(ARDQ_ID);
        mvc.perform(MockMvcRequestBuilders.delete("/v1/augmentation/registration/ardq/cardq"))
                .andExpect(status().isNoContent())
                .andReturn();
        verify(deleteArdqRegistrationHandler, times(1)).handle(ARDQ_ID);
    }

    @Test
    void deleteArdqRegistration_NoContentWithTrailingSlash() throws Exception {
        doNothing().when(deleteArdqRegistrationHandler).handle(ARDQ_ID);
        mvc.perform(MockMvcRequestBuilders.delete("/v1/augmentation/registration/ardq/cardq/"))
                .andExpect(status().isNoContent())
                .andReturn();
        verify(deleteArdqRegistrationHandler, times(1)).handle(ARDQ_ID);
    }

    @Test
    void deleteArdqRegistration_ArdqIdNotFound() throws Exception {
        doThrow(HttpNotFoundRequestProblemException.class).when(this.deleteArdqRegistrationHandler).handle(ARDQ_ID);

        this.mvc.perform(MockMvcRequestBuilders.delete("/v1/augmentation/registration/ardq/cardq"))
                .andExpect(status().isNoContent())
                .andReturn();

        verify(this.deleteArdqRegistrationHandler, times(1)).handle(ARDQ_ID);
    }

    @Test
    void createArdqRegistration_created() throws Exception {
        doNothing().when(createArdqRegistrationHandler).handle(VALID_REGISTRATION_DTO);
        mvc.perform(post("/v1/augmentation/registration/ardq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_DTO)))
                .andExpect(status().isCreated())
                .andReturn();
        verify(createArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_DTO);
    }

    @Test
    void createArdqRegistration_createdWithTrailingSlash() throws Exception {
        doNothing().when(createArdqRegistrationHandler).handle(VALID_REGISTRATION_DTO);
        mvc.perform(post("/v1/augmentation/registration/ardq/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_DTO)))
                .andExpect(status().isCreated())
                .andReturn();
        verify(createArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_DTO);
    }

    @Test
    void createArdqRegistrationWithArdqType_created() throws Exception {
        doNothing().when(createArdqRegistrationHandler).handle(VALID_REGISTRATION_WITH_ARDQTYPE_DTO);
        mvc.perform(post("/v1/augmentation/registration/ardq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_WITH_ARDQTYPE_DTO)))
            .andExpect(status().isCreated())
            .andReturn();
        verify(createArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_WITH_ARDQTYPE_DTO);
    }

    @Test
    void updateArdqRegistration_Ok() throws Exception {
        doNothing().when(updateArdqRegistrationHandler).handle(VALID_REGISTRATION_DTO);
        mvc.perform(put("/v1/augmentation/registration/ardq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_DTO)))
                .andExpect(status().isOk())
                .andReturn();
        verify(updateArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_DTO);
    }

    @Test
    void updateArdqRegistration_OkWithTrailingSlash() throws Exception {
        doNothing().when(updateArdqRegistrationHandler).handle(VALID_REGISTRATION_DTO);
        mvc.perform(put("/v1/augmentation/registration/ardq/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_DTO)))
                .andExpect(status().isOk())
                .andReturn();
        verify(updateArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_DTO);
    }

    @Test
    void updateArdqRegistrationWithArdqType_Ok() throws Exception {
        doNothing().when(updateArdqRegistrationHandler).handle(VALID_REGISTRATION_WITH_ARDQTYPE_DTO);
        mvc.perform(put("/v1/augmentation/registration/ardq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(VALID_REGISTRATION_WITH_ARDQTYPE_DTO)))
            .andExpect(status().isOk())
            .andReturn();
        verify(updateArdqRegistrationHandler, times(1)).handle(VALID_REGISTRATION_WITH_ARDQTYPE_DTO);
    }

    private Optional<List<ArdqRegistrationDto>> getDummyArdqRegistrationsWithArdqIds(List<String> ardqIds) {
        return Optional.of(ardqIds.stream()
                .map(id -> new ArdqRegistrationDto().ardqId(id))
                .collect(Collectors.toList()));
    }
}
