/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * The base class for all runtime exceptions thrown by any OpenGamma code,
 * outside the base JRE-defined runtime exceptions.
 *
 * @author kirk
 */
public class OpenGammaRuntimeException extends RuntimeException {
  
  public OpenGammaRuntimeException(String message) {
    super(message);
  }
  
  public OpenGammaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
