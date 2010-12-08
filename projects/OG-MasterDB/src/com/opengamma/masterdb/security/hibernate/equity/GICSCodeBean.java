/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import com.opengamma.masterdb.security.hibernate.EnumWithDescriptionBean;


public class GICSCodeBean extends EnumWithDescriptionBean {
  protected GICSCodeBean() {
  }

  public GICSCodeBean(final String code, final String description) {
    super(code, description);
  }
}
