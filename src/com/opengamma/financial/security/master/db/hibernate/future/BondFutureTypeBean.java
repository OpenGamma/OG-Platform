/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.future;

import com.opengamma.financial.security.master.db.hibernate.EnumBean;

public class BondFutureTypeBean extends EnumBean {
  
  protected BondFutureTypeBean() {
  }

  public BondFutureTypeBean(String bondType) {
    super(bondType);
  }
  
}
