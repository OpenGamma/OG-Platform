/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storage.
 */
public class BondFutureTypeBean extends EnumBean {
  
  protected BondFutureTypeBean() {
  }

  public BondFutureTypeBean(String bondType) {
    super(bondType);
  }
  
}
