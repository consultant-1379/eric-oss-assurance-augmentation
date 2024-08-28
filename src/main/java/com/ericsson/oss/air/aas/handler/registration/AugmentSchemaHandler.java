/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler.registration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.handler.registration.provider.MessageSchemaRequestDtoProvider;
import com.ericsson.oss.air.aas.handler.registration.provider.OutputSchemaNameProvider;
import com.ericsson.oss.air.aas.handler.registration.validation.AugmentationFieldsValidator;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.model.SchemaReference;
import com.ericsson.oss.air.aas.model.SpecificationReference;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.record.SchemaNamespace;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.schema.DataCatalogService;
import com.ericsson.oss.air.aas.service.schema.SchemaRegistryService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.SchemaNamespaceParseException;
import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import com.ericsson.oss.air.exception.SpecificationReferenceParseException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpInternalServerErrorException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpServiceUnavailableException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler class to create/update augmented schema
 */
@Component
@Slf4j
public class AugmentSchemaHandler {

    @Autowired
    private ArdqRegistrationDao ardqRegistrationDao;

    @Autowired
    private SchemaDao schemaDao;

    @Autowired
    private DataCatalogService dataCatalogService;

    @Autowired
    private SchemaRegistryService schemaRegistryService;

    @Autowired
    private MessageSchemaRequestDtoProvider messageSchemaRequestDtoProvider;

    @Autowired
    private OutputSchemaNameProvider outputSchemaNameProvider;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private FaultHandler faultHandler;

    /**
     * Get a list of {@link Schema.Field} from target schema and ArdqAugmentationFieldDto list.
     *
     * @param targetSchema Schema
     * @param fieldDtoList List of ArdqAugmentationFieldDto
     * @return list of {@link Schema.Field}
     */
    private static List<Schema.Field> compileFields(final Schema targetSchema, final List<ArdqAugmentationFieldDto> fieldDtoList) {
        final List<Schema.Field> fields = getUnindexedFields(targetSchema);

        final List<Schema.Field> missingFields = fieldDtoList.stream()
                .map(ArdqAugmentationFieldDto::getOutput)
                .filter(fieldName -> targetSchema.getField(fieldName) == null)
                .map(fieldName -> new Schema.Field(fieldName, Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.STRING)),
                        null, Schema.Field.NULL_DEFAULT_VALUE))
                .collect(Collectors.toList());

        fields.addAll(missingFields);

        return fields;
    }

    /**
     * Creates an output schema based on the input schema information, output schema name and augments with additional field
     *
     * @param inputSchema                  the input schema
     * @param schemaName                   the schema name
     * @param ardqAugmentationFieldDtoList List of ArdqAugmentationFieldDtos
     * @return an augmented schema
     */
    static Schema createOutputSchema(final Schema inputSchema, final String schemaName,
                                     final List<ArdqAugmentationFieldDto> ardqAugmentationFieldDtoList) {
        // Create the output schema from input schema
        final Schema outputSchema = Schema.createRecord(schemaName, inputSchema.getDoc(), inputSchema.getNamespace(),
                inputSchema.isError());

        // if the output field does not exist in the target schema, add it to the output schema
        outputSchema.setFields(AugmentSchemaHandler.compileFields(inputSchema, ardqAugmentationFieldDtoList));
        return outputSchema;
    }

    /**
     * Creates an output schema based on existing output schema information and augments with additional field
     *
     * @param outputSchema                 the output schema
     * @param ardqAugmentationFieldDtoList the ardq augmentation field dto list
     * @return the schema
     */
    static Schema updateOutputSchema(final Schema outputSchema, final List<ArdqAugmentationFieldDto> ardqAugmentationFieldDtoList) {

        // if the output field does not exist in the target schema, add it to the output schema
        final List<Schema.Field> fields = AugmentSchemaHandler.compileFields(outputSchema, ardqAugmentationFieldDtoList);

        return Schema.createRecord(outputSchema.getName(), outputSchema.getDoc(), outputSchema.getNamespace(),
                outputSchema.isError(), fields);
    }

    /**
     * Creates output SchemaReference object from input SchemaReference and ardq id
     *
     * @param inputSchemaReference the input schema reference
     * @param ardqId               the ardq id
     * @return the output schema reference object
     */
    private SchemaReference createOutputSchemaReference(final SchemaReference inputSchemaReference, final String ardqId) {

        final String outputSchemaName = this.outputSchemaNameProvider.generate(ardqId, inputSchemaReference.getSchemaName());

        return SchemaReference.builder()
                .withSchemaName(outputSchemaName)
                .withDataCategory(inputSchemaReference.getDataCategory())
                .withDataSpace(inputSchemaReference.getDataSpace())
                .build();
    }

    /**
     * Gets a list of un-indexed schema fields so they can be added to newly created schema.
     *
     * @param schema the schema
     * @return the un-indexed schema fields
     */
    private static List<Schema.Field> getUnindexedFields(final Schema schema) {
        return schema.getFields()
                .stream()
                .map(field -> new Schema.Field(field.name(), field.schema(), field.doc(), field.defaultVal()))
                .collect(Collectors.toList());
    }

    private static List<ArdqAugmentationFieldDto> getFieldsInRules(final List<ArdqAugmentationRuleDto> rules) {
        return rules.stream()
                .map(ArdqAugmentationRuleDto::getFields)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ArdqAugmentationFieldDto::getOutput))
                .collect(Collectors.toList());
    }

    /**
     * Returns a map of input schema references mapped to their {@link ArdqAugmentationFieldDto}'s from the provided {@link ArdqRegistrationDto}.
     *
     * @param ardqRegistrationDto the ARDQ Registration
     * @return a map of input schema references mapped to their {@link ArdqAugmentationFieldDto}'s
     */
    static Map<String, List<ArdqAugmentationFieldDto>> getFieldsPerInputSchemaReferenceMap(final ArdqRegistrationDto ardqRegistrationDto) {
        return ardqRegistrationDto.getRules().stream()
                .collect(Collectors.groupingBy(ArdqAugmentationRuleDto::getInputSchema,
                        Collectors.collectingAndThen(Collectors.toList(), AugmentSchemaHandler::getFieldsInRules)));
    }

    /**
     * Create {@code SpecificationReference} object.
     *
     * @param inputSchemaDataType the input schema data type
     * @param schemaReference     the schema reference
     * @return the SpecificationReference object
     */
    static SpecificationReference createSpecificationReference(final DataTypeResponseDto inputSchemaDataType,
                                                               final SchemaReference schemaReference) {

        final String dataProvider = inputSchemaDataType.getMessageSchema().getMessageDataTopic().getDataProviderType().getProviderTypeId();

        return SpecificationReference.builder()
                .dataSpace(schemaReference.getDataSpace())
                .dataProvider(dataProvider)
                .dataCategory(schemaReference.getDataCategory())
                .schemaName(schemaReference.getSchemaName())
                .build();
    }

    /**
     * Parse schema reference string to {@code SchemaReference} Object with correct HTTP problem exception
     *
     * @param schemaReferenceStr a schema reference
     * @return {@code SchemaReference}
     * @throws HttpBadRequestProblemException throw HttpProblemException exception when schema reference is not correct
     */
    static SchemaReference parseSchemaReference(final String schemaReferenceStr) throws HttpBadRequestProblemException {
        final SchemaReference schemaReference;
        try {
            schemaReference = SchemaReference.parse(schemaReferenceStr);
        } catch (final SchemaReferenceParseException e) {
            final String errorMessage = String.format("Unable to parse schema reference: [%s]", schemaReferenceStr);
            log.debug(errorMessage, e);
            throw HttpBadRequestProblemException.builder()
                    .description(e.getMessage())
                    .build();
        }
        return schemaReference;
    }

    /**
     * Return a SchemaSubject object from Schema object
     *
     * @param schema the schema
     * @return the schema subject
     * @throws HttpBadRequestProblemException the http bad request problem exception
     */
    static SchemaSubject parseSchemaSubject(final Schema schema) throws HttpBadRequestProblemException {

        final SchemaNamespace nameSpace;
        try {
            nameSpace = SchemaNamespace.parse(schema.getNamespace());
        } catch (final SchemaNamespaceParseException e) {
            log.debug(e.getMessage(), e);
            throw HttpBadRequestProblemException.builder()
                    .description(e.getMessage())
                    .build();
        }

        return new SchemaSubject(schema.getName(), nameSpace);
    }

    /**
     * Gets specification reference data type response dto.
     *
     * @param dataTypeResponse the data type response
     * @return the specification reference data type response dto
     */
    private static SpecificationReference getSpecificationReferenceDataTypeResponseDto(final DataTypeResponseDto dataTypeResponse) {
        final String specificationReference = dataTypeResponse.getMessageSchema().getSpecificationReference();
        try {
            return SpecificationReference.parse(specificationReference);
        } catch (final SpecificationReferenceParseException e) {
            final String errorMessage = String.format("Unable to parse specification reference: [%s]", specificationReference);
            log.error(errorMessage, e);
            throw HttpInternalServerErrorException.builder()
                    .description(e.getMessage())
                    .build();
        }
    }

    /**
     * Creates augmented schemata
     *
     * @param registrationDto the registration dto
     * @return a list of {@code IoSchema} to be updated
     */
    public List<IoSchema> create(final ArdqRegistrationDto registrationDto) {

        final String ardqId = registrationDto.getArdqId();

        final List<IoSchema> ioSchemasList = new ArrayList<>();

        // For each unique input schema in the ARDQ registration
        getFieldsPerInputSchemaReferenceMap(registrationDto).forEach((inputSchemaReferenceStr, fieldsList) -> {

            // 1. get the input schema from DMM
            final SchemaReference inputSchemaReference = parseSchemaReference(inputSchemaReferenceStr);
            final DataTypeResponseDto inputSchemaDataType = this.getDataTypeResponse(inputSchemaReference);
            final SpecificationReference inputSpecificationReference = getSpecificationReferenceDataTypeResponseDto(inputSchemaDataType);
            final Schema inputSchema = this.retrieveInputSchema(inputSpecificationReference.getSchemaSubject(), ardqId);

            // Validating the input and output fields against input schema
            AugmentationFieldsValidator.fieldValidator.test(fieldsList, inputSchema);

            final SchemaReference outputSchemaReference = this.handlerOutputSchema(inputSchemaReferenceStr,
                    fieldsList,
                    inputSchemaReference,
                    ardqId,
                    inputSpecificationReference,
                    inputSchema,
                    inputSchemaDataType);

            ioSchemasList.add(new IoSchema(ardqId, inputSchemaReferenceStr, outputSchemaReference.toString()));
        });

        return ioSchemasList;
    }

    private SchemaReference handlerOutputSchema(final String inputSchemaReferenceStr, final List<ArdqAugmentationFieldDto> fieldsList,
                                                final SchemaReference inputSchemaReference, final String ardqId,
                                                final SpecificationReference inputSpecificationReference, final Schema inputSchema,
                                                final DataTypeResponseDto inputSchemaDataType) {
        // 2. create the output schema in memory
        final SchemaReference outputSchemaReference = createOutputSchemaReference(inputSchemaReference, ardqId);
        final SpecificationReference outputSchemaSpecificationReference = inputSpecificationReference.toBuilder()
                .schemaName(outputSchemaReference.getSchemaName()).build();

        final Optional<DataTypeResponseDto> outputSchemaDataType = this.getDataTypeResponseOptional(outputSchemaReference);
        final Schema outputSchema = this.getOutputSchema(fieldsList, inputSchema, outputSchemaReference);
        log.info("[{}] Creating output schema [{}] from input schema [{}]", ardqId, outputSchemaReference.getSchemaName(),
                inputSchemaReference.getSchemaName());

        // 3.  register the output schema in the DMM
        this.registerOutputSchemaToSR(outputSchema, ardqId);
        // get version for output schema from Schema Registry
        outputSchemaSpecificationReference.setSchemaVersion(this.getSchemaVersionFromSR(outputSchema, ardqId));
        this.registerOutputSchemaToDC(inputSchemaDataType, outputSchemaDataType, outputSchemaSpecificationReference);

        // 4. Store schema information in AAS
        this.schemaDao.saveSchema(inputSchemaReferenceStr, inputSchema);
        this.schemaDao.saveSchema(outputSchemaReference.toString(), outputSchema);
        return outputSchemaReference;
    }

    /**
     * Updates augmented schemata
     *
     * @param registrationDto the registration dto
     * @return a list of {@code IoSchema} to be updated
     */
    public List<IoSchema> update(final ArdqRegistrationDto registrationDto) {

        final String ardqId = registrationDto.getArdqId();

        final List<IoSchema> ioSchemasList = new ArrayList<>();

        final Optional<ArdqRegistrationDto> registrationFromDb = this.ardqRegistrationDao.findByArdqId(ardqId);
        if (registrationFromDb.isEmpty()) {
            // This should be prevented by the ObjectMissingValidator
            return ioSchemasList;
        }

        final ArdqRegistrationDto ardqRegistrationDto = registrationFromDb.get();

        // For each unique input schema in the ARDQ registration
        getFieldsPerInputSchemaReferenceMap(registrationDto).forEach((inputSchemaReferenceStr, fieldsList) -> {

            // 1. get the input schema from DMM
            final SchemaReference inputSchemaReference = parseSchemaReference(inputSchemaReferenceStr);
            final DataTypeResponseDto inputSchemaDataType = this.getDataTypeResponse(inputSchemaReference);
            final SpecificationReference inputSpecificationReference = getSpecificationReferenceDataTypeResponseDto(inputSchemaDataType);
            final Schema inputSchema = this.retrieveInputSchema(inputSpecificationReference.getSchemaSubject(), ardqId);

            // Validating the input and output fields against input schema
            log.debug("Validating input and output fields for ardq id: [{}] with schema reference: [{}] ", ardqId, inputSchemaReferenceStr);
            AugmentationFieldsValidator.fieldValidator.test(fieldsList, inputSchema);

            // Validating the output field against registration resource
            // if the output field exists in the existing registration, check for conflicting input field specifications in registration.
            // i.e. the input field specification is different from existing input field, return a 409 - Conflict. Flow ends.
            final List<ArdqAugmentationFieldDto> existingFieldsList = new ArrayList<>();
            ardqRegistrationDto.getRules().forEach(ardqAugmentationRuleDto -> {
                if (ardqAugmentationRuleDto.getInputSchema().equals(inputSchemaReferenceStr)) {
                    existingFieldsList.addAll(ardqAugmentationRuleDto.getFields());
                }
            });
            log.debug("Validating fields conflict for ardq id: [{}] with schema reference: [{}] ", ardqId, inputSchemaReferenceStr);
            AugmentationFieldsValidator.validateFieldsConflict(fieldsList, existingFieldsList);

            final SchemaReference outputSchemaReference = this.handlerOutputSchema(inputSchemaReferenceStr,
                    fieldsList,
                    inputSchemaReference,
                    ardqId,
                    inputSpecificationReference,
                    inputSchema,
                    inputSchemaDataType);

            ioSchemasList.add(new IoSchema(ardqId, inputSchemaReferenceStr, outputSchemaReference.toString()));
        });

        return ioSchemasList;
    }

    /**
     * Initiate a database resync for the provided registrationDto. This will repopulate the data in both the schema registry and the data catalog if
     * they are missing
     *
     * @param registrationDto the registration dto
     */
    public void resync(final ArdqRegistrationDto registrationDto) {

        final List<IoSchema> schemaMappings = this.schemaDao.getIOSchemas(registrationDto.getArdqId());

        schemaMappings.forEach(ioSchema -> {

            final SchemaReference inputSchemaReference = parseSchemaReference(ioSchema.getInputSchemaReference());
            final SchemaReference outputSchemaReference = parseSchemaReference(ioSchema.getOutputSchemaReference());

            final String ardqId = ioSchema.getArdqRegistrationId();

            final Optional<Schema> outputSchemaOptional = this.schemaDao.getSchema(outputSchemaReference.toString());

            if (outputSchemaOptional.isEmpty()) {
                log.error("Failed to resynchronize schema entries with DMM due to missing schema: {}", outputSchemaReference);
                return;
            }

            final Schema outputSchema = outputSchemaOptional.get();
            final SchemaSubject schemaSubject = parseSchemaSubject(outputSchema);

            final boolean isSchemaFoundInSR;

            try {
                isSchemaFoundInSR = this.schemaRegistryService.isSchemaFound(schemaSubject.toString(), new AvroSchema(outputSchema));
            } catch (final RestClientException | IOException e) {
                this.faultHandler.fatal("Failed to resynchronize schema entries with DMM for " + ardqId, e);
                return;
            }

            if (isSchemaFoundInSR) {
                return;
            }

            log.warn("Expected augmented schema [{}] is absent from the Schema Registry. This could be caused by a partially restored DMM database.",
                    schemaSubject);
            log.warn(
                    "AAS cannot function with a corrupted DMM database. It will repopulate the missing data entries in both the Schema Registry and"
                            + " the Data Catalog.");

            // Update schema registry
            this.registerOutputSchemaToSR(outputSchema, ardqId);

            // Update Data catalog
            final DataTypeResponseDto inputSchemaDataType = this.getDataTypeResponse(inputSchemaReference);
            final SpecificationReference inputSpecificationReference = getSpecificationReferenceDataTypeResponseDto(inputSchemaDataType);
            final SpecificationReference outputSchemaSpecificationReference = inputSpecificationReference.toBuilder()
                    .schemaName(outputSchemaReference.getSchemaName())
                    .schemaVersion(this.getSchemaVersionFromSR(outputSchema, ardqId))
                    .build();

            final Optional<DataTypeResponseDto> outputSchemaDataType = this.getDataTypeResponseOptional(outputSchemaReference);
            this.registerOutputSchemaToDC(inputSchemaDataType, outputSchemaDataType, outputSchemaSpecificationReference);
        });
    }

    private Schema retrieveInputSchema(final String subject, final String registrationId) {
        final String errorPrefix = "Error occurred when retrieving input schema from DMM for registration Id '" + registrationId + "'";

        // query the SR for the schema
        final Optional<AvroSchema> schema = this.getSchemaFromSchemaRegistry(subject, errorPrefix);
        if (schema.isPresent()) {
            log.info("Retrieved input schema: {} successfully for registration: [{}]", schema.get().rawSchema(), registrationId);
            return schema.get().rawSchema();
        }

        final String errorMessage = errorPrefix + ": Unable to return the input schema from Schema Registry.";
        log.error(errorMessage);
        throw HttpInternalServerErrorException.builder()
                .description(errorMessage)
                .build();
    }

    private DataTypeResponseDto getDataTypeResponse(final SchemaReference schemaReference) {
        log.info("Retrieving schema metadata for input schema reference: [{}]", schemaReference);

        // Get the schema's metadata from the DC
        final DataTypeResponseDto dataTypeResponseDto;
        try {
            dataTypeResponseDto = this.dataCatalogService.retrieveSchemaMetadata(schemaReference);
        } catch (final HttpNotFoundRequestProblemException exception) {
            final String errorMsg = "Unable to found resource for schema reference '" + schemaReference.toString() + "' with error message: "
                    + exception.getDescription();
            this.faultHandler.fatal(errorMsg, exception);
            // return 400 bad request to the AAS client
            throw HttpBadRequestProblemException.builder().description(errorMsg).build();
        }

        if (dataTypeResponseDto == null) {
            final String errorMessage = "Unable to retrieve the schema metadata from Data Catalog for schema reference '" + schemaReference + "'.";
            log.error(errorMessage);
            throw HttpBadRequestProblemException.builder()
                    .description(errorMessage)
                    .build();
        }

        log.info("Retrieved schema metadata successfully for schema reference: [{}]", schemaReference);
        // This should only be triggered in debug mode and for debug purpose only
        this.parseResponseObjectToJson(dataTypeResponseDto);
        return dataTypeResponseDto;
    }

    private Optional<DataTypeResponseDto> getDataTypeResponseOptional(final SchemaReference schemaReference) {
        log.info("Retrieving schema metadata for output schema reference: [{}]", schemaReference);

        // Get the schema's metadata from the DC
        final DataTypeResponseDto dataTypeResponseDto;
        try {
            dataTypeResponseDto = this.dataCatalogService.retrieveSchemaMetadata(schemaReference);
        } catch (final HttpNotFoundRequestProblemException exception) {  //NOSONAR
            return Optional.empty();
        }

        if (dataTypeResponseDto == null) {
            return Optional.empty();
        }

        return Optional.of(dataTypeResponseDto);
    }

    private Optional<AvroSchema> getSchemaFromSchemaRegistry(final String subject, final String errorPrefix) {
        final String errorMessage = errorPrefix + ": Unable to retrieve the latest schema from Schema Registry because of ";
        final Optional<AvroSchema> schema;
        try {
            schema = this.schemaRegistryService.getLatestSchema(subject);
        } catch (final RestClientException restClientException) {
            this.faultHandler.fatal(errorMessage, restClientException);
            throw HttpInternalServerErrorException.builder()
                    .description(errorMessage + restClientException.getMessage())
                    .build();
        } catch (final IOException ioException) {
            this.faultHandler.fatal(errorMessage, ioException);
            throw HttpServiceUnavailableException.builder()
                    .description(errorMessage + ioException.getMessage())
                    .build();
        }
        return schema;
    }

    private Schema getOutputSchema(final List<ArdqAugmentationFieldDto> fieldsList,
                                   final Schema inputSchema,
                                   final SchemaReference outputSchemaReference) {

        return this.schemaDao.getSchema(outputSchemaReference.toString())
                .map(schema -> updateOutputSchema(schema, fieldsList))
                .orElseGet(() -> createOutputSchema(inputSchema, outputSchemaReference.getSchemaName(), fieldsList));
    }

    /**
     * Register output schema to DC. If outputSchemaDataType doesn't exist, using inputSchemaDataType to build MessageSchemaRequestDto
     *
     * @param inputSchemaDataType                the input schema data type
     * @param outputSchemaDataType               the output schema data type
     * @param outputSchemaSpecificationReference the output schema specification reference
     */
    private void registerOutputSchemaToDC(final DataTypeResponseDto inputSchemaDataType,
                                          final Optional<DataTypeResponseDto> outputSchemaDataType,
                                          final SpecificationReference outputSchemaSpecificationReference) {

        // Building the MessageSchemaRequestDto from input schema metadata when no output metadata available
        if (outputSchemaDataType.isEmpty()) {
            final MessageSchemaRequestDto messageSchemaRequestDto = this.messageSchemaRequestDtoProvider.build(inputSchemaDataType,
                    outputSchemaSpecificationReference);
            this.dataCatalogService.register(messageSchemaRequestDto);

            log.info("Registered MessageSchema: {} successfully to Data Catalog with reference: [{}]", messageSchemaRequestDto,
                    outputSchemaSpecificationReference);
            return;
        }

        // When expected schema version matches, no update required
        if (outputSchemaDataType.get().getSchemaVersion().equals(String.valueOf(outputSchemaSpecificationReference.getSchemaVersion()))) {
            log.info("Skip Data Catalog registration because it existed already with reference: [{}] for version: [{}] ",
                    outputSchemaSpecificationReference,
                    outputSchemaSpecificationReference.getSchemaVersion());
            return;
        }

        // Building the MessageSchemaRequestDto from output schema metadata and submit registration request
        final MessageSchemaRequestDto messageSchemaRequestDto = this.messageSchemaRequestDtoProvider.build(
                outputSchemaDataType.get(),
                outputSchemaSpecificationReference);

        this.dataCatalogService.update(messageSchemaRequestDto);

        log.info("Updated MessageSchema: {} successfully to Data Catalog with reference: [{}]", messageSchemaRequestDto,
                outputSchemaSpecificationReference);
    }

    private void registerOutputSchemaToSR(final Schema outputSchema, final String ardqId) {
        final SchemaSubject schemaSubject = parseSchemaSubject(outputSchema);

        final String errorMessage = "Error occurred when registering output schema to Schema Registry ";
        final AvroSchema avroSchema = new AvroSchema(outputSchema);

        log.debug("Registering output schema {} to Schema Registry", avroSchema);
        try {
            this.schemaRegistryService.register(schemaSubject.toString(), avroSchema);
        } catch (final RestClientException | IOException e) {
            this.faultHandler.fatal(errorMessage + avroSchema + " schema issues ", e);
            throw HttpInternalServerErrorException.builder()
                    .description(e.getMessage())
                    .build();
        }

        log.info("Updated augmented schema: {} successfully for registration: [{}]", outputSchema, ardqId);
    }

    private int getSchemaVersionFromSR(final Schema outputSchema, final String ardqId) {
        final SchemaSubject schemaSubject = parseSchemaSubject(outputSchema);

        final String errorMessage = "Error occurred when getting output schema version from Schema Registry ";
        final AvroSchema avroSchema = new AvroSchema(outputSchema);
        final int version;
        try {
            version = this.schemaRegistryService.getVersion(schemaSubject.toString(), avroSchema);
        } catch (final RestClientException | IOException e) {
            this.faultHandler.fatal(errorMessage + avroSchema + " schema issues ", e);
            throw HttpInternalServerErrorException.builder()
                    .description(e.getMessage())
                    .build();
        }

        log.info("Retrieved output schema version: [{}] successfully for schema [{}] and registration: [{}]", version, outputSchema.getName(),
                ardqId);
        return version;
    }

    private void parseResponseObjectToJson(final Object response) {
        try {
            log.debug(this.mapper.writeValueAsString(response));
        } catch (final JsonProcessingException e) {
            final String errorMsg = "Unable to process response '" + response.toString() + "' to Json format." + e.getMessage();
            log.error(errorMsg, e);
            throw HttpInternalServerErrorException.builder().description(errorMsg).build();
        }
    }
}
