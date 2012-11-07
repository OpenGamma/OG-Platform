/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogEventListener;
import com.opengamma.util.log.ThreadLocalLogEventListener;

/**
 * Collects and processes log messages which occur during function execution.
 * <p>
 * An instance should be used only by the thread for which logs are to be collected.
 */
public class ExecutionLogCollector implements LogEventListener {

  private final ThreadLocalLogEventListener _threadLocalLogEventListener;
  private ExecutionLogMode _logMode;
  
  /**
   * Constructs an instance.
   * 
   * @param logListener  the log listener, not null
   */
  public ExecutionLogCollector(ThreadLocalLogEventListener logListener) {
    ArgumentChecker.notNull(logListener, "logListener");
    _threadLocalLogEventListener = logListener;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Starts log collection for the calling thread.
   * 
   * @param logMode  the log mode, not null
   */
  public void start(ExecutionLogMode logMode) {
    ArgumentChecker.notNull(logMode, "logMode");
    _logMode = logMode;
    _threadLocalLogEventListener.setThreadLocalListener(this);
  }
  
  /**
   * Stops log collection for the calling thread, returning the logs collected.
   * 
   * @throws IllegalStateException  if log collection has not been started for the calling thread
   */
  public void stop() {
    _threadLocalLogEventListener.removeThreadLocalListener();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void log(LogEvent event) {
  }

}
