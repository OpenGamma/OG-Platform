/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;

/**
 * Allows log events which occur during function execution to be collected.
 */
public class MutableExecutionLog implements ExecutionLog {

  private final ExecutionLogMode _logMode;
  private final EnumSet<LogLevel> _levels = EnumSet.noneOf(LogLevel.class);
  private final List<LogEvent> _events;
  
  private String _exceptionClass;
  private String _exceptionMessage;
  private String _exceptionStackTrace;
  
  public MutableExecutionLog(ExecutionLogMode logMode) {
    ArgumentChecker.notNull(logMode, "logMode");
    _events = logMode == ExecutionLogMode.FULL ? new LinkedList<LogEvent>() : null;
    _logMode = logMode;
  }
  
  public static ExecutionLog single(LogEvent logEvent, ExecutionLogMode logMode) {
    MutableExecutionLog log = new MutableExecutionLog(logMode);
    log.add(logEvent);
    return log;
  }

  //-------------------------------------------------------------------------
  public void add(LogEvent logEvent) {
    ArgumentChecker.notNull(logEvent, "logEvent");
    LogLevel level = logEvent.getLevel();
    _levels.add(level);
    if (_logMode == ExecutionLogMode.FULL) {
      _events.add(new SimpleLogEvent(level, logEvent.getMessage()));
    }
  }
  
  public void setException(Throwable exception) {
    _exceptionClass = exception.getClass().getName();
    _exceptionMessage = exception.getMessage();
    final StringBuffer buffer = new StringBuffer();
    for (StackTraceElement element : exception.getStackTrace()) {
      buffer.append(element.toString() + "\n");
    }
    _exceptionStackTrace = buffer.toString();
  }
  
  public void setException(String exceptionClass, String exceptionMessage) {
    ArgumentChecker.notNull(exceptionClass, "exceptionClass");
    _exceptionClass = exceptionClass;
    _exceptionMessage = exceptionMessage;
    _exceptionStackTrace = null;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean hasError() {
    return _levels.contains(LogLevel.ERROR);
  }

  @Override
  public boolean hasWarn() {
    return _levels.contains(LogLevel.WARN);
  }

  @Override
  public boolean hasInfo() {
    return _levels.contains(LogLevel.INFO);
  }

  @Override
  public List<LogEvent> getEvents() {
    return _events;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean hasException() {
    return getExceptionClass() != null;
  }

  @Override
  public String getExceptionClass() {
    return _exceptionClass;
  }

  @Override
  public String getExceptionMessage() {
    return _exceptionMessage;
  }

  @Override
  public String getExceptionStackTrace() {
    return _exceptionStackTrace;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_events == null) ? 0 : _events.hashCode());
    result = prime * result + ((_exceptionClass == null) ? 0 : _exceptionClass.hashCode());
    result = prime * result + ((_exceptionMessage == null) ? 0 : _exceptionMessage.hashCode());
    result = prime * result + ((_exceptionStackTrace == null) ? 0 : _exceptionStackTrace.hashCode());
    result = prime * result + ((_levels == null) ? 0 : _levels.hashCode());
    result = prime * result + ((_logMode == null) ? 0 : _logMode.hashCode());
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
    if (!(obj instanceof MutableExecutionLog)) {
      return false;
    }
    MutableExecutionLog other = (MutableExecutionLog) obj;
    return ObjectUtils.equals(_events, other._events)
        && ObjectUtils.equals(_exceptionClass, other._exceptionClass)
        && ObjectUtils.equals(_exceptionMessage, other._exceptionMessage)
        && ObjectUtils.equals(_exceptionStackTrace, other._exceptionStackTrace)
        && ObjectUtils.equals(_levels, other._levels)
        && ObjectUtils.equals(_logMode, other._logMode);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
    style.appendStart(sb, this);
    style.append(sb, "error", hasError(), null);
    style.append(sb, "warn", hasWarn(), null);
    style.append(sb, "info", hasInfo(), null);
    if (hasException()) {
      style.append(sb, "exception", getExceptionClass(), null);
    }
    style.appendEnd(sb, this);
    return sb.toString();
  }
  
}
