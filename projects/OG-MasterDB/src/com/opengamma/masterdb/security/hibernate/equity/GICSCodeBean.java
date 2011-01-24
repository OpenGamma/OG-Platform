/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import com.opengamma.masterdb.security.hibernate.EnumWithDescriptionBean;

/**
 * Hibernate bean for storage.
 */
public class GICSCodeBean extends EnumWithDescriptionBean {

  protected GICSCodeBean() {
  }

  public GICSCodeBean(final String code, final String description) {
    super(code, description);
  }

}
