/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import org.apache.log4j.Level;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender that passes messages to the {@link LogBridge}.
 * 
 * @param <E>  the type of log event
 */
public class LogbackBridgeAppender<E> extends UnsynchronizedAppenderBase<E> {

  private final LogBridge _logBridge = LogBridge.getInstance();
  
  @Override
  protected void append(E eventObject) {
    if (!isStarted() || !_logBridge.hasListeners()) {
      return;
    }
    if (!(eventObject instanceof ILoggingEvent)) {
      return;
    }
    ILoggingEvent event = (ILoggingEvent) eventObject;
    String message = event.getFormattedMessage();
    LogLevel level;
    switch (event.getLevel().toInt()) {
      case Level.FATAL_INT:
      case Level.ERROR_INT:
        level = LogLevel.ERROR;
        break;
      case Level.WARN_INT:
        level = LogLevel.WARN;
        break;
      case Level.INFO_INT:
        level = LogLevel.INFO;
        break;
      case Level.DEBUG_INT:
        level = LogLevel.DEBUG;
        break;
      case Level.TRACE_INT:
        level = LogLevel.TRACE;
        break;
      default:
        level = LogLevel.WARN;
        message = "[" + event.getLevel().toString() + "] " + message;
        break;
    }
    _logBridge.log(level, message);
  }

}
