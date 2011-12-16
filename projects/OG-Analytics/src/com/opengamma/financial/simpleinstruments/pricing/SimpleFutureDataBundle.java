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
public class SimpleFutureDataBundle {
  private final YieldAndDiscountCurve _yieldCurve;
  private final double _spot;
  private final double _costOfCarry;
  
  public SimpleFutureDataBundle(final YieldAndDiscountCurve yieldCurve, final double spot, final double costOfCarry) {
    Validate.notNull(yieldCurve, "yield curve");
    _yieldCurve = yieldCurve;
    _spot = spot;
    _costOfCarry = costOfCarry;
  }
  
  public YieldAndDiscountCurve getCurve() {
    return _yieldCurve;
  }
  
  public double getSpot() {
    return _spot;
  }
  
  public double getCostOfCarry() {
    return _costOfCarry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_costOfCarry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _yieldCurve.hashCode();
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
    final SimpleFutureDataBundle other = (SimpleFutureDataBundle) obj;
    if (Double.doubleToLongBits(_costOfCarry) != Double.doubleToLongBits(other._costOfCarry)) {
      return false;
    }
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldCurve, other._yieldCurve)) {
      return false;
    }
    return true;
  }
  
  
}
