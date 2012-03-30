/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;

/**
 * A market bundle decorated for a given discounting curve and a specific time. The (zero-coupon) rate is shifted by the shift provided.
 */
public class MarketDiscountingTimeDecorated extends MarketBundle {

  /**
   * The currency for which the discounting curve is decorated.
   */
  private final Currency _ccy;
  /**
   * The time at which the discounting rate is decorated.
   */
  private final double _time;
  /**
   * The shift applied to the rate.
   */
  private final double _shift;

  /**
   * Constructor from an exiting market, the currency and a time to be decorated for discounting.
   * @param market The original market.
   * @param ccy The currency for which the discounting curve will be decorated.
   * @param time The time.
   * @param shift The shift.
   */
  public MarketDiscountingTimeDecorated(MarketBundle market, Currency ccy, double time, double shift) {
    super(market);
    Validate.notNull(ccy, "Currency");
    _ccy = ccy;
    _time = time;
    _shift = shift;
  }

  @Override
  public double getDiscountingFactor(Currency ccy, Double time) {
    if ((ccy == _ccy) && (_time == time)) {
      double rate = -Math.log(super.getDiscountingFactor(ccy, time)) / time;
      return Math.exp(-(rate + _shift) * time);
    }
    return super.getDiscountingFactor(ccy, time);
  }

}
