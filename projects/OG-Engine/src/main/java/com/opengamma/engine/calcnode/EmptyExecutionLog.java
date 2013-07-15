/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;

/**
 * Represents an empty execution log, mainly for testing.
 */
public final class EmptyExecutionLog implements ExecutionLog {

  /**
   * An instance of an empty execution log.
   */
  public static final ExecutionLog INSTANCE = new EmptyExecutionLog();

  private final EnumSet<LogLevel> _levels = EnumSet.noneOf(LogLevel.class);

  /**
   * Hidden constructor.
   */
  private EmptyExecutionLog() {
  }

  @Override
  public EnumSet<LogLevel> getLogLevels() {
    return _levels;
  }

  @Override
  public List<LogEvent> getEvents() {
    return null;
  }

  @Override
  public boolean hasException() {
    return false;
  }

  @Override
  public String getExceptionClass() {
    return null;
  }

  @Override
  public String getExceptionMessage() {
    return null;
  }

  @Override
  public String getExceptionStackTrace() {
    return null;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isEmpty() {
    return true;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof EmptyExecutionLog;
  }

  @Override
  public String toString() {
    return "ExecutionLog[]";
  }
  
}
