/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.PublicAPI;

/**
 * Used to indicate that a user does not have a necessary permission.
 */
@PublicAPI
public class ViewPermissionException extends OpenGammaRuntimeException {
  
  private static final long serialVersionUID = 1L;

  public ViewPermissionException(String message) {
    super(message);
  }
  
  public ViewPermissionException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
