/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;



/**
 * 
 */
public class ForwardCurveYieldImplied extends ForwardCurve {
  private final double _spot;
  private final YieldAndDiscountCurve _riskFreeCurve;
  private final YieldAndDiscountCurve _costOfCarryCurve;

  /**
   * 
   * @param spot The current value of the underlying
   * @param riskFreeCurve The risk free interest rate curve (in FX this is the domestic risk free curve), not null
   * @param costOfCarryCurve In equity this would represent the expected dividend yield, while in FX it is the foreign risk free rate, not null
   */
  public ForwardCurveYieldImplied(final double spot, final YieldAndDiscountCurve riskFreeCurve, final YieldAndDiscountCurve costOfCarryCurve) {
    super(getForwardCurve(spot, riskFreeCurve, costOfCarryCurve));
    _spot = spot;
    _riskFreeCurve = riskFreeCurve;
    _costOfCarryCurve = costOfCarryCurve;
  }

  @Override
  public double getSpot() {
    return _spot;
  }

  public YieldAndDiscountCurve getRiskFreeCurve() {
    return _riskFreeCurve;
  }

  public YieldAndDiscountCurve getCostOfCarryCurve() {
    return _costOfCarryCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _costOfCarryCurve.hashCode();
    result = prime * result + _riskFreeCurve.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof ForwardCurveYieldImplied)) {
      return false;
    }
    final ForwardCurveYieldImplied other = (ForwardCurveYieldImplied) obj;
    if (Double.compare(_spot, other._spot) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_costOfCarryCurve, other._costOfCarryCurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_riskFreeCurve, other._riskFreeCurve)) {
      return false;
    }
    return true;
  }


}
