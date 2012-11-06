/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

/**
 * Receives log events.
 */
public interface LogEventListener {
  
  /**
   * Called when a log event is received.
   * 
   * @param event  the event, not null
   */
  void log(LogEvent event);

}
