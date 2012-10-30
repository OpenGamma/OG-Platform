/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

/**
 * Receives log messages from the {@link LogBridge}.
 */
public interface LogBridgeListener {
  
  /**
   * Called when a log message is received by the bridge.
   * 
   * @param level  the logging level
   * @param message  the log message
   */
  void logReceived(LogLevel level, String message);

}
