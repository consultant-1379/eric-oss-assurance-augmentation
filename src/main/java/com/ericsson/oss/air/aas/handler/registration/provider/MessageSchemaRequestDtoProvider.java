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

import com.ericsson.oss.air.aas.config.kafka.KafkaProperties;
import com.ericsson.oss.air.aas.model.SpecificationReference;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataCategoryRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataProviderTypeRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataServiceInstanceRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataServiceRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataSpaceRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.DataTypeRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.InnerMessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageDataTopicRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataProviderTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import org.springframework.stereotype.Component;

/**
 * Provider to build {@link MessageSchemaRequestDto} to register with Data Catalog
 */
@Component
public class MessageSchemaRequestDtoProvider {

    public static final String DATA_CATALOG_DATA_MEDIUM_TYPE = "stream";
    public static final String DATA_CATALOG_SUPPORTED_PREDICTED_PARAMETER_NAME = "pd101";
    public static final String DATA_CATALOG_INSTANCE_END_POINT = "http://localhost:8082";

    private final KafkaProperties kafkaProperties;

    public MessageSchemaRequestDtoProvider(final KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * This method uses the data from {@link DataTypeResponseDto} and {@link SpecificationReference} to build {@link MessageSchemaRequestDto}
     *
     * @param dataTypeResponseDto    {@link DataTypeResponseDto}
     * @param specificationReference {@link SpecificationReference}
     * @return {@link MessageSchemaRequestDto}
     */
    public MessageSchemaRequestDto build(final DataTypeResponseDto dataTypeResponseDto, final SpecificationReference specificationReference) {

        final MessageSchemaResponseDto messageSchema = dataTypeResponseDto.getMessageSchema();
        final DataProviderTypeResponseDto dataProviderTypeResponseDto = messageSchema.getMessageDataTopic().getDataProviderType();

        final String augmentedDataOutputTopicName = this.kafkaProperties.getAutoConfigTopics().getAugmentationProcessing().getName();
        final String aasChartName = augmentedDataOutputTopicName.substring(0, augmentedDataOutputTopicName.lastIndexOf('-'));

        final DataSpaceRequestDto dataSpaceRequestDto = DataSpaceRequestDto.builder()
                .name(dataProviderTypeResponseDto.getDataSpace().getName())
                .build();

        final DataCategoryRequestDto dataCategoryRequestDto = DataCategoryRequestDto.builder()
                .dataCategoryName(dataProviderTypeResponseDto.getDataCategoryType()
                        .getDataCategoryName())
                .build();

        final DataProviderTypeRequestDto dataProviderTypeRequestDto = DataProviderTypeRequestDto.builder()
                .providerVersion(dataProviderTypeResponseDto.getProviderVersion())
                .providerTypeId(dataProviderTypeResponseDto.getProviderTypeId())
                .build();

        final MessageDataTopicRequestDto messageDataTopicRequestDto = MessageDataTopicRequestDto.builder()
                .name(augmentedDataOutputTopicName)
                .messageBusId(messageSchema.getMessageDataTopic().getMessageBus().getId())
                .encoding(messageSchema.getMessageDataTopic().getEncoding())
                .build();

        final DataServiceRequestDto dataServiceRequestDto = DataServiceRequestDto.builder()
                .dataServiceName(aasChartName)
                .build();

        final DataServiceInstanceRequestDto dataServiceInstanceRequestDto = DataServiceInstanceRequestDto.builder()
                .dataServiceInstanceName(aasChartName + "-" + dataProviderTypeRequestDto.getProviderTypeId())
                .controlEndPoint(DATA_CATALOG_INSTANCE_END_POINT)
                .consumedDataSpace(dataTypeResponseDto.getConsumedDataSpace())
                .consumedDataCategory(dataTypeResponseDto.getConsumedDataCategory())
                .consumedDataProvider(dataTypeResponseDto.getConsumedDataProvider())
                .consumedSchemaName(dataTypeResponseDto.getSchemaName())
                .consumedSchemaVersion(dataTypeResponseDto.getConsumedSchemaVersion())
                .build();

        final DataTypeRequestDto dataTypeRequestDto = DataTypeRequestDto.builder()
                .mediumType(DATA_CATALOG_DATA_MEDIUM_TYPE)
                .schemaName(specificationReference.getSchemaName())
                .schemaVersion(String.valueOf(specificationReference.getSchemaVersion()))
                .isExternal(dataTypeResponseDto.getIsExternal())
                .consumedDataSpace(dataTypeResponseDto.getConsumedDataSpace())
                .consumedDataCategory(dataTypeResponseDto.getConsumedDataCategory())
                .consumedDataProvider(dataTypeResponseDto.getConsumedDataProvider())
                .consumedSchemaName(dataTypeResponseDto.getSchemaName())
                .consumedSchemaVersion(dataTypeResponseDto.getSchemaVersion())
                .build();

        final InnerMessageSchemaRequestDto innerMessageSchemaRequestDto = InnerMessageSchemaRequestDto.builder()
                .specificationReference(specificationReference.toString())
                .build();

        final MessageSchemaRequestDto.MessageSchemaRequestDtoBuilder messageSchemaRequestDtoBuilder = MessageSchemaRequestDto.builder()
                .dataSpace(dataSpaceRequestDto)
                .dataService(dataServiceRequestDto)
                .dataCategory(dataCategoryRequestDto)
                .dataProviderType(dataProviderTypeRequestDto)
                .messageDataTopic(messageDataTopicRequestDto)
                .dataServiceInstance(dataServiceInstanceRequestDto)
                .dataType(dataTypeRequestDto)
                .messageSchema(innerMessageSchemaRequestDto)
                .id(messageSchema.getId());
        return messageSchemaRequestDtoBuilder.build();
    }

}
