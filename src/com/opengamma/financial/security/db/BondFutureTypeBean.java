/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import javax.persistence.Entity;

@Entity
public class BondFutureTypeBean extends EnumBean {
  
  protected BondFutureTypeBean() {
  }

  public BondFutureTypeBean(String bondType) {
    super(bondType);
  }
  
}
