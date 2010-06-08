/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * Thrown if there is a problem with interpolation.
 * 
 */
public class InterpolationException extends RuntimeException {

  public InterpolationException() {
    super();
  }

  public InterpolationException(final String s) {
    super(s);
  }

  public InterpolationException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public InterpolationException(final Throwable cause) {
    super(cause);
  }
}
