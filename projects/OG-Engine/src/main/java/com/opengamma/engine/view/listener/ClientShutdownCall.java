/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;

/**
 * Represents a call to {@link ViewResultListener#clientShutdown(Exception)}
 */
public class ClientShutdownCall implements Function<ViewResultListener, Object> {

  private final Exception _exception;
  
  public ClientShutdownCall(Exception exception) {
    _exception = exception;
  }
  
  public Exception getException() {
    return _exception;
  }
  
  @Override
  public Object apply(ViewResultListener listener) {
    listener.clientShutdown(getException());
    return null;
  }

}
