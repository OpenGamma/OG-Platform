package com.opengamma.auth;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class AuthorisationException extends OpenGammaRuntimeException {
  public AuthorisationException(String message) {
    super(message);
  }

  public AuthorisationException(String message, Throwable cause) {
    super(message, cause);
  }
}
