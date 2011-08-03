/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;

/**
 * Class describing a "market" with discounting, forward and price index curves.
 */
public class MarketBundle {

  /**
   * A map with one (discounting) curve by currency.
   */
  private final Map<Currency, YieldAndDiscountCurve> _discountingCurves;
  /**
   * A map with one (forward) curve by Ibor index.
   */
  private final Map<IborIndex, YieldAndDiscountCurve> _forwardCurves;
  /**
   * A map with one price curve by price index.
   */
  private final Map<PriceIndex, PriceIndexCurve> _priceIndexCurves;

  // TODO: Add credit curves.

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public MarketBundle() {
    _discountingCurves = new HashMap<Currency, YieldAndDiscountCurve>();
    _forwardCurves = new HashMap<IborIndex, YieldAndDiscountCurve>();
    _priceIndexCurves = new HashMap<PriceIndex, PriceIndexCurve>();
  }

  /**
   * Gets the discount factor for one currency at a given time to maturity.
   * @param ccy The currency.
   * @param time The time.
   * @return The discount factor.
   */
  public double getDiscountingFactor(Currency ccy, Double time) {
    return _discountingCurves.get(ccy).getDiscountFactor(time);
  }

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accuralFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  public double getForwardRate(IborIndex index, double startTime, double endTime, double accuralFactor) {
    return (_forwardCurves.get(index).getDiscountFactor(startTime) / _forwardCurves.get(index).getDiscountFactor(endTime) - 1) / accuralFactor;
  }

  /**
   * Gets the estimated price index for a given reference time.
   * @param index The price index.
   * @param time The reference time.
   * @return The price index.
   */
  public double getPriceIndex(PriceIndex index, Double time) {
    return _priceIndexCurves.get(index).getPriceIndex(time);
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(Currency ccy) {
    return _discountingCurves.get(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(IborIndex index) {
    return _forwardCurves.get(index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(PriceIndex index) {
    return _priceIndexCurves.get(index);
  }

  /**
   * Gets the set of all currencies in the market.
   * @return The set of currencies.
   */
  public Set<Currency> getCurrencies() {
    return _discountingCurves.keySet();
  }

  /**
   * Gets the set of Ibor indexes defined in the market.
   * @return The set of index.
   */
  public Set<IborIndex> getIborIndexes() {
    return _forwardCurves.keySet();
  }

  /**
   * Gets the set of price indexes defined in the market.
   * @return The st of index.
   */
  public Set<PriceIndex> getPriceIndexes() {
    return _priceIndexCurves.keySet();
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    Validate.notNull(ccy, "currency");
    Validate.notNull(curve, "curve");
    if (_discountingCurves.containsKey(ccy)) {
      throw new IllegalArgumentException("Currency discounting curve already set: " + ccy.toString());
    }
    _discountingCurves.put(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    Validate.notNull(index, "index");
    Validate.notNull(curve, "curve");
    if (_forwardCurves.containsKey(index)) {
      throw new IllegalArgumentException("Ibor index forward curve already set: " + index.toString());
    }
    _forwardCurves.put(index, curve);
  }

  /**
   * Sets the price index curve for a price index.
   * @param index The price index.
   * @param curve The curve.
   */
  public void setCurve(final PriceIndex index, final PriceIndexCurve curve) {
    Validate.notNull(index, "index");
    Validate.notNull(curve, "curve");
    if (_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve already set: " + index.toString());
    }
    _priceIndexCurves.put(index, curve);
  }

}
