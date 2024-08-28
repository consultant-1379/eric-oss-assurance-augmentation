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

import static com.ericsson.oss.air.util.ExceptionUtils.isDatabaseConnectivityException;
import static com.ericsson.oss.air.util.ExceptionUtils.isKafkaConnectivityException;
import static com.ericsson.oss.air.util.ExceptionUtils.isRestClientConnectivityException;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ericsson.oss.air.exception.AasDaoException;
import com.ericsson.oss.air.exception.AasValidationException;
import org.apache.kafka.clients.consumer.RetriableCommitFailedException;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.kafka.KafkaException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;

class ExceptionUtilsTest {

    @Test
    void isDatabaseConnectivityException_NullException_ReturnsFalse() {

        assertFalse(isDatabaseConnectivityException(null));

    }

    @Test
    void isDatabaseConnectivityException_AasDaoException_ReturnsTrue() {

        final DataAccessException nestedException = new DataAccessResourceFailureException(EXCEPTION_MSG);
        final AasDaoException exception = new AasDaoException(nestedException);

        assertTrue(isDatabaseConnectivityException(exception));

    }

    @Test
    void isDatabaseConnectivityException_AasDaoException_NonDBConnectivityCause_ReturnsFalse() {

        final Exception nestedException = new AasValidationException(EXCEPTION_MSG);
        final AasDaoException exception = new AasDaoException(nestedException);

        assertFalse(isDatabaseConnectivityException(exception));

    }

    @Test
    void isDatabaseConnectivityException_AasDaoException_NullCause_ReturnsFalse() {

        final AasDaoException exception = new AasDaoException(EXCEPTION_MSG);

        assertFalse(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_TransactionException_ReturnsTrue() {

        final SQLException nestedException = new SQLTransientConnectionException(EXCEPTION_MSG);
        final TransactionException exception = new CannotCreateTransactionException(EXCEPTION_MSG, nestedException);

        assertTrue(isDatabaseConnectivityException(exception));

    }

    @Test
    void isDatabaseConnectivityException_TransactionException_NonDBConnectivityCause_ReturnsFalse() {

        final Exception nestedException = new AasValidationException(EXCEPTION_MSG);
        final TransactionException exception = new CannotCreateTransactionException(EXCEPTION_MSG, nestedException);

        assertFalse(isDatabaseConnectivityException(exception));

    }

    @Test
    void isDatabaseConnectivityException_TransactionException_NullCause_ReturnsFalse() {

        final TransactionException exception = new CannotCreateTransactionException(EXCEPTION_MSG);

        assertFalse(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_TransactionSystemException_ReturnsTrue() {

        final DataAccessException nestedException = new DataAccessResourceFailureException(Strings.EMPTY);
        final TransactionSystemException exception = new TransactionSystemException(EXCEPTION_MSG);
        exception.initApplicationException(nestedException);

        assertTrue(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_TransactionSystemException_NonDBConnectivityCause_ReturnsFalse() {

        final Exception nestedException = new AasValidationException(Strings.EMPTY);
        final TransactionSystemException exception = new TransactionSystemException(EXCEPTION_MSG);
        exception.initApplicationException(nestedException);

        assertFalse(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_DataAccessResourceFailureException_ReturnsTrue() {

        final DataAccessException exception = new CannotGetJdbcConnectionException(EXCEPTION_MSG);

        assertTrue(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_SQLTransientConnectionException_ReturnsTrue() {

        final SQLException exception = new SQLTransientConnectionException(EXCEPTION_MSG);

        assertTrue(isDatabaseConnectivityException(exception));
    }

    @Test
    void isDatabaseConnectivityException_NonDBConnectivityCause_ReturnsFalse() {

        final Exception exception = new AasValidationException(EXCEPTION_MSG);

        assertFalse(isDatabaseConnectivityException(exception));
    }

    @Test
    void isRestClientConnectivityException_ReturnsTrue() {

        final List<Integer> statusValueList = IntStream.concat(IntStream.rangeClosed(400, 405), IntStream.rangeClosed(413, 415)).boxed()
                .collect(Collectors.toList());
        statusValueList.add(408);
        statusValueList.add(409);
        statusValueList.add(429);
        statusValueList.addAll(IntStream.rangeClosed(500, 505).boxed().toList());

        for (final int statusValue : statusValueList) {
            assertTrue(isRestClientConnectivityException(statusValue));
        }

    }

    @Test
    void isRestClientConnectivityException_ReturnsFalse() {
        assertFalse(isRestClientConnectivityException(418));
    }

    @Test
    void isKafkaConnectivityException_ReturnsTrue() {

        final Exception timeoutException = new TimeoutException();
        final Exception disconnectException = new DisconnectException();
        final Exception retriableCommitFailedException = new RetriableCommitFailedException(EXCEPTION_MSG);

        final List<Exception> exceptionList = List.of(new KafkaException(EXCEPTION_MSG, timeoutException),
                new KafkaException(EXCEPTION_MSG, disconnectException),
                new KafkaException(EXCEPTION_MSG, retriableCommitFailedException), timeoutException, disconnectException,
                retriableCommitFailedException);

        for (final Exception exception : exceptionList) {
            assertTrue(isKafkaConnectivityException(exception));
        }
    }

    @Test
    void isKafkaConnectivityException_NonConnectivityException_ReturnsFalse() {
        assertFalse(isKafkaConnectivityException(new AasValidationException(EXCEPTION_MSG)));
    }

    @Test
    void isKafkaConnectivityException_NullException_ReturnsFalse() {
        assertFalse(isKafkaConnectivityException(null));
    }

}