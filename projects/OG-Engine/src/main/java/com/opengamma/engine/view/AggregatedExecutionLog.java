/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.calcnode.EmptyAggregatedExecutionLog;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.log.LogLevel;

/**
 * Aggregates the logs which affect an execution with a top-level overview.
 * <p>
 * A computed value may require inputs which have been computed separately and which have their own execution logs. The
 * aggregated execution log combines the logs from an execution with the logs from all of its inputs.
 */
@PublicAPI
public interface AggregatedExecutionLog {

  /**
   * An empty execution log.
   */
  AggregatedExecutionLog EMPTY = EmptyAggregatedExecutionLog.INSTANCE;

  //-------------------------------------------------------------------------
  /**
   * Gets an overview of the levels of log event across all execution logs included in the aggregate.
   * 
   * @return the levels of log event, not null.
   */
  EnumSet<LogLevel> getLogLevels();
  
  /**
   * Gets the log from the root-level execution.
   * 
   * @return the log from the root-level execution, null if not available
   */
  ExecutionLogWithContext getRootLog();
  
  /**
   * Gets all the execution logs included in the aggregate.
   * <p>
   * The logs are ordered appropriately, starting with the root-level execution log.
   * 
   * @return the execution logs, null if not available
   */
  List<ExecutionLogWithContext> getLogs();
  
}
