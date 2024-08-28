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

package com.ericsson.oss.air.aas.handler.registration.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import org.junit.jupiter.api.Test;

class AugmentationRulesValidatorTest {

    private static final String FIELD1 = "field1";
    private static final String FIELD2 = "field2";
    private static final String FIELD3 = "field3";
    private static final String FIELD4 = "field4";
    private static final String FIELD5 = "field5";

    private static final String INPUT_SCHEMA_REF1 = "inputSchema1";
    private static final String INPUT_SCHEMA_REF2 = "inputSchema2";

    private static final ArdqAugmentationFieldDto FIELD_DTO_INPUT12_OUTPUT4 = new ArdqAugmentationFieldDto()
            .addInputItem(FIELD1)
            .addInputItem(FIELD2)
            .output(FIELD4);
    private static final ArdqAugmentationFieldDto FIELD_DTO_INPUT3_OUTPUT5 = new ArdqAugmentationFieldDto()
            .addInputItem(FIELD3)
            .output(FIELD5);

    private static final ArdqAugmentationFieldDto FIELD_DTO_INPUT3_OUTPUT4 = new ArdqAugmentationFieldDto()
            .addInputItem(FIELD3)
            .output(FIELD4);

    private static final ArdqAugmentationFieldDto FIELD_DTO_INPUT1_OUTPUT1 = new ArdqAugmentationFieldDto()
            .addInputItem(FIELD1)
            .output(FIELD1);

    private static final ArdqAugmentationFieldDto FIELD_DTO_INPUT14_OUTPUT5 = new ArdqAugmentationFieldDto()
            .addInputItem(FIELD1)
            .addInputItem(FIELD4)
            .output(FIELD5);

    @Test
    public void validate_SingleRule_Success() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().addRulesItem(ardqAugmentationRuleDto);
        AugmentationRulesValidator.validate(ardqRegistrationDto);

    }

    @Test
    public void validate_SingleRule_DuplicateOutputField_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().addRulesItem(ardqAugmentationRuleDto);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));

    }

    @Test
    public void validate_SingleRule_InputEqualsOutputField_SameFieldSpec_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT1_OUTPUT1)
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().addRulesItem(ardqAugmentationRuleDto);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_SingleRule_InputEqualsOutputField_DifferentFieldSpec_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT14_OUTPUT5)
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().addRulesItem(ardqAugmentationRuleDto);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_MultipleRule_SingleInputSchema_Success() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT5)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB);

        AugmentationRulesValidator.validate(ardqRegistrationDto);

    }

    @Test
    public void validate_MultipleRule_SingleInputSchema_DuplicateOutputField_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_MultipleRule_SingleInputSchema_InputEqualsOutputField_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT14_OUTPUT5)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));

    }

    @Test
    public void validate_MultipleRule_MultipleInputSchemas_DuplicateOutputFieldAcrossSchemas_Success() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF2);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB);

        AugmentationRulesValidator.validate(ardqRegistrationDto);

    }

    @Test
    public void validate_MultipleRule_MultipleInputSchemas_InputEqualsOutputFieldAcrossSchemas_Success() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT14_OUTPUT5)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF2);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB);

        AugmentationRulesValidator.validate(ardqRegistrationDto);

    }

    @Test
    public void validate_MultipleRule_MultipleInputSchemas_DuplicateOutputField_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT3_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoC = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF2);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB)
                .addRulesItem(ardqAugmentationRuleDtoC);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_MultipleRule_MultipleInputSchemas_InputEqualsOutputField_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT14_OUTPUT5)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoC = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF2);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB)
                .addRulesItem(ardqAugmentationRuleDtoC);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_MultipleRule_MultipleInputSchemas_MultipleViolations_ThrowsException() {

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT14_OUTPUT5)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoB = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqAugmentationRuleDto ardqAugmentationRuleDtoC = new ArdqAugmentationRuleDto()
                .addFieldsItem(FIELD_DTO_INPUT12_OUTPUT4)
                .inputSchema(INPUT_SCHEMA_REF1);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto()
                .addRulesItem(ardqAugmentationRuleDto)
                .addRulesItem(ardqAugmentationRuleDtoB)
                .addRulesItem(ardqAugmentationRuleDtoC);

        assertThrows(HttpBadRequestProblemException.class, () -> AugmentationRulesValidator.validate(ardqRegistrationDto));
    }

    @Test
    public void validate_NullInput_Success() {
        AugmentationRulesValidator.validate(null);
    }

}