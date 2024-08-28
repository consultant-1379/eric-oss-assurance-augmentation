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

package com.ericsson.oss.air.aas.handler.registration.provider;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.AAS_CHART_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.AUGMENTATION_PROCESSING_TOPIC;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DATA_SERVICE_INSTANCE_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DATA_TYPE_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.MESSAGE_BUS_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SPECIFICATION_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.aas.config.kafka.KafkaProperties;
import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageSchemaRequestDtoProviderTest {

    private MessageSchemaRequestDtoProvider messageSchemaRequestDtoProvider;

    @BeforeEach
    void setUp() {
        final KafkaProperties kafkaProperties;

        final KafkaProperties.AutoConfigTopics autoConfigTopics;

        final KafkaProperties.AugmentationProcessing augmentationProcessing;

        augmentationProcessing = mock(KafkaProperties.AugmentationProcessing.class);
        when(augmentationProcessing.getName()).thenReturn(AUGMENTATION_PROCESSING_TOPIC);

        autoConfigTopics = mock(KafkaProperties.AutoConfigTopics.class);
        when(autoConfigTopics.getAugmentationProcessing()).thenReturn(augmentationProcessing);

        kafkaProperties = mock(KafkaProperties.class);
        when(kafkaProperties.getAutoConfigTopics()).thenReturn(autoConfigTopics);

        this.messageSchemaRequestDtoProvider = new MessageSchemaRequestDtoProvider(
                kafkaProperties);
    }

    @Test
    void buildMessageSchemaRequestDtoTest() {
        final MessageSchemaRequestDto messageSchemaRequestDto =
                this.messageSchemaRequestDtoProvider.build(DATA_TYPE_RESPONSE_DTO, SPECIFICATION_REFERENCE);

        assertEquals(DATA_TYPE_RESPONSE_DTO.getMessageSchema().getMessageDataTopic().getDataProviderType().getDataSpace().getName(),
                messageSchemaRequestDto.getDataSpace().getName());

        assertEquals(AAS_CHART_NAME, messageSchemaRequestDto.getDataService().getDataServiceName());

        assertEquals(
                DATA_TYPE_RESPONSE_DTO.getMessageSchema().getMessageDataTopic().getDataProviderType().getDataCategoryType().getDataCategoryName(),
                messageSchemaRequestDto.getDataCategory().getDataCategoryName());

        assertEquals(DATA_TYPE_RESPONSE_DTO.getMessageSchema().getMessageDataTopic().getDataProviderType().getProviderTypeId(),
                messageSchemaRequestDto.getDataProviderType().getProviderTypeId());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getMessageSchema().getMessageDataTopic().getDataProviderType().getProviderVersion(),
                messageSchemaRequestDto.getDataProviderType().getProviderVersion());

        assertNull(messageSchemaRequestDto.getMessageStatusTopic());

        assertEquals(AUGMENTATION_PROCESSING_TOPIC, messageSchemaRequestDto.getMessageDataTopic().getName());
        assertEquals(MESSAGE_BUS_ID, messageSchemaRequestDto.getMessageDataTopic().getMessageBusId());
        assertEquals(EncodingEnum.AVRO, messageSchemaRequestDto.getMessageDataTopic().getEncoding());

        assertEquals(AAS_CHART_NAME + "-CORE", messageSchemaRequestDto.getDataServiceInstance().getDataServiceInstanceName());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getMessageSchema().getDataService().getDataServiceInstance().get(0).getControlEndpoint(),
                messageSchemaRequestDto.getDataServiceInstance().getControlEndPoint());
        assertEquals(DATA_SERVICE_INSTANCE_RESPONSE_DTO.getConsumedDataSpace(),
                messageSchemaRequestDto.getDataServiceInstance().getConsumedDataSpace());
        assertEquals(DATA_SERVICE_INSTANCE_RESPONSE_DTO.getConsumedDataCategory(),
                messageSchemaRequestDto.getDataServiceInstance().getConsumedDataCategory());
        assertEquals(DATA_SERVICE_INSTANCE_RESPONSE_DTO.getConsumedDataProvider(),
                messageSchemaRequestDto.getDataServiceInstance().getConsumedDataProvider());
        assertEquals(DATA_SERVICE_INSTANCE_RESPONSE_DTO.getConsumedSchemaVersion(),
                messageSchemaRequestDto.getDataServiceInstance().getConsumedSchemaVersion());

        assertEquals(MessageSchemaRequestDtoProvider.DATA_CATALOG_DATA_MEDIUM_TYPE, messageSchemaRequestDto.getDataType().getMediumType());
        assertEquals(SPECIFICATION_REFERENCE.getSchemaName(), messageSchemaRequestDto.getDataType().getSchemaName());
        assertEquals(String.valueOf(SPECIFICATION_REFERENCE.getSchemaVersion()), messageSchemaRequestDto.getDataType().getSchemaVersion());
        assertEquals(true, messageSchemaRequestDto.getDataType().getIsExternal());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getConsumedDataSpace(), messageSchemaRequestDto.getDataType().getConsumedDataSpace());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getConsumedDataCategory(), messageSchemaRequestDto.getDataType().getConsumedDataCategory());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getConsumedDataProvider(), messageSchemaRequestDto.getDataType().getConsumedDataProvider());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getSchemaName(), messageSchemaRequestDto.getDataType().getConsumedSchemaName());
        assertEquals(DATA_TYPE_RESPONSE_DTO.getSchemaVersion(), messageSchemaRequestDto.getDataType().getConsumedSchemaVersion());

        assertNull(messageSchemaRequestDto.getSupportedPredicateParameter());

        assertEquals(SPECIFICATION_REFERENCE.toString(), messageSchemaRequestDto.getMessageSchema().getSpecificationReference());
    }

}
