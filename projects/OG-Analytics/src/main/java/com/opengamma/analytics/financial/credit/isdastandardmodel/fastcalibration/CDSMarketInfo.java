/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

/**
 * 
 */
public class CDSMarketInfo {

  private final double _coupon;
  private final double _puf;
  private final double _lgd;

  public CDSMarketInfo(final double coupon, final double puf, final double recoveryRate) {
    _coupon = coupon;
    _puf = puf;
    _lgd = 1 - recoveryRate;
  }

  /**
   * Gets the coupon.
   * @return the coupon
   */
  public double getCoupon() {
    return _coupon;
  }

  /**
   * Gets the puf.
   * @return the puf
   */
  public double getPuf() {
    return _puf;
  }

  /**
   * Gets the lgd.
   * @return the lgd
   */
  public double getLGD() {
    return _lgd;
  }

}
