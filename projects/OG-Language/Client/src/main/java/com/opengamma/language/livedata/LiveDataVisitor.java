/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.connector.LiveData;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Defines a visitor interface for incoming live-data messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface LiveDataVisitor<T1, T2> {

  T1 visitConnect(Connect message, T2 data) throws AsynchronousExecution;
  
  T1 visitCustom(Custom message, T2 data) throws AsynchronousExecution;
  
  T1 visitDisconnect(Disconnect message, T2 data) throws AsynchronousExecution;

  T1 visitQueryAvailable(QueryAvailable message, T2 data) throws AsynchronousExecution;

  T1 visitQueryValue(QueryValue message, T2 data) throws AsynchronousExecution;

  T1 visitUnexpected(LiveData message, T2 data) throws AsynchronousExecution;

}
