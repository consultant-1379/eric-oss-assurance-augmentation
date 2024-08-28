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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.AasValidationException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.util.ObjectUtils;

/**
 * Responsible for performing the operations required to create the augmented output data record
 */
public class AugmentationProcessorHandler {

    private AugmentationProcessorHandler() {
    }

    /**
     * Populates the output record with all the un-augmented field values from the incoming record and augmented field values from the ARDQ response.
     *
     * @param incomingRecord     The incoming record from core parser
     * @param augmentationFields The list of {@link ArdqResponseDto.AugmentationField} objects containing the augmented fields and values
     * @param outputSchema       the output schema
     * @return an output record populated with the incoming field values
     */
    public static GenericRecord getPopulatedOutputRecord(final ConsumerRecord<String, GenericRecord> incomingRecord,
                                                         final List<ArdqResponseDto.AugmentationField> augmentationFields,
                                                         final Schema outputSchema) throws AasValidationException {
        // From core-parser's code, it looks like the schema is available in the GenericRecord object. If it is unavailable, instead
        // get the schemaID from the header and query schema registry for the actual schema
        final Schema inputSchema = incomingRecord.value().getSchema();

        final GenericRecord outputRecord = new GenericData.Record(outputSchema);

        final List<String> outputFieldNames = outputSchema.getFields().stream().map(Schema.Field::name).collect(Collectors.toList());

        // Copy all un-augmented fields from the incoming avro record
        for (final Schema.Field field : inputSchema.getFields()) {
            final String fieldName = field.name();
            if (!outputFieldNames.contains(fieldName)) {
                throw new AasValidationException(String.format("The output schema does not contain the field '%s' present in the input schema",
                        field.name()));
            }
            outputRecord.put(fieldName, incomingRecord.value().get(fieldName));
        }

        // Copy all the augmented fields from the ARDQ response
        for (final ArdqResponseDto.AugmentationField augmentationField : augmentationFields) {
            final String fieldName = augmentationField.getName();
            if (!outputFieldNames.contains(fieldName)) {
                throw new AasValidationException(String.format("The output schema does not contain the field '%s' present in the ARDQ response",
                        fieldName));
            }
            outputRecord.put(fieldName, augmentationField.getValue());
        }

        return outputRecord;
    }

    /**
     * Populates the ARDQ query resource with the details from the augmentation processor.
     *
     * @param inputRecord The input record containing the fields and values to be used for retrieving the augmented fields
     * @param fields      the fields
     * @return the populated augmentation request object
     */
    public static ArdqRequestDto getPopulatedArdqRequest(final GenericRecord inputRecord, final List<ArdqAugmentationFieldDto> fields,
                                                         final String queryType) throws AasValidationException {

        final Map<String, Object> recordFields = new HashMap<>();

        inputRecord.getSchema().getFields().forEach(field -> recordFields.put(field.name(), inputRecord.get(field.name())));

        final ArdqRequestDto ardqRequestDto = ArdqRequestDto.newRequest();

        for (final ArdqAugmentationFieldDto field : fields) {
            ardqRequestDto.addOutputField(field.getOutput());

            for (final String inputName : field.getInput()) {
                if (recordFields.containsKey(inputName)) {
                    ardqRequestDto.addInputField(inputName, ObjectUtils.nullSafeToString(recordFields.get(inputName)));
                } else {
                    throw new AasValidationException("The deserialized avro record provided does not contain the field: " + inputName);
                }
            }
        }
        if (queryType != null) {
            ardqRequestDto.setQueryType(queryType);
        }
        return ardqRequestDto;
    }

}
