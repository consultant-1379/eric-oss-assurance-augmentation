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

package com.ericsson.oss.air.aas.repository.impl.inmemorydb;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELD_DTO1;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA2;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.RULE_DTO2;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_FIELD_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArdqRegistrationDaoImplTest {

    @Mock
    private SchemaDaoImpl schemaDao;

    private static final ArdqAugmentationRuleDto RULE_1 = new ArdqAugmentationRuleDto()
            .inputSchema("schemaRef1");
    private static final ArdqAugmentationRuleDto RULE_2 = new ArdqAugmentationRuleDto()
            .inputSchema("schemaRef2");
    private static final ArdqRegistrationDto REGISTRATION_1 = new ArdqRegistrationDto()
            .ardqId("ardqid1")
            .addRulesItem(RULE_1);
    private static final ArdqRegistrationDto REGISTRATION_2 = new ArdqRegistrationDto()
            .ardqId("ardqid2")
            .addRulesItem(RULE_2);
    private static final ArdqRegistrationDto REGISTRATION_3 = new ArdqRegistrationDto()
            .ardqId("ardqid3")
            .addRulesItem(RULE_DTO2)
            .addRulesItem(RULE_DTO2);
    private ArdqRegistrationDaoImpl ardqRegistrationDao;

    private static ArdqAugmentationFieldDto getDummyArdqAugmentationField() {
        return new ArdqAugmentationFieldDto().output("nsi").addInputItem("snssai").addInputItem("moFDN");
    }

    private static ArdqAugmentationRuleDto getDummyArdqAugmentationRule() {
        return new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE).addFieldsItem(getDummyArdqAugmentationField());
    }

    @BeforeEach
    void setUp() {
        this.ardqRegistrationDao = new ArdqRegistrationDaoImpl();
    }

    @Test
    void findByArdqId_nonExistedId_emptyReturned() {
        assertEquals(Optional.empty(), this.ardqRegistrationDao.findByArdqId("NON_EXISTED_ARDQ_ID"));
    }

    @Test
    void saveArdqRegistration_PutRegistration() {
        ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().ardqId("ArdqId");
        this.ardqRegistrationDao.saveArdqRegistration(ardqRegistrationDto);

        final Optional<ArdqRegistrationDto> registrationDto = this.ardqRegistrationDao.findByArdqId("ArdqId");
        assertThat(registrationDto).isPresent();
        assertThat(registrationDto.get()).isEqualTo(ardqRegistrationDto);
    }

    @Test
    void getAllArdqRegistrations_emptyList() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertTrue(testDao.getAllArdqRegistrations().get().isEmpty());
    }

    @Test
    void getAllArdqRegistrations_notEmptyList() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("ArdqId"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertFalse(testDao.getAllArdqRegistrations().get().isEmpty());
    }

    @Test
    void getAllArdqRegistrations_savetwoWithDiffArdqIds_returnTwoArdqRegistrations() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("ArdqId"));
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("another-ardqId"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertEquals(2, testDao.getAllArdqRegistrations().get().size());
        assertEquals(List.of("ArdqId", "another-ardqId"),
                testDao.getAllArdqRegistrations().get().stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    @Test
    void getAllArdqRegistrations_saveTwoWithSameArdqId_returnOneArdqRegistration() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("ArdqId"));
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("ArdqId"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertEquals(1, testDao.getAllArdqRegistrations().get().size());
        assertEquals(List.of("ArdqId"),
                testDao.getAllArdqRegistrations().get().stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    @Test
    void retrieveRegistrationsByInputSchemaRef_shouldReturnRegistrationList() {
        this.ardqRegistrationDao.saveArdqRegistration(REGISTRATION_1);
        this.ardqRegistrationDao.saveArdqRegistration(REGISTRATION_2);

        final Optional<List<ArdqRegistrationDto>> registrationDtoList = this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef("schemaRef1");

        assertTrue(registrationDtoList.isPresent());
        assertEquals(1, registrationDtoList.get().size());
        assertEquals(REGISTRATION_1, registrationDtoList.get().get(0));
    }

    @Test
    void retrieveRegistrationsByInputSchemaRef_shouldReturnEmptyRegistrationList() {
        this.ardqRegistrationDao.saveArdqRegistration(REGISTRATION_1);
        this.ardqRegistrationDao.saveArdqRegistration(REGISTRATION_2);

        final Optional<List<ArdqRegistrationDto>> registrationDtoList = this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef("schemaRef3");

        assertFalse(registrationDtoList.isPresent());
    }

    @Test
    void saveArdqRegistration_getAugmentationFields() {
        this.ardqRegistrationDao.saveArdqRegistration(VALID_REGISTRATION_DTO);

        assertEquals(Optional.of(List.of(VALID_FIELD_DTO)), this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void saveArdqRegistration_getAugmentationFields_nonExistentArdqRegistrationId_fieldsNotFound() {
        this.ardqRegistrationDao.saveArdqRegistration(VALID_REGISTRATION_DTO);

        assertEquals(Optional.empty(), this.ardqRegistrationDao.getAugmentationFields("non-existent-name", INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void saveArdqRegistration_nonExistentOutputSchemaReference_fieldsNotFound() {
        this.ardqRegistrationDao.saveArdqRegistration(VALID_REGISTRATION_DTO);

        assertEquals(Optional.empty(), this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, "non-existent-name"));
    }

    @Test
    void saveArdqRegistration_nonExistentArdqRegistrationIdOutputSchemaReference_fieldsNotFound() {
        this.ardqRegistrationDao.saveArdqRegistration(VALID_REGISTRATION_DTO);

        assertEquals(Optional.empty(), this.ardqRegistrationDao.getAugmentationFields("something", "non-existent-name"));
    }

    @Test
    void deleteArdqRegistrations_givenOneRegistrationInDao_whenValidIdProvided_shouldReturnOneRow() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("first-id"));
        assertEquals(1, testDao.deleteRegistrationByArdqId("first-id"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertEquals(0, testDao.getAllArdqRegistrations().get().size());
    }

    @Test
    void deleteArdqRegistrations_givenTwoRegistrationsInDao_whenValidIdProvided_shouldReturnOneRow() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("first-id"));
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("second-id"));
        assertEquals(1, testDao.deleteRegistrationByArdqId("first-id"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertEquals(1, testDao.getAllArdqRegistrations().get().size());
        assertEquals(List.of("second-id"),
                testDao.getAllArdqRegistrations().get().stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    @Test
    void deleteArdqRegistrations_givenTwoRegistrationsInDao_whenInvalidIdProvided_shouldReturnOneRow() {
        final ArdqRegistrationDaoImpl testDao = new ArdqRegistrationDaoImpl();
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("first-id"));
        testDao.saveArdqRegistration(new ArdqRegistrationDto().ardqId("second-id"));
        assertEquals(0, testDao.deleteRegistrationByArdqId("invalid-id"));
        assertTrue(testDao.getAllArdqRegistrations().isPresent());
        assertEquals(2, testDao.getAllArdqRegistrations().get().size());
        assertEquals(List.of("first-id", "second-id"),
                testDao.getAllArdqRegistrations().get().stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    @Test
    void getAugmentationFields_givenOneRegistration_withTwoRulesHavingSameInputSchemaReference_shouldReturnTwoRows() {
        this.ardqRegistrationDao.saveArdqRegistration(REGISTRATION_3);

        final Optional<List<ArdqAugmentationFieldDto>> fieldDtoList = this.ardqRegistrationDao.getAugmentationFields("ardqid3", INPUT_SCHEMA2);

        assertTrue(fieldDtoList.isPresent());
        assertEquals(2, fieldDtoList.get().size());
        assertEquals(FIELD_DTO1, fieldDtoList.get().get(0));
        assertEquals(FIELD_DTO1, fieldDtoList.get().get(1));
    }

}