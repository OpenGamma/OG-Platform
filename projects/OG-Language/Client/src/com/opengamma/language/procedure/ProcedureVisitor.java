/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import com.opengamma.language.connector.Procedure;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Defines a visitor interface for incoming procedure messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface ProcedureVisitor<T1, T2> {

  T1 visitCustom(Custom message, T2 data) throws AsynchronousExecution;

  T1 visitInvoke(Invoke message, T2 data) throws AsynchronousExecution;

  T1 visitQueryAvailable(QueryAvailable message, T2 data) throws AsynchronousExecution;

  T1 visitUnexpected(Procedure message, T2 data) throws AsynchronousExecution;

}
