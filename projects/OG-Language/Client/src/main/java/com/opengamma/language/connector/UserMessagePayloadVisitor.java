/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import com.opengamma.util.async.AsynchronousExecution;

/**
 * Defines a visitor interface for the incoming user messages. Further visitor interfaces may be
 * defined around these top-level messages to allow additional message-specific processing.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface UserMessagePayloadVisitor<T1, T2> {

  T1 visitUserMessagePayload(UserMessagePayload payload, T2 data) throws AsynchronousExecution;

  T1 visitTest(Test message, T2 data) throws AsynchronousExecution;

  T1 visitLiveData(LiveData message, T2 data) throws AsynchronousExecution;

  T1 visitFunction(Function message, T2 data) throws AsynchronousExecution;

  T1 visitProcedure(Procedure message, T2 data) throws AsynchronousExecution;

  T1 visitCustom(Custom message, T2 data) throws AsynchronousExecution;

}
