/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A market bundle decorated for a given discounting curve.
 */
public class MarketDiscountingDecorated extends MarketDiscountBundle {

  /**
   * The currency for which the discounting curve is decorated.
   */
  private final Currency _ccy;
  /**
   * The replacing discounting curve.
   */
  private final YieldAndDiscountCurve _curve;

  /**
   * Constructor from an exiting market and the currency to be decorated for discounting.
   * @param market The original market.
   * @param ccy The currency for which the discounting curve will be decorated.
   * @param curve The replacing curve for the discounting.
   */
  public MarketDiscountingDecorated(MarketDiscountBundle market, Currency ccy, YieldAndDiscountCurve curve) {
    super(market);
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(curve, "Curve");
    _ccy = ccy;
    _curve = curve;
  }

  @Override
  public double getDiscountFactor(Currency ccy, Double time) {
    if (ccy == _ccy) {
      return _curve.getDiscountFactor(time);
    }
    return super.getDiscountFactor(ccy, time);
  }

  @Override
  public YieldAndDiscountCurve getCurve(Currency ccy) {
    if (ccy == _ccy) {
      return _curve;
    }
    return super.getCurve(ccy);
  }

}
