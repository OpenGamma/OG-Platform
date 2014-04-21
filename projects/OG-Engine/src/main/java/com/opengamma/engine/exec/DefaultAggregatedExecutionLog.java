/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogLevel;

/**
 * Default implementation of {@link AggregatedExecutionLog}.
 * <p>
 * In {@link ExecutionLogMode#INDICATORS} mode, the root log and dependent logs are inspected to obtain an aggregate of the log levels, but no individual logs are stored. In
 * {@link ExecutionLogMode#FULL} mode, individual non-empty logs are also stored.
 */
public final class DefaultAggregatedExecutionLog implements AggregatedExecutionLog {

  private static volatile Map<EnumSet<LogLevel>, DefaultAggregatedExecutionLog> s_indicatorInstances = Collections.emptyMap();

  private final EnumSet<LogLevel> _logLevels;
  private final List<ExecutionLogWithContext> _logs;
  /**
   * Flag to indicate whether the execution log at the root level was empty and has been excluded.
   */
  private final boolean _emptyRoot;

  /**
   * Constructs an instance for a root level with possible logs from its dependencies when the full logging mode is being used.
   * 
   * @param functionName the name of the function, not null
   * @param target the computation target specification, not null
   * @param rootLog the root log, not null
   * @param dependentLogs the dependent logs, if any, may be null or empty
   * @return the log instance
   */
  public static DefaultAggregatedExecutionLog fullLogMode(String functionName, ComputationTargetSpecification target, ExecutionLog rootLog, Collection<AggregatedExecutionLog> dependentLogs) {
    ArgumentChecker.notNull(functionName, "functionName");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(rootLog, "rootLog");
    EnumSet<LogLevel> logLevels = rootLog.getLogLevels();
    boolean logLevelsCopied = false;
    final List<ExecutionLogWithContext> logs = new ArrayList<ExecutionLogWithContext>();
    boolean emptyRoot = rootLog.isEmpty();
    if (!emptyRoot) {
      logs.add(ExecutionLogWithContext.of(functionName, target, rootLog));
    }
    if (dependentLogs != null) {
      for (AggregatedExecutionLog dependentLog : dependentLogs) {
        final EnumSet<LogLevel> dependentLogLevels = dependentLog.getLogLevels();
        if (logLevelsCopied) {
          logLevels.addAll(dependentLogLevels);
        } else {
          if (!logLevels.equals(dependentLogLevels)) {
            logLevels = EnumSet.copyOf(logLevels);
            logLevels.addAll(dependentLogLevels);
            logLevelsCopied = true;
          }
        }
        if (dependentLog.getLogs() != null) {
          logs.addAll(dependentLog.getLogs());
        }
      }
    }
    return new DefaultAggregatedExecutionLog(logLevels, logs, emptyRoot);
  }

  /**
   * Constructs an instance for a root level with possible logs from its dependencies when the indicator-only logging mode is being used.
   * 
   * @param rootLogLevels the root log indicators, not null
   * @return the log instance
   */
  public static DefaultAggregatedExecutionLog indicatorLogMode(EnumSet<LogLevel> rootLogLevels) {
    Map<EnumSet<LogLevel>, DefaultAggregatedExecutionLog> indicators = s_indicatorInstances;
    final DefaultAggregatedExecutionLog existing = indicators.get(rootLogLevels);
    if (existing != null) {
      return existing;
    }
    final DefaultAggregatedExecutionLog log = new DefaultAggregatedExecutionLog(rootLogLevels, null, false);
    indicators = new HashMap<EnumSet<LogLevel>, DefaultAggregatedExecutionLog>(indicators);
    indicators.put(rootLogLevels, log);
    s_indicatorInstances = indicators;
    return log;

  }

  /**
   * Constructs an instance from the internal fields.
   * <p>
   * Public for deserialization only. Performs no consistency checking of the inputs.
   * 
   * @param logLevels an overview of the log levels, not null
   * @param logs the individual logs, null if not available
   * @param emptyRoot true if the root log was empty, false otherwise
   */
  public DefaultAggregatedExecutionLog(EnumSet<LogLevel> logLevels, List<ExecutionLogWithContext> logs, boolean emptyRoot) {
    _logLevels = logLevels;
    _logs = logs;
    _emptyRoot = emptyRoot;
  }

  @Override
  public EnumSet<LogLevel> getLogLevels() {
    return _logLevels;
  }

  @Override
  public ExecutionLogWithContext getRootLog() {
    return _logs != null && !_emptyRoot ? _logs.get(0) : null;
  }

  @Override
  public List<ExecutionLogWithContext> getLogs() {
    return _logs;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _logLevels.hashCode();
    result = prime * result + (_emptyRoot ? 1231 : 1237);
    result = prime * result + ((_logs == null) ? 0 : _logs.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DefaultAggregatedExecutionLog)) {
      return false;
    }
    // Optimise for the majority of cases where no logs have been collected.
    // If logs are present then don't perform detailed equality checking, just check instance equality.
    DefaultAggregatedExecutionLog other = (DefaultAggregatedExecutionLog) obj;
    return ObjectUtils.equals(_logLevels, other._logLevels) && ObjectUtils.equals(_logs, other._logs) && _emptyRoot == other._emptyRoot;
  }

  //-------------------------------------------------------------------------  
  @Override
  public String toString() {
    StrBuilder sb = new StrBuilder()
        .append("AggLog[");
    if (!getLogLevels().isEmpty()) {
      sb.append("aggLevels=").append(getLogLevels());
      if (getLogs() != null) {
        sb.append(", logs=").append(getLogs());
      }
    }
    sb.append(']');
    return sb.toString();
  }
}
