/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


public class GICSCodeBean extends EnumWithDescriptionBean {
  protected GICSCodeBean() {
  }

  public GICSCodeBean(final String code, final String description) {
    super(code, description);
    System.err.println ("code=" + code + "; description=" + description);
  }
}
