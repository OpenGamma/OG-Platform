/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogEventListener;
import com.opengamma.util.log.ThreadLocalLogEventListener;

/**
 * Provides log listening functionality for a calculation node.
 */
public class CalculationNodeLogEventListener implements LogEventListener {

  private final ThreadLocalLogEventListener _threadLocalListener;
  private MutableExecutionLog _log;

  public CalculationNodeLogEventListener(ThreadLocalLogEventListener threadLocalListener) {
    ArgumentChecker.notNull(threadLocalListener, "threadLocalListener");
    _threadLocalListener = threadLocalListener;
  }

  //-------------------------------------------------------------------------
  /**
   * Attaches an execution log to the calling thread's log output.
   * 
   * @param log the execution log, not null
   */
  public void attach(MutableExecutionLog log) {
    ArgumentChecker.notNull(log, "log");
    if (_log != null) {
      // Clear the flag to try and recover if the "detach" never happened. Worst case is
      // we'll see another exception thrown when the original caller attempts to detach.
      _log = null;
      throw new IllegalStateException("Another log is already attached to the listener");
    }
    _log = log;
    _threadLocalListener.setThreadLocalListener(this);
  }

  /**
   * Detaches the existing execution log from the calling thread's log output.
   */
  public void detach() {
    if (_log == null) {
      throw new IllegalStateException("No log to detach");
    }
    _threadLocalListener.removeThreadLocalListener();
    _log = null;
  }

  //-------------------------------------------------------------------------
  @Override
  public void log(LogEvent event) {
    _log.add(event);
  }

}
