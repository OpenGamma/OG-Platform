/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogLevel;

/**
 * Simple implementation of {@link AggregatedExecutionLog}.
 */
public class SimpleAggregatedExecutionLog implements AggregatedExecutionLog {

  private final EnumSet<LogLevel> _logLevels;
  private final List<ExecutionLogWithContext> _logs;
  
  /**
   * Constructs an instance for a root level with possible logs from its dependencies. 
   * 
   * @param rootLog  the root log, not null
   * @param dependentLogs  the dependent logs, if any, may be null or empty
   * @param logMode  the mode describing the outputs required, not null
   */
  public SimpleAggregatedExecutionLog(ExecutionLogWithContext rootLog, List<AggregatedExecutionLog> dependentLogs, ExecutionLogMode logMode) {
    ArgumentChecker.notNull(rootLog, "rootLog");
    ArgumentChecker.notNull(logMode, "logMode");
    _logLevels = EnumSet.copyOf(rootLog.getExecutionLog().getLogLevels());
    if (logMode == ExecutionLogMode.FULL) {
      _logs = new ArrayList<ExecutionLogWithContext>();
      _logs.add(rootLog);
    } else {
      _logs = null;
    }
    if (dependentLogs != null) {
      for (AggregatedExecutionLog dependentLog : dependentLogs) {
        _logLevels.addAll(dependentLog.getLogLevels());
        if (_logs != null && dependentLog.getLogs() != null) {
          _logs.addAll(dependentLog.getLogs());
        }
      }
    }
  }
  
  /**
   * Constructs an instance from the internal fields.
   * <p>
   * Intended for deserialisation. Performs no consistency checking of the inputs.
   * 
   * @param logLevels  an overview of the log levels, not null
   * @param logs  the individual logs, null if not available
   */
  public SimpleAggregatedExecutionLog(EnumSet<LogLevel> logLevels, List<ExecutionLogWithContext> logs) {
    _logLevels = logLevels;
    _logs = logs;
  }
  
  @Override
  public EnumSet<LogLevel> getLogLevels() {
    return _logLevels;
  }
  
  @Override
  public ExecutionLogWithContext getRootLog() {
    return _logs != null && _logs.size() > 0 ? _logs.get(0) : null;
  }

  @Override
  public List<ExecutionLogWithContext> getLogs() {
    return _logs;
  }

}
