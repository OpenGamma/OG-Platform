/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.market;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IndexPrice;

/**
 * A market bundle decorated for a given price index curve and a specific time. The price index is shifted by the shift provided.
 */
public class MarketPriceIndexTimeDecorated extends MarketBundle {

  /**
   * The Price index for which the market is decorated.
   */
  private final IndexPrice _index;
  /**
   * The time at which the price index value is decorated.
   */
  private final double _time;
  /**
   * The shift applied to the price.
   */
  private final double _shift;

  /**
   * Constructor from an exiting market, the currency and a time to be decorated for discounting.
   * @param market The original market.
   * @param index The price index.
   * @param time The time.
   * @param shift The shift.
   */
  public MarketPriceIndexTimeDecorated(MarketBundle market, IndexPrice index, double time, double shift) {
    super(market);
    Validate.notNull(index, "Index");
    _index = index;
    _time = time;
    _shift = shift;
  }

  @Override
  public double getPriceIndex(IndexPrice index, Double time) {
    double price = super.getPriceIndex(index, time);
    if ((index == _index) && (_time == time)) {
      return price + _shift;
    }
    return price;
  }

}
