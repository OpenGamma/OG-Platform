/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * Thrown if there is a problem with interpolation.
 * 
 * @author emcleod
 */

public class InterpolationException extends RuntimeException {

  public InterpolationException() {
    super();
  }

  public InterpolationException(String s) {
    super(s);
  }

  public InterpolationException(String s, Throwable cause) {
    super(s, cause);
  }

  public InterpolationException(Throwable cause) {
    super(cause);
  }
}
