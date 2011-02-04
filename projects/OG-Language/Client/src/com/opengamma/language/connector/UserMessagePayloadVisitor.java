/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

/**
 * Defines a visitor interface for the incoming user messages. Further visitor interfaces may be
 * defined around these top-level messages to allow additional message-specific processing.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface UserMessagePayloadVisitor<T1, T2> {

  T1 visitUserMessagePayload(final UserMessagePayload payload, final T2 data);

  T1 visitTest(final Test message, final T2 data);

  // TODO: LiveData

  // TODO: Function

  // TODO: Procedure

}
