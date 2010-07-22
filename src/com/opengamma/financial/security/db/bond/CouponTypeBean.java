/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.bond;

import com.opengamma.financial.security.db.EnumBean;

public class CouponTypeBean extends EnumBean {
  
  protected CouponTypeBean() {
  }

  public CouponTypeBean(String couponType) {
    super(couponType);
  }
  
}
