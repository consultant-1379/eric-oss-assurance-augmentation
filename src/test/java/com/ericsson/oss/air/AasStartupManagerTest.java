/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.FlywaySchemaMigration;
import com.ericsson.oss.air.aas.config.kafka.APKafkaConfiguration;
import com.ericsson.oss.air.aas.handler.CreateNotificationHandler;
import com.ericsson.oss.air.aas.handler.registration.AugmentSchemaHandler;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AasStartupManagerTest {

    private static final String ARDQ_REGISTRATION_ID = "cardq";

    private static final String ARDQ_REGISTRATION_URL = "http://eric-oss-cardq:8080";

    private static final String INPUT_SCHEMA_REFERENCE = "something_something_something";

    @Mock
    private CreateNotificationHandler notificationHandler;

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    @Mock
    private KafkaAdminService adminService;

    @Mock
    private APKafkaConfiguration apKafkaConfiguration;

    @Mock
    private FlywaySchemaMigration flywaySchemaMigration;

    @Mock
    private ConfigurationNotificationService configurationNotificationService;

    @Mock
    private AugmentSchemaHandler augmentSchemaHandler;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private AasStartupManager aasStartupManager;

    @Test
    void init_foundArdqRegistrationInDB() {
        when(ardqRegistrationDao.getAllArdqRegistrations()).thenReturn(ardqRegistrationDtoList(getDummyArdqRegistrationDto()));

        this.aasStartupManager.init();

        verify(this.configurationNotificationService, times(1)).startKafkaListener();
        verify(this.notificationHandler, times(1)).apply(any(ArdqRegistrationDto.class));
        verify(this.flywaySchemaMigration, times(1)).migrate();
    }

    @Test
    void init_notFoundArdqRegistrationInDB() {
        when(ardqRegistrationDao.getAllArdqRegistrations()).thenReturn(Optional.empty());

        this.aasStartupManager.init();

        verify(this.configurationNotificationService, times(1)).startKafkaListener();
        verify(this.notificationHandler, times(0)).apply(any(ArdqRegistrationDto.class));
        verify(this.flywaySchemaMigration, times(1)).migrate();
    }

    @Test
    void init_flywayException() {
        doThrow(new FlywayException()).when(this.flywaySchemaMigration).migrate();

        assertThrows(FlywayException.class, () -> this.aasStartupManager.init());
    }

    @Test
    void init_argumentationWorkflowException() {

        when(this.ardqRegistrationDao.getAllArdqRegistrations()).thenReturn(ardqRegistrationDtoList(getDummyArdqRegistrationDto()));
        doThrow(new RuntimeException()).when(this.augmentSchemaHandler).resync(any());

        this.aasStartupManager.init();

        verify(this.flywaySchemaMigration, times(1)).migrate();
        verify(this.faultHandler, times(1)).error(any(), any());
        verify(this.configurationNotificationService, times(1)).startKafkaListener();
    }

    private static ArdqAugmentationFieldDto getDummyArdqAugmentationField() {
        return new ArdqAugmentationFieldDto().output("nsi").addInputItem("snssai").addInputItem("moFDN");
    }

    private static ArdqAugmentationRuleDto getDummyArdqAugmentationRule() {
        return new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE).addFieldsItem(getDummyArdqAugmentationField());
    }

    private static ArdqRegistrationDto getDummyArdqRegistrationDto() {
        return new ArdqRegistrationDto().ardqId(ARDQ_REGISTRATION_ID).ardqUrl(ARDQ_REGISTRATION_URL).addRulesItem(getDummyArdqAugmentationRule());
    }

    private static Optional<List<ArdqRegistrationDto>> ardqRegistrationDtoList(ArdqRegistrationDto ardqRegistrationDto) {
        return Optional.of(List.of(ardqRegistrationDto));
    }
}