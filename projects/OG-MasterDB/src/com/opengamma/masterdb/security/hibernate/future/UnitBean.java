/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storage.
 */
public class UnitBean extends EnumBean {
  
  protected UnitBean() {
  }

  public UnitBean(String unitName) {
    super(unitName);
  }
  
}
