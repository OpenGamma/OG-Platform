/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender that passes events to the {@link LogBridge}.
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
    LogbackLogEvent logEvent = new LogbackLogEvent(event);
    _logBridge.log(logEvent);
  }

}
