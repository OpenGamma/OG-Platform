/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

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
  private final EnumSet<LogLevel> _logLevels = EnumSet.noneOf(LogLevel.class);
  private final List<LogEvent> _events;

  private String _exceptionClass;
  private String _exceptionMessage;
  private String _exceptionStackTrace;

  public MutableExecutionLog(ExecutionLogMode logMode) {
    ArgumentChecker.notNull(logMode, "logMode");
    _events = logMode == ExecutionLogMode.FULL ? new LinkedList<LogEvent>() : null;
    _logMode = logMode;
  }

  public MutableExecutionLog(ExecutionLog copyFrom) {
    _logMode = ExecutionLogMode.FULL;
    _logLevels.addAll(copyFrom.getLogLevels());
    if (copyFrom.getEvents() != null) {
      _events = new LinkedList<LogEvent>(copyFrom.getEvents());
    } else {
      _events = new LinkedList<LogEvent>();
    }
    _exceptionClass = copyFrom.getExceptionClass();
    _exceptionMessage = copyFrom.getExceptionMessage();
    _exceptionStackTrace = copyFrom.getExceptionStackTrace();
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
    _logLevels.add(level);
    if (_logMode == ExecutionLogMode.FULL) {
      _events.add(new SimpleLogEvent(level, logEvent.getMessage()));
    }
  }

  public void setException(Throwable exception) {
    _exceptionClass = exception.getClass().getName();
    _exceptionMessage = exception.getMessage();
    final StringBuilder buffer = new StringBuilder();
    for (StackTraceElement element : exception.getStackTrace()) {
      buffer.append(element.toString()).append("\n");
    }
    _exceptionStackTrace = buffer.toString();
    _logLevels.add(LogLevel.WARN);
  }

  public void setException(String exceptionClass, String exceptionMessage) {
    ArgumentChecker.notNull(exceptionClass, "exceptionClass");
    _exceptionClass = exceptionClass;
    _exceptionMessage = exceptionMessage;
    _exceptionStackTrace = null;
    _logLevels.add(LogLevel.WARN);
  }

  //-------------------------------------------------------------------------

  @Override
  public EnumSet<LogLevel> getLogLevels() {
    return EnumSet.copyOf(_logLevels);
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
  public boolean isEmpty() {
    return _logLevels.isEmpty() && !hasException();
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
    result = prime * result + ((_logLevels == null) ? 0 : _logLevels.hashCode());
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
        && ObjectUtils.equals(_logLevels, other._logLevels)
        && ObjectUtils.equals(_logMode, other._logMode);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
    style.appendStart(sb, this);
    if (hasException()) {
      style.append(sb, "exception", getExceptionClass(), null);
    }
    if (!_logLevels.isEmpty()) {
      style.append(sb, "logLevels", _logLevels, null);
    }
    style.appendEnd(sb, this);
    return sb.toString();
  }

}
