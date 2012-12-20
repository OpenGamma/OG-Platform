/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.masterdb.security.hibernate.EnumBean;

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
