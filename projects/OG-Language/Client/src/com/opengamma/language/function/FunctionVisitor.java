/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.connector.Function;

/**
 * Defines a visitor interface for incoming function messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface FunctionVisitor<T1, T2> {

  T1 visitCustom(Custom message, T2 data);

  T1 visitQueryAvailable(QueryAvailable message, T2 data);

  T1 visitUnexpected(Function message, T2 data);

}
