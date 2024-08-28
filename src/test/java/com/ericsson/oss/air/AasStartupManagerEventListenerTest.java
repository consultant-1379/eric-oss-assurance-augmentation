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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.aas.config.kafka.APKafkaConfiguration;
import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;
import com.ericsson.oss.air.aas.handler.CreateNotificationHandler;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { CreateNotificationHandler.class, ArdqRegistrationDao.class, KafkaAdminService.class, APKafkaConfiguration.class,
        Flyway.class, ConfigurationNotificationService.class, AasStartupManager.class })
@ActiveProfiles("test")
class AasStartupManagerEventListenerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockBean
    private CreateNotificationHandler notificationHandler;

    @MockBean
    private ArdqRegistrationDao ardqRegistrationDao;

    @MockBean
    private KafkaAdminService adminService;

    @MockBean
    private APKafkaConfiguration apKafkaConfiguration;

    @MockBean
    private Flyway flyway;

    @MockBean
    private ConfigurationNotificationService configurationNotificationService;

    @MockBean
    private AasStartupManager aasStartupManager;

    @Test
    void init_eventListener() {

        this.publisher.publishEvent(new KafkaConfiguredEvent(this));

        verify(this.aasStartupManager, times(1)).init();

    }

}