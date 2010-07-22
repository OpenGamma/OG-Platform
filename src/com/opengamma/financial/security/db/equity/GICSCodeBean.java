/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.equity;

import com.opengamma.financial.security.db.EnumWithDescriptionBean;


public class GICSCodeBean extends EnumWithDescriptionBean {
  protected GICSCodeBean() {
  }

  public GICSCodeBean(final String code, final String description) {
    super(code, description);
  }
}
