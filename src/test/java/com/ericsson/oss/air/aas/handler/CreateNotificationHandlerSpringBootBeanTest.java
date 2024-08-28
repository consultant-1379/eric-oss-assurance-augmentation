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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ericsson.oss.air.aas.repository.impl.inmemorydb.ArdqRegistrationDaoImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {ArdqRegistrationDaoImpl.class, CreateNotificationHandler.class})
@MockBean({AugmentationWorkflowHandler.class})
@ActiveProfiles("test")
class CreateNotificationHandlerSpringBootBeanTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void autoWireWorksForParentsDependencies() {
        // A simple test to make sure NotificationHandler's dependencies are wired in correctly
        assertDoesNotThrow(() -> this.applicationContext.getBean(CreateNotificationHandler.class));
        assertNotNull(this.applicationContext.getBean(CreateNotificationHandler.class).ardqRegistrationDao);
    }

}