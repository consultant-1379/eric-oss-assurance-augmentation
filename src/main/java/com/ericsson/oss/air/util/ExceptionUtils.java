/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import java.sql.SQLTransientConnectionException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ericsson.oss.air.exception.AasDaoException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.RetriableCommitFailedException;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;

/**
 * Utility methods to filter exceptions into categories
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

    /**
     * Returns true if the exception is classified as a database connectivity exception. Otherwise, returns false.
     *
     * @param throwable the exception to test for a database connectivity error
     * @return true if the exception is classified as a database connectivity exception
     */
    public static boolean isDatabaseConnectivityException(final Throwable throwable) {

        Throwable nonNestedException = throwable;

        if (throwable instanceof TransactionSystemException) {
            nonNestedException = ((TransactionSystemException) throwable).getApplicationException();
        } else if ((throwable instanceof AasDaoException || throwable instanceof TransactionException) && Objects.nonNull(throwable.getCause())) {
            nonNestedException = throwable.getCause();
        }

        return nonNestedException instanceof DataAccessResourceFailureException
                || nonNestedException instanceof SQLTransientConnectionException;

    }

    /**
     * Returns true if the exception is classified as a REST client connectivity exception. Otherwise, returns false.
     *
     * @param value the status code value to test for a REST client connectivity error
     * @return true if the exception is classified as a REST client connectivity exception
     */
    public static boolean isRestClientConnectivityException(final int value) {

        final Set<Integer> statusCodes = IntStream.of(400, 401, 402, 403, 404, 405, 408, 409, 413, 414, 415, 429, 500, 501, 502, 503, 504, 505)
                .boxed().collect(Collectors.toSet());

        return statusCodes.contains(value);
    }

    /**
     * Returns true if the exception is classified as a Kafka connectivity exception. Otherwise, returns false.
     *
     * @param exception the exception to test for a Kafka connectivity error
     * @return true if the exception is classified as a Kafka connectivity exception
     */
    public static boolean isKafkaConnectivityException(final Exception exception) {

        if (Objects.isNull(exception)) {
            return false;
        }

        final Set<Class<? extends RetriableException>> exceptionClasses = Set.of(DisconnectException.class, RetriableCommitFailedException.class,
                TimeoutException.class);

        final Throwable exceptionCause = exception.getCause();

        return exceptionClasses.contains(exception.getClass()) || (Objects.nonNull(exceptionCause) && exceptionClasses.contains(
                exceptionCause.getClass()));
    }

}
