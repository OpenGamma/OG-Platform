/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewProcessErrorType;
import com.opengamma.engine.view.ViewProcessListener;

/**
 * Represents a call to {@link ViewProcessListener#error(ViewProcessErrorType, String, Exception)}.
 */
public class ErrorCall implements Function<ViewProcessListener, Object> {

  private final ViewProcessErrorType _errorType;
  private final String _details;
  private final Exception _exception;
  
  public ErrorCall(ViewProcessErrorType errorType, String details, Exception exception) {
    _errorType = errorType;
    _details = details;
    _exception = exception;
  }

  @Override
  public Object apply(ViewProcessListener listener) {
    listener.error(_errorType, _details, _exception);
    return null;
  }
  
}
