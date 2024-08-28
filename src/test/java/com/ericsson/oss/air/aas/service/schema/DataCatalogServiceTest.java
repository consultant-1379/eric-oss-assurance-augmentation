/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.schema;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE_OBJ;
import static com.ericsson.oss.air.aas.service.schema.DataCatalogService.REGISTER_MESSAGE_SCHEMA_URI;
import static com.ericsson.oss.air.aas.service.schema.DataCatalogService.SCHEMA_METADATA_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.aas.model.datacatalog.request.InnerMessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class DataCatalogServiceTest {

    private static final String DATA_CATALOG_URL = "http://localhost:9590";

    @InjectMocks
    private DataCatalogService dataCatalogService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(this.dataCatalogService, "dataCatalogUrl", "http://localhost:9590");
    }

    @Test
    void register_ValidMessageSchemaRequestDto_Success() {
        final MessageSchemaResponseDto messageSchemaResponseDto = MessageSchemaResponseDto.builder().build();

        when(this.restTemplate.exchange(DATA_CATALOG_URL + REGISTER_MESSAGE_SCHEMA_URI, HttpMethod.PUT,
                this.getRequestEntityForMessageSchemaRequestDto(this.buildMessageSchemaRequestDto()), MessageSchemaResponseDto.class))
                .thenReturn(new ResponseEntity<>(messageSchemaResponseDto, HttpStatus.OK));

        assertThat(this.dataCatalogService.register(buildMessageSchemaRequestDto())).isEqualTo(messageSchemaResponseDto);

        verify(this.restTemplate, times(1)).setErrorHandler(any(DataCatalogHttpResponseErrorHandler.class));

    }

    @Test
    void register_NullMessageSchemaRequestDto_ThrowsException() {
        assertThrows(NullPointerException.class, () -> this.dataCatalogService.register(null));
    }

    @Test
    void update_ValidMessageSchemaRequestDto_Success() {

        final MessageSchemaResponseDto messageSchemaResponseDto = MessageSchemaResponseDto.builder().build();
        final MessageSchemaRequestDto messageSchemaRequestDto = this.buildMessageSchemaRequestDto();
        messageSchemaRequestDto.setId(1);

        when(this.restTemplate.exchange(DATA_CATALOG_URL + REGISTER_MESSAGE_SCHEMA_URI, HttpMethod.PUT,
                this.getRequestEntityForMessageSchemaRequestDto(messageSchemaRequestDto), MessageSchemaResponseDto.class))
                .thenReturn(new ResponseEntity<>(messageSchemaResponseDto, HttpStatus.OK));

        assertThat(this.dataCatalogService.update(messageSchemaRequestDto)).isEqualTo(messageSchemaResponseDto);

        verify(this.restTemplate, times(1)).setErrorHandler(any(DataCatalogHttpResponseErrorHandler.class));
    }

    @Test
    void update_invalidMessageSchemaRequestDto_Failed() {
        // When id is missing from update request, an NullPointerException will be raised.
        assertThrows(NullPointerException.class, () -> this.dataCatalogService.update(this.buildMessageSchemaRequestDto()));
    }

    @Test
    void retrieveInputSchemaMetadata_Success() {
        final DataTypeResponseDto dataTypeResponseDto1 = DataTypeResponseDto.builder().build();
        final DataTypeResponseDto dataTypeResponseDto2 = DataTypeResponseDto.builder().build();
        final List<DataTypeResponseDto> responseDtoList = List.of(dataTypeResponseDto1, dataTypeResponseDto2);

        when(this.restTemplate.exchange(this.buildEndpointUrl(), HttpMethod.GET, this.getRequestEntity(),
                new ParameterizedTypeReference<List<DataTypeResponseDto>>() {
                }))
                .thenReturn(new ResponseEntity<>(responseDtoList, HttpStatus.OK));

        assertThat(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).isEqualTo(dataTypeResponseDto1);

        verify(this.restTemplate, times(1)).setErrorHandler(any(DataCatalogHttpResponseErrorHandler.class));
    }

    @Test
    void retrieveInputSchemaMetadata_Success_returnLatestSchema() {
        final DataTypeResponseDto dataTypeResponseDto1 = DataTypeResponseDto.builder().build();
        final DataTypeResponseDto dataTypeResponseDto2 = DataTypeResponseDto.builder().id(1).build();
        final DataTypeResponseDto dataTypeResponseDto3 = DataTypeResponseDto.builder().id(10).build();
        final DataTypeResponseDto dataTypeResponseDto4 = DataTypeResponseDto.builder().id(99).build();
        final List<DataTypeResponseDto> responseDtoList = List.of(dataTypeResponseDto1, dataTypeResponseDto2, dataTypeResponseDto3,
                dataTypeResponseDto4);

        when(this.restTemplate.exchange(this.buildEndpointUrl(), HttpMethod.GET, this.getRequestEntity(),
                new ParameterizedTypeReference<List<DataTypeResponseDto>>() {
                }))
                .thenReturn(new ResponseEntity<>(responseDtoList, HttpStatus.OK));

        assertThat(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).isEqualTo(dataTypeResponseDto4);

        verify(this.restTemplate, times(1)).setErrorHandler(any(DataCatalogHttpResponseErrorHandler.class));
    }

    @Test
    void retrieveInputSchemaMetadata_Not_Found() {
        when(this.restTemplate.exchange(this.buildEndpointUrl(), HttpMethod.GET, this.getRequestEntity(),
                new ParameterizedTypeReference<List<DataTypeResponseDto>>() {
                }))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        assertThat(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).isNull();

        verify(this.restTemplate, times(1)).setErrorHandler(any(DataCatalogHttpResponseErrorHandler.class));
    }

    @Test
    void retrieveInputSchemaMetaData_NullInputSchemaRef_ThrowsException() {
        assertThrows(NullPointerException.class, () -> this.dataCatalogService.retrieveSchemaMetadata(null));
    }

    private MessageSchemaRequestDto buildMessageSchemaRequestDto() {
        final MessageSchemaRequestDto messageSchemaRequestDto = MessageSchemaRequestDto.builder()
                .messageSchema(InnerMessageSchemaRequestDto.builder()
                        .specificationReference("foobar")
                        .build())
                .build();

        return messageSchemaRequestDto;
    }

    private HttpEntity getRequestEntityForMessageSchemaRequestDto(final MessageSchemaRequestDto messageSchemaRequestDto) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<MessageSchemaRequestDto> requestEntity = new HttpEntity<>(messageSchemaRequestDto, headers);

        return requestEntity;
    }

    private String buildEndpointUrl() {
        final String endpointUrl = UriComponentsBuilder
                .fromUriString(DATA_CATALOG_URL + SCHEMA_METADATA_URI)
                .queryParam("dataSpace", "5G")
                .queryParam("dataCategory", "PM_COUNTERS")
                .queryParam("schemaName", "AMF_Mobility_NetworkSlice_1")
                .toUriString();

        return endpointUrl;
    }

    private HttpEntity getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }

}
