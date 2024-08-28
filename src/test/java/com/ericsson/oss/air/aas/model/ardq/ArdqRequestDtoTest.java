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

package com.ericsson.oss.air.aas.model.ardq;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto.AugmentationFieldRequest;
import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto.QueryField;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class ArdqRequestDtoTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void constructor_initially_shouldCreateEmptyInputAndOutputFieldsList() {
        // When
        var request = ArdqRequestDto.newRequest();

        // Then
        assertThat(request.getInputFields()).isEmpty();
        assertThat(request.getAugmentationFields()).isEmpty();
    }

    @Test
    void addInputField_shouldAddNewInputFieldToInstance() {
        // Given
        var request = ArdqRequestDto.newRequest();

        // When
        request.addInputField("name", "value");

        // Then
        assertThat(request.getInputFields()).hasSize(1)
                .containsExactly(QueryField.of("name", "value"));

        // When
        request.addInputField("anotherName", "anotherValue");

        // Then
        assertThat(request.getInputFields()).hasSize(2)
                .containsExactlyInAnyOrder(
                        QueryField.of("name", "value"),
                        QueryField.of("anotherName", "anotherValue")
                );

        // When
        request.addInputField("anotherName", "anotherValue");

        // Then
        assertThat(request.getInputFields()).hasSize(2)
                .containsExactlyInAnyOrder(
                        QueryField.of("name", "value"),
                        QueryField.of("anotherName", "anotherValue")
                );
    }

    @Test
    void addOutputFields_shouldAddNewOutputFieldToInstance() {
        // Given
        var request = ArdqRequestDto.newRequest();

        // When
        request.addOutputField("output");

        // Then
        assertThat(request.getAugmentationFields()).hasSize(1)
                .containsExactly(AugmentationFieldRequest.of("output"));

        // When
        request.addOutputField("field");

        // Then
        assertThat(request.getAugmentationFields()).hasSize(2)
                .containsExactly(
                        AugmentationFieldRequest.of("output"),
                        AugmentationFieldRequest.of("field")
                );
    }

    @Test
    void shouldSerializeInstance() throws IOException, JSONException {
        // Given
        var request =
                ArdqRequestDto.newRequest()
                        .addInputField("first", "firstValue")
                        .addInputField("second", "secondValue")
                        .addOutputField("output")
                        .addOutputField("another");

        // When
        var json = mapper.writeValueAsString(request);

        // Then
        var expectedString = "{\"inputFields\":" +
                "[{\"name\":\"first\",\"value\":\"firstValue\"}," +
                "{\"name\":\"second\",\"value\":\"secondValue\"}]," +
                "\"augmentationFields\":" +
                "[{\"name\":\"output\"}," +
                "{\"name\": \"another\"}]}";
        JSONAssert.assertEquals(expectedString, json, false);
    }

    @Test
    void shouldSerializeInstanceWithQueryType() throws IOException, JSONException {
        // Given
        var request =
                ArdqRequestDto.newRequest()
                        .addInputField("first", "firstValue")
                        .addInputField("second", "secondValue")
                        .addOutputField("output")
                        .addOutputField("another")
                        .setQueryType("core");

        // When
        var json = mapper.writeValueAsString(request);

        // Then
        var expectedString = "{\"inputFields\":" +
                "[{\"name\":\"first\",\"value\":\"firstValue\"}," +
                "{\"name\":\"second\",\"value\":\"secondValue\"}]," +
                "\"augmentationFields\":" +
                "[{\"name\":\"output\"}," +
                "{\"name\": \"another\"}]}" +
                "\"queryType\": \"core\"";
        JSONAssert.assertEquals(expectedString, json, false);
    }
}