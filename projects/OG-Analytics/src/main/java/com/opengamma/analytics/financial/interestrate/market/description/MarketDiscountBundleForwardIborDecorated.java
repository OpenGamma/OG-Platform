/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * A market bundle decorated (i.e. some methods are changed in special cases) for a given forward curve.
 */
public class MarketDiscountBundleForwardIborDecorated extends MarketDiscountBundle {
  // TODO: review to make an extension of IMarketBundle

  /**
   * The index for which the discounting curve is decorated.
   */
  private final IborIndex _index;
  /**
   * The replacing discounting curve.
   */
  private final YieldAndDiscountCurve _curve;

  /**
   * Constructor from an exiting market and the index to be decorated for forward.
   * @param market The original market.
   * @param index The index for which the forward curve will be decorated.
   * @param curve The replacing curve.
   */
  public MarketDiscountBundleForwardIborDecorated(MarketDiscountBundle market, IborIndex index, YieldAndDiscountCurve curve) {
    super(market);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(curve, "Curve");
    _index = index;
    _curve = curve;
  }

  @Override
  public double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor) {
    if (_index.equals(index)) {
      return (_curve.getDiscountFactor(startTime) / _curve.getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    return super.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public YieldAndDiscountCurve getCurve(IborIndex index) {
    if (_index.equals(index)) {
      return _curve;
    }
    return super.getCurve(index);
  }

}
