/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
