/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka;

import org.springframework.context.ApplicationEvent;

/**
 * This customized event class extends the Spring ApplicationEvent {@link ApplicationEvent} class. It represents
 * the event of successful set-up of AAS' Kafka configuration.
 */
public class KafkaConfiguredEvent extends ApplicationEvent {

    private static final long serialVersionUID = -35083998740091627L;

    /**
     * Creates a {@code KafkaConfiguredEvent}.
     *
     * @param source  the object that is raising the event
     */
    public KafkaConfiguredEvent(final Object source) {
        super(source);
    }
}
