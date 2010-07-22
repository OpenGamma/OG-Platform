/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.future;

import com.opengamma.financial.security.db.EnumBean;

public class BondFutureTypeBean extends EnumBean {
  
  protected BondFutureTypeBean() {
  }

  public BondFutureTypeBean(String bondType) {
    super(bondType);
  }
  
}
