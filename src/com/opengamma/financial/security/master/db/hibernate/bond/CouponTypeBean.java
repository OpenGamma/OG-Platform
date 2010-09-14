/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.financial.security.master.db.hibernate.EnumBean;

/**
 * Hibernate bean for storing a coupon type.
 */
public class CouponTypeBean extends EnumBean {

  protected CouponTypeBean() {
  }

  public CouponTypeBean(String couponType) {
    super(couponType);
  }

}
