/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.view.calcnode.EmptyExecutionLog;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;

/**
 * Provides access to the log events which occurred during some execution.
 * <p>
 * These are summarised as error, warning and information indicators, although the full list of events may contain any
 * level of log event.
 */
@PublicAPI
public interface ExecutionLog {
  
  /**
   * An empty execution log.
   */
  ExecutionLog EMPTY = EmptyExecutionLog.INSTANCE;

  /**
   * The levels of log events in this log, not null.
   *
   * @return The levels of log events in this log, not null.
   */
  EnumSet<LogLevel> getLogLevels();

  /**
   * Gets the log events, if these have been collected.
   *  
   * @return an ordered list of log events, null if not collected
   */
  List<LogEvent> getEvents();
  
  //-------------------------------------------------------------------------
  /**
   * Indicates whether an exception occurred that prevented the execution from completing.
   *  
   * @return true if any exception details are available, false otherwise
   */
  boolean hasException();

  /**
   * Gets the class of the exception that occurred which prevented the execution from completing, if applicable.
   * 
   * @return the class of the exception, null if not applicable.
   */
  String getExceptionClass();
  
  /**
   * Gets the message from the exception that occurred which prevented the execution from completing, if applicable.
   * 
   * @return the message from the exception, null if not applicable.
   */
  String getExceptionMessage();
  
  /**
   * Gets the stack trace of the exception that occurred which prevented the execution from completing, if applicable.
   * 
   * @return the stack trace of the exception, null if not applicable.
   */
  String getExceptionStackTrace();
  
}
