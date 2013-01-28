/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import org.threeten.bp.Instant;

import com.google.common.base.Function;

/**
 * Represents a call to {@link ViewResultListener#viewDefinitionCompilationFailed(javax.time.Instant, Exception)}.
 */
public class ViewDefinitionCompilationFailedCall implements Function<ViewResultListener, Object> {

  private final Instant _valuationTime;
  private final Exception _exception;
  
  public ViewDefinitionCompilationFailedCall(Instant valuationTime, Exception exception) {
    _valuationTime = valuationTime;
    _exception = exception;
  }
  
  public Instant getValuationTime() {
    return _valuationTime;
  }
  
  public Exception getException() {
    return _exception;
  }
  
  @Override
  public Object apply(ViewResultListener listener) {
    listener.viewDefinitionCompilationFailed(getValuationTime(), getException());
    return null;
  }

}
