/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.ardq;

import static com.ericsson.oss.air.aas.service.ardq.ArdqService.AUGMENTATION_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import com.ericsson.oss.air.exception.AasValidationException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpUnauthorizedProblemException;
import io.micrometer.core.instrument.Counter;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class ArdqServiceTest {

    private static final String VALID_URL = "http://eric-oss-cardq:8080";

    private static final List<String> AUGMENTED_VALUES = List.of("NSI-B", "NSI-C");

    private static final ArdqRequestDto REQUEST = ArdqRequestDto.newRequest()
            .addInputField("input", "value")
            .addOutputField("output");

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Counter ardqValidResponsesCounter;

    @Mock
    private Counter ardqInvalidResponsesCounter;

    @Mock
    private Counter ardqErrorResponsesCounter;

    private ArdqService ardqService;

    @BeforeEach
    void setup() {

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.ardqService = new ArdqService(this.restTemplate, factory.getValidator(), this.ardqValidResponsesCounter,
                this.ardqInvalidResponsesCounter, this.ardqErrorResponsesCounter);

    }

    @Test
    public void getAugmentationData_validUrl_successResponseWithBody() throws AasValidationException {
        //given
        final ArdqResponseDto ardqResponseDto = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name("nsi").value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name("nsi").value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenReturn((new ResponseEntity<>(ardqResponseDto, HttpStatus.OK)));
        //when
        final var response = this.ardqService.getAugmentationData(VALID_URL, REQUEST);

        //then
        assertNotNull(response.getBody());
        assertThat(response.getBody().getFields()).hasSize(2);
        assertThat(response.getBody().getFields().stream().flatMap(List::stream).map(ArdqResponseDto.AugmentationField::getValue))
                .containsExactlyInAnyOrder("NSI-B", "NSI-C");

        verify(this.ardqValidResponsesCounter, times(1)).increment();
        verify(this.ardqInvalidResponsesCounter, never()).increment();
        verify(this.ardqErrorResponsesCounter, never()).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_valueFieldEmpty_successResponseWithBody() throws AasValidationException {
        //given
        final ArdqResponseDto ardqResponseDto = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name("nsi").value("").build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name("nsi").value("").build())))
                .build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenReturn((new ResponseEntity<>(ardqResponseDto, HttpStatus.OK)));

        //Then
        assertDoesNotThrow(() -> this.ardqService.getAugmentationData(VALID_URL, REQUEST));

        //when
        final var response = this.ardqService.getAugmentationData(VALID_URL, REQUEST);

        //then
        assertNotNull(response.getBody());
        assertThat(response.getBody().getFields()).hasSize(2);
        assertThat(response.getBody().getFields().stream().flatMap(List::stream).map(ArdqResponseDto.AugmentationField::getValue))
                .containsExactlyInAnyOrder("", "");

        verify(this.ardqValidResponsesCounter, times(2)).increment();
        verify(this.ardqInvalidResponsesCounter, never()).increment();
        verify(this.ardqErrorResponsesCounter, never()).increment();

        verify(this.restTemplate, times(2)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_nameFieldEmpty_ExceptionThrown() {
        //given
        final ArdqResponseDto ardqResponseDto = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenReturn((new ResponseEntity<>(ardqResponseDto, HttpStatus.OK)));
        //then
        assertThrows(AasValidationException.class, () -> this.ardqService.getAugmentationData(VALID_URL, REQUEST));

        verify(this.ardqValidResponsesCounter, never()).increment();
        verify(this.ardqInvalidResponsesCounter, times(1)).increment();
        verify(this.ardqErrorResponsesCounter, never()).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_valueListEmpty_ExceptionThrown() {
        //given
        final ArdqResponseDto ardqResponseDto = ArdqResponseDto.builder()
                .fields(List.of(List.of()))
                .build();

        //when
        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenReturn((new ResponseEntity<>(ardqResponseDto, HttpStatus.OK)));

        //then
        assertThrows(AasValidationException.class, () -> this.ardqService.getAugmentationData(VALID_URL, REQUEST));

        verify(this.ardqValidResponsesCounter, never()).increment();
        verify(this.ardqInvalidResponsesCounter, times(1)).increment();
        verify(this.ardqErrorResponsesCounter, never()).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_fieldsListEmpty_success() throws AasValidationException {
        //given
        final ArdqResponseDto ardqResponseDto = ArdqResponseDto.builder()
                .fields(Collections.emptyList())
                .build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenReturn((new ResponseEntity<>(ardqResponseDto, HttpStatus.OK)));

        final var response = this.ardqService.getAugmentationData(VALID_URL, REQUEST);

        //Then
        assertNotNull(response.getBody());
        assertThat(response.getBody().getFields()).isEmpty();

        verify(this.ardqValidResponsesCounter, times(1)).increment();
        verify(this.ardqInvalidResponsesCounter, never()).increment();
        verify(this.ardqErrorResponsesCounter, never()).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_badRequest() {
        //given
        final HttpBadRequestProblemException httpBadRequestProblemException = HttpBadRequestProblemException.builder().build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenThrow(httpBadRequestProblemException);
        //Then
        assertThrows(HttpBadRequestProblemException.class, () -> this.ardqService.getAugmentationData(VALID_URL, REQUEST));

        verify(this.ardqValidResponsesCounter, never()).increment();
        verify(this.ardqInvalidResponsesCounter, never()).increment();
        verify(this.ardqErrorResponsesCounter, times(1)).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    @Test
    void getAugmentationData_unauthorized() {
        //given
        final HttpUnauthorizedProblemException httpUnauthorizedProblemException = HttpUnauthorizedProblemException.builder().build();

        when(this.restTemplate.exchange(VALID_URL + AUGMENTATION_URL, HttpMethod.POST,
                this.getRequestEntity(), ArdqResponseDto.class))
                .thenThrow(httpUnauthorizedProblemException);
        //Then
        assertThrows(HttpUnauthorizedProblemException.class, () -> this.ardqService.getAugmentationData(VALID_URL, REQUEST));

        verify(this.ardqValidResponsesCounter, never()).increment();
        verify(this.ardqInvalidResponsesCounter, never()).increment();
        verify(this.ardqErrorResponsesCounter, times(1)).increment();

        verify(this.restTemplate, times(1)).setErrorHandler(any(ArdqHttpResponseErrorHandler.class));
    }

    private HttpEntity getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<ArdqRequestDto> entity = new HttpEntity<>(REQUEST, headers);

        return entity;
    }
}
