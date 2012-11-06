/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

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

}
