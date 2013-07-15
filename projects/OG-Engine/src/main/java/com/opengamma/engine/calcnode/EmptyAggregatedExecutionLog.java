/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.util.log.LogLevel;

/**
 * Represents an empty execution log, mainly for testing.
 */
public final class EmptyAggregatedExecutionLog implements AggregatedExecutionLog {

  /**
   * An instance of an empty execution log.
   */
  public static final AggregatedExecutionLog INSTANCE = new EmptyAggregatedExecutionLog();

  private final EnumSet<LogLevel> _levels = EnumSet.noneOf(LogLevel.class);

  /**
   * Hidden constructor.
   */
  private EmptyAggregatedExecutionLog() {
  }
  
  //-------------------------------------------------------------------------
  @Override
  public EnumSet<LogLevel> getLogLevels() {
    return _levels;
  }
  
  public ExecutionLogWithContext getRootLog() {
    return null;
  }

  @Override
  public List<ExecutionLogWithContext> getLogs() {
    return null;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof EmptyAggregatedExecutionLog;
  }

  @Override
  public String toString() {
    return "AggregatedExecutionLog[]";
  }
  
}
