/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

public class RootNotFoundException extends RuntimeException {

  public RootNotFoundException() {
    super();
  }

  public RootNotFoundException(final String s) {
    super(s);
  }

  public RootNotFoundException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public RootNotFoundException(final Throwable cause) {
    super(cause);
  }
}
