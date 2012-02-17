/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;


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
   * @param riskFreeCurve The risk free interest rate curve (in FX this is the domestic risk free curve)
   * @param costOfCarryCurve In equity this would represent the expected dividend yield, while in FX it is the foreign risk free rate
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
}
