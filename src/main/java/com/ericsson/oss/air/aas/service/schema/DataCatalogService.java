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

package com.ericsson.oss.air.aas.service.schema;

import java.util.Comparator;
import java.util.List;

import com.ericsson.oss.air.aas.model.SchemaReference;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import com.ericsson.oss.air.util.LombokExtensions;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This service is for interacting with the Data Catalog.
 */
@Service
@Slf4j
@NoArgsConstructor
@ExtensionMethod(LombokExtensions.class)
public class DataCatalogService {

    static final String REGISTER_MESSAGE_SCHEMA_URI = "/catalog/v1/message-schema";

    static final String SCHEMA_METADATA_URI = "/catalog/v1/data-type";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${dmm.dataCatalog.url}")
    private String dataCatalogUrl;

    /**
     * Registers a new  schema's metadata in the Data Catalog.
     *
     * @param messageSchemaRequestDto object that contains a schema's metadata
     * @return the registered schema's metadata along with Data Catalog generated values
     */
    public MessageSchemaResponseDto register(
            @NonNull
            final MessageSchemaRequestDto messageSchemaRequestDto) throws HttpProblemException {

        // When register new data type to DC, the ID need to be reset to null.
        messageSchemaRequestDto.setId(null);

        log.info("Register message schema with specification reference: [{}] to the Data Catalog",
                messageSchemaRequestDto.getMessageSchema().getSpecificationReference());

        return this.registerImpl(messageSchemaRequestDto);
    }

    /**
     * Update a schema's metadata in the Data Catalog.
     *
     * @param messageSchemaRequestDto object that contains a schema's metadata
     * @return the registered schema's metadata along with Data Catalog generated values
     */
    public MessageSchemaResponseDto update(
            @NonNull
            final MessageSchemaRequestDto messageSchemaRequestDto) throws HttpProblemException {
        log.info("Update message schema with specification reference: [{}] to the Data Catalog",
                messageSchemaRequestDto.getMessageSchema().getSpecificationReference());

        if (ObjectUtils.isEmpty(messageSchemaRequestDto.getId())) {
            throw new NullPointerException("Can't operate update with message schema id null");
        }

        return this.registerImpl(messageSchemaRequestDto);
    }

    /**
     * Registers a schema's metadata in the Data Catalog.
     *
     * @param messageSchemaRequestDto object that contains a schema's metadata
     * @return the registered schema's metadata along with Data Catalog generated values
     */
    @CircuitBreaker(name = "dataCatalog")
    @Retry(name = "dataCatalog")
    private MessageSchemaResponseDto registerImpl(
            @NonNull
            final MessageSchemaRequestDto messageSchemaRequestDto) throws HttpProblemException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<MessageSchemaRequestDto> requestEntity = new HttpEntity<>(messageSchemaRequestDto, headers);

        this.restTemplate.setErrorHandler(new DataCatalogHttpResponseErrorHandler());

        log.info("Request message: {}", messageSchemaRequestDto.toJsonString());
        return this.restTemplate.exchange(this.dataCatalogUrl + REGISTER_MESSAGE_SCHEMA_URI, HttpMethod.PUT,
                requestEntity, MessageSchemaResponseDto.class).getBody();
    }

    /**
     * Retrieves the schema's metadata from the Data Catalog(DC) for a given schema reference.
     *
     * @param schemaRef a schema reference object
     * @return schema's metadata in the form of DataTypeResponseDto
     */
    @CircuitBreaker(name = "dataCatalog")
    @Retry(name = "dataCatalog")
    public DataTypeResponseDto retrieveSchemaMetadata(
            @NonNull
            final SchemaReference schemaRef) throws HttpProblemException {
        final String dataSpace = schemaRef.getDataSpace();
        final String dataCategory = schemaRef.getDataCategory();
        final String schemaName = schemaRef.getSchemaName();

        log.info("Retrieving metadata for schema reference: [{}|{}|{}] from the Data Catalog", dataSpace, dataCategory, schemaName);

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(dataCatalogUrl + SCHEMA_METADATA_URI)
                .queryParam("dataSpace", dataSpace)
                .queryParam("dataCategory", dataCategory)
                .queryParam("schemaName", schemaName)
                .toUriString();

        log.info("Data Catalog Request URL: {}", endpointUrl);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        this.restTemplate.setErrorHandler(new DataCatalogHttpResponseErrorHandler());

        final ResponseEntity<List<DataTypeResponseDto>> response = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });

        log.info("Retrieved data-type: {}", response.toJsonString());

        final List<DataTypeResponseDto> responseDtoList = response.getBody();
        if (!CollectionUtils.isEmpty(responseDtoList)) {
            return responseDtoList.stream()
                    .max(Comparator.comparing(DataTypeResponseDto::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(null);
        }
        return null;
    }

}
