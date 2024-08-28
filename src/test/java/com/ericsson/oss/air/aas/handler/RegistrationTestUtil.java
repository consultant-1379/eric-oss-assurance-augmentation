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

package com.ericsson.oss.air.aas.handler;

import java.util.List;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.model.SchemaReference;
import com.ericsson.oss.air.aas.model.SpecificationReference;
import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataCategoryResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataProviderTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataServiceInstanceResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataServiceResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataSpaceResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageBusResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageDataTopicResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import com.ericsson.oss.air.aas.model.impl.inmemorydb.OutputSchemaDetails;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationSchemaMappingDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationTestUtil {

    public static final String ARDQ_ID = "cardq";
    public static final String NON_EXISTED_ARDQ_ID = "nonExistedArdqId";
    public static final String ARDQ_URL = "http://eric-oss-cardq:8080";
    public static final String INVALID_ARDQ_URL = "http://eric-oss-cardq";
    public static final String ARDQ_TYPE = "core";

    public static final String INPUT_SCHEMA_NAME = "AMF_Mobility_NetworkSlice_1";
    public static final String INPUT_SCHEMA_REFERENCE = "5G|PM_COUNTERS|" + INPUT_SCHEMA_NAME;
    public static final String SCHEMA_NAMESPACE = "5G.CORE.PM_COUNTERS";
    public static final String SCHEMA_SUBJECT = SCHEMA_NAMESPACE + "." + INPUT_SCHEMA_NAME;
    public static final SchemaSubject SCHEMA_SUBJECT_OBJ = SchemaSubject.parse(SCHEMA_SUBJECT);
    public static final SchemaReference INPUT_SCHEMA_REFERENCE_OBJ = new SchemaReference("5G", "PM_COUNTERS", INPUT_SCHEMA_NAME);

    public static final String DEPRECATED_SCHEMA_REFERENCE = "5G|DEPRECATED|" + INPUT_SCHEMA_NAME;

    public static final String INPUT_FIELD1 = "inputField1";
    public static final String INPUT_FIELD2 = "inputField2";
    public static final String INPUT_FIELD3 = "inputField3";
    public static final String INPUT_FIELD4 = "inputField4";
    public static final String INPUT_FIELD5 = "inputField5";
    public static final String OUTPUT_FIELD1 = "outputField1";
    public static final String OUTPUT_FIELD2 = "outputField2";
    public static final String OUTPUT_FIELD3 = "outputField3";

    public static final String INPUT_SCHEMA1 = "inputSchema1";
    public static final String INPUT_SCHEMA2 = "inputSchema2";
    public static final String INPUT_SCHEMA3 = "inputSchema3";

    public static final ArdqAugmentationFieldDto FIELD_DTO1 = new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD1, INPUT_FIELD2))
            .output(OUTPUT_FIELD1);
    public static final ArdqAugmentationFieldDto FIELD_DTO2 = new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD3, INPUT_FIELD4))
            .output(OUTPUT_FIELD2);
    public static final ArdqAugmentationFieldDto FIELD_DTO3 = new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD5))
            .output(OUTPUT_FIELD3);
    public static final List<ArdqAugmentationFieldDto> FIELDS = List.of(FIELD_DTO1, FIELD_DTO2);

    public static final ArdqAugmentationRuleDto RULE_DTO1 = new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA1);
    public static final ArdqAugmentationRuleDto RULE_DTO2 = new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA2)
            .fields(List.of(FIELD_DTO1));
    public static final ArdqAugmentationRuleDto RULE_DTO3 = new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA3)
            .fields(List.of(FIELD_DTO1, FIELD_DTO3));
    public static final ArdqAugmentationRuleDto RULE_DTO4 = new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
            .fields(List.of(FIELD_DTO1));
    public static final ArdqAugmentationRuleDto RULE_DTO5 = new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
            .fields(List.of(FIELD_DTO1, FIELD_DTO2));

    public static final ArdqAugmentationFieldDto VALID_FIELD_DTO = new ArdqAugmentationFieldDto().input(List.of("snssai", "moFDN")).output("nsi");
    public static final ArdqRegistrationDto VALID_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .ardqType(ARDQ_TYPE)
            .addRulesItem(
                    new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
                            .addFieldsItem(VALID_FIELD_DTO));

    public static final ArdqRegistrationDto VALID_REGISTRATION_WITH_ARDQTYPE_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .ardqType(ARDQ_TYPE)
            .addRulesItem(
                    new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
                            .addFieldsItem(VALID_FIELD_DTO));

    public static final ArdqRegistrationDto INVALID_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(INVALID_ARDQ_URL)
            .addRulesItem(
                    new ArdqAugmentationRuleDto()
                            .inputSchema(INPUT_SCHEMA_REFERENCE)
                            .addFieldsItem(VALID_FIELD_DTO));

    public static final ArdqRegistrationDto REGISTRATION_DTO_NON_EXISTED_ARDQ_ID = new ArdqRegistrationDto()
            .ardqId(NON_EXISTED_ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .addRulesItem(
                    new ArdqAugmentationRuleDto()
                            .inputSchema(INPUT_SCHEMA_REFERENCE)
                            .addFieldsItem(VALID_FIELD_DTO));

    public static final ArdqRegistrationDto NO_FIELD_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .addRulesItem(RULE_DTO1);

    public static final ArdqRegistrationDto ONE_FIELD_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .addRulesItem(RULE_DTO2);
    public static final ArdqRegistrationDto ONE_RULE_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .ardqType(ARDQ_TYPE)
            .addRulesItem(new ArdqAugmentationRuleDto()
                    .inputSchema(INPUT_SCHEMA_REFERENCE)
                    .addFieldsItem(FIELD_DTO1));

    // IDUN-67207 Added second rule to test grouping rules by input schema reference
    public static final ArdqRegistrationDto TWO_RULES_REGISTRATION_DTO = new ArdqRegistrationDto()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .ardqType(ARDQ_TYPE)
            .addRulesItem(new ArdqAugmentationRuleDto()
                    .inputSchema(INPUT_SCHEMA_REFERENCE)
                    .addFieldsItem(FIELD_DTO1))
            .addRulesItem(new ArdqAugmentationRuleDto()
                    .inputSchema(INPUT_SCHEMA_REFERENCE)
                    .addFieldsItem(FIELD_DTO2));

    public static final String OUTPUT_SCHEMA_NAME = ARDQ_ID + "_" + INPUT_SCHEMA_NAME;
    public static final String OUTPUT_SCHEMA_REFERENCE = "5G|PM_COUNTERS|" + OUTPUT_SCHEMA_NAME;
    public static final SchemaReference OUTPUT_SCHEMA_REFERENCE_OBJ = new SchemaReference("5G", "PM_COUNTERS", OUTPUT_SCHEMA_NAME);
    public static final String SUBJECT_NAME = "5G.Core.PM_COUNTERS.smf_session_management_n1_snssai_apn_1";
    public static final String OUTPUT_SCHEMA_STRING = "{\n" +
            "    \"type\": \"record\",\n" +
            "    \"name\": \"cardq_AMF_Mobility_NetworkSlice_1\",\n" +
            "    \"namespace\": \"5G.CORE.PM_COUNTERS\",\n" +
            "    \"fields\": [\n" +
            "      {\n" +
            "        \"name\": \"snssai\",\n" +
            "        \"type\": \"string\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"moFDN\",\n" +
            "        \"type\": \"string\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"nsi\",\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    ]\n" +
            "}";

    public static final Schema OUTPUT_SCHEMA = new Schema.Parser().parse(OUTPUT_SCHEMA_STRING);

    public static final Schema INPUT_SCHEMA = SchemaBuilder.record("schema_from_DMM")
            .namespace(SCHEMA_NAMESPACE)
            .fields()
            .name(INPUT_FIELD1).type().stringType().noDefault()
            .name(INPUT_FIELD2).type().stringType().noDefault()
            .name(INPUT_FIELD3).type().stringType().noDefault()
            .name(INPUT_FIELD4).type().stringType().noDefault()
            .endRecord();

    public static final ArdqRegistrationNotification VALID_CREATE_ARDQ_REGISTRATION_NOTIFICATION = ArdqRegistrationNotification.newBuilder()
            .setArdqId(ARDQ_ID)
            .setArdqNotificationType(ArdqNotificationType.CREATE)
            .build();

    public static final ArdqRegistrationNotification VALID_UPDATE_ARDQ_REGISTRATION_NOTIFICATION = ArdqRegistrationNotification.newBuilder()
            .setArdqId(ARDQ_ID)
            .setArdqNotificationType(ArdqNotificationType.UPDATE)
            .setDeprecatedInputSchemas(List.of(DEPRECATED_SCHEMA_REFERENCE))
            .build();

    public static final ArdqRegistrationNotification VALID_DELETE_ARDQ_REGISTRATION_NOTIFICATION = ArdqRegistrationNotification.newBuilder()
            .setArdqId(ARDQ_ID)
            .setArdqNotificationType(ArdqNotificationType.DELETE)
            .setDeprecatedInputSchemas(List.of(DEPRECATED_SCHEMA_REFERENCE))
            .build();

    //Fields for creating DataTypeResponseDTO

    public static final String CONSUMED_DATA_SPACE = "consumedDataSpace";

    public static final String CONSUMED_DATA_CATEGORY = "consumedDataCategoru";
    public static final String CONSUMED_DATA_PROVIDER = "dataProvider";

    public static final String INPUT_SCHEMA_VERSION = "1";

    public static final int OUPUT_SCHEMA_VERSION = 1;

    public static final String CONSUMED_SCHEMA_NAME = "consumedSchemaName";

    public static final String CONSUMED_SCHEMA_VERSION = "consumedSchemaVersion";

    public static final String CONTROL_ENDPOINT = "http://localhost:8082";

    public static final String AAS_CHART_NAME = "eric-oss-assurance-augmentation";

    public static final String AUGMENTATION_PROCESSING_TOPIC = "eric-oss-assurance-augmentation-processing";

    public static final int MESSAGE_BUS_ID = 1;

    public static final String DATA_TOPIC_NAME = "DATA_TOPIC_NAME";

    public static final DataServiceInstanceResponseDto DATA_SERVICE_INSTANCE_RESPONSE_DTO = DataServiceInstanceResponseDto.builder()
            .controlEndpoint(CONTROL_ENDPOINT)
            .consumedDataSpace(CONSUMED_DATA_SPACE)
            .consumedDataCategory(CONSUMED_DATA_CATEGORY)
            .consumedDataProvider(CONSUMED_DATA_PROVIDER)
            .consumedSchemaName(CONSUMED_SCHEMA_NAME)
            .consumedSchemaVersion(CONSUMED_SCHEMA_VERSION)
            .build();
    public static final DataServiceResponseDto DATA_SERVICE_RESPONSE_DTO = DataServiceResponseDto.builder()
            .dataServiceInstance(List.of(DATA_SERVICE_INSTANCE_RESPONSE_DTO))
            .build();

    public static final MessageBusResponseDto MESSAGE_BUS_RESPONSE_DTO = MessageBusResponseDto.builder()
            .id(MESSAGE_BUS_ID)
            .name("name")
            .clusterName("clusterName")
            .build();

    public static final DataSpaceResponseDto DATA_SPACE_RESPONSE_DTO = DataSpaceResponseDto.builder()
            .name("5G")
            .build();

    public static final DataCategoryResponseDto DATA_CATEGORY_RESPONSE_DTO = DataCategoryResponseDto.builder()
            .dataCategoryName("PM_COUNTERS")
            .build();

    public static final DataProviderTypeResponseDto DATA_PROVIDER_TYPE_RESPONSE_DTO = DataProviderTypeResponseDto.builder()
            .dataSpace(DATA_SPACE_RESPONSE_DTO)
            .dataCategoryType(DATA_CATEGORY_RESPONSE_DTO)
            .providerTypeId("CORE")
            .build();

    public static final MessageDataTopicResponseDto MESSAGE_DATA_TOPIC_RESPONSE_DTO = MessageDataTopicResponseDto.builder()
            .dataProviderType(DATA_PROVIDER_TYPE_RESPONSE_DTO)
            .messageBus(MESSAGE_BUS_RESPONSE_DTO)
            .name(DATA_TOPIC_NAME)
            .encoding(EncodingEnum.AVRO)
            .build();

    public static final MessageSchemaResponseDto MESSAGE_SCHEMA_RESPONSE_DTO = MessageSchemaResponseDto.builder()
            .messageDataTopic(MESSAGE_DATA_TOPIC_RESPONSE_DTO)
            .specificationReference("5G.Core.PM_COUNTERS.smf_session_management_n1_snssai_apn_1/1")
            .dataService(DATA_SERVICE_RESPONSE_DTO)
            .build();

    public static final DataTypeResponseDto DATA_TYPE_RESPONSE_DTO = DataTypeResponseDto.builder()
            .consumedDataSpace(CONSUMED_DATA_SPACE)
            .consumedDataCategory(CONSUMED_DATA_CATEGORY)
            .consumedDataProvider(CONSUMED_DATA_PROVIDER)
            .schemaName(INPUT_SCHEMA_NAME)
            .schemaVersion(INPUT_SCHEMA_VERSION)
            .consumedSchemaName(CONSUMED_SCHEMA_NAME)
            .consumedSchemaVersion(CONSUMED_SCHEMA_VERSION)
            .messageSchema(MESSAGE_SCHEMA_RESPONSE_DTO)
            .isExternal(true)
            .build();

    public static final SpecificationReference SPECIFICATION_REFERENCE = SpecificationReference.builder()
            .dataSpace(CONSUMED_DATA_SPACE)
            .dataProvider(CONSUMED_DATA_PROVIDER)
            .dataCategory(CONSUMED_DATA_CATEGORY)
            .schemaName(OUTPUT_SCHEMA_NAME)
            .schemaVersion(OUPUT_SCHEMA_VERSION)
            .build();

    public static final ArdqRegistrationResponseDto REGISTRATION_RESPONSE_DTO = new ArdqRegistrationResponseDto().ardqId(ARDQ_ID).ardqUrl(ARDQ_URL)
            .ardqType(ARDQ_TYPE)
            .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
                    .addFieldsItem(VALID_FIELD_DTO))
            .addSchemaMappingsItem(new ArdqRegistrationSchemaMappingDto().inputSchema(INPUT_SCHEMA_REFERENCE).outputSchema(OUTPUT_SCHEMA_REFERENCE));

    public static final OutputSchemaDetails OUTPUT_SCHEMA_DETAILS = new OutputSchemaDetails(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA,
            OUTPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_NAME, List.of(VALID_FIELD_DTO));
}
