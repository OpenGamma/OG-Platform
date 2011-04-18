/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Represents a call to {@link ViewResultListener#cycleExecutionFailed(com.opengamma.engine.view.execution.ViewCycleExecutionOptions, Exception)}.
 */
public class CycleExecutionFailedCall implements Function<ViewResultListener, Object> {

  private final ViewCycleExecutionOptions _executionOptions;
  private final Exception _exception;
  
  public CycleExecutionFailedCall(ViewCycleExecutionOptions executionOptions, Exception exception) {
    _executionOptions = executionOptions;
    _exception = exception;
  }

  public ViewCycleExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  public Exception getException() {
    return _exception;
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleExecutionFailed(getExecutionOptions(), getException());
    return null;
  }

}
