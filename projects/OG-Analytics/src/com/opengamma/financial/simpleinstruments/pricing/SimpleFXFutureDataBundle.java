/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.simpleinstruments.pricing;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class SimpleFXFutureDataBundle {
  private final YieldAndDiscountCurve _payYieldCurve;
  private final YieldAndDiscountCurve _receiveYieldCurve;
  private final double _spot;
  
  public SimpleFXFutureDataBundle(final YieldAndDiscountCurve payYieldCurve, final YieldAndDiscountCurve receiveYieldCurve, final double spot) {
    Validate.notNull(payYieldCurve, "pay yield curve");
    Validate.notNull(receiveYieldCurve, "receive yield curve");
    _payYieldCurve = payYieldCurve;
    _receiveYieldCurve = receiveYieldCurve;
    _spot = spot;
  }
  
  public YieldAndDiscountCurve getPayCurve() {
    return _payYieldCurve;
  }
  
  public YieldAndDiscountCurve getReceiveCurve() {
    return _receiveYieldCurve;
  }
  
  public double getSpot() {
    return _spot;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _payYieldCurve.hashCode();
    result = prime * result + _receiveYieldCurve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleFXFutureDataBundle other = (SimpleFXFutureDataBundle) obj;
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot)) {
      return false;
    }
    if (!ObjectUtils.equals(_payYieldCurve, other._payYieldCurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_receiveYieldCurve, other._receiveYieldCurve)) {
      return false;
    }
    return true;
  }
  
  
}
