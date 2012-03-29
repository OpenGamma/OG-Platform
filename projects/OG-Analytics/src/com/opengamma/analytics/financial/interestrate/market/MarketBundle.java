/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 */
public class MarketBundle {

  /**
   * A map with one (discounting) curve by currency.
   */
  private final Map<Currency, YieldAndDiscountCurve> _discountingCurves;
  /**
   * A map with one (forward) curve by Ibor/OIS index.
   */
  private final Map<IndexDeposit, YieldAndDiscountCurve> _forwardCurves;
  /**
   * A map with one price curve by price index.
   */
  private final Map<IndexPrice, PriceIndexCurve> _priceIndexCurves;
  /**
   * A map with issuer discounting curves.
   */
  private final Map<String, YieldAndDiscountCurve> _issuerCurves;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public MarketBundle() {
    _discountingCurves = new HashMap<Currency, YieldAndDiscountCurve>();
    _forwardCurves = new HashMap<IndexDeposit, YieldAndDiscountCurve>();
    _priceIndexCurves = new HashMap<IndexPrice, PriceIndexCurve>();
    _issuerCurves = new HashMap<String, YieldAndDiscountCurve>();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param market The existing market.
   */
  public MarketBundle(MarketBundle market) {
    _discountingCurves = market._discountingCurves;
    _forwardCurves = market._forwardCurves;
    _priceIndexCurves = market._priceIndexCurves;
    _issuerCurves = market._issuerCurves;
  }

  /**
   * Gets the discount factor for one currency at a given time to maturity.
   * @param ccy The currency.
   * @param time The time.
   * @return The discount factor.
   */
  public double getDiscountingFactor(Currency ccy, Double time) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy).getDiscountFactor(time);
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
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
    if (_forwardCurves.containsKey(index)) {
      return (_forwardCurves.get(index).getDiscountFactor(startTime) / _forwardCurves.get(index).getDiscountFactor(endTime) - 1) / accuralFactor;
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  /**
   * Gets the estimated price index for a given reference time.
   * @param index The price index.
   * @param time The reference time.
   * @return The price index.
   */
  public double getPriceIndex(IndexPrice index, Double time) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getPriceIndex(time);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(Currency ccy) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy);
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(IborIndex index) {
    if (_forwardCurves.containsKey(index)) {
      return _forwardCurves.get(index);
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  /**
   * Gets the discounting curve associated to an issuer.
   * @param issuer The issuer name.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(String issuer) {
    if (_issuerCurves.containsKey(issuer)) {
      return _issuerCurves.get(issuer);
    }
    throw new IllegalArgumentException("Issuer curve not found: " + issuer);
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
  public Set<IndexDeposit> getIborIndexes() {
    return _forwardCurves.keySet();
  }

  /**
   * Gets the set of price indexes defined in the market.
   * @return The set of index.
   */
  public Set<IndexPrice> getPriceIndexes() {
    return _priceIndexCurves.keySet();
  }

  /**
   * Gets the set of issuer names defined in the market.
   * @return The set of issuers names.
   */
  public Set<String> getIssuers() {
    return _issuerCurves.keySet();
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
  public void setCurve(final IndexPrice index, final PriceIndexCurve curve) {
    Validate.notNull(index, "index");
    Validate.notNull(curve, "curve");
    if (_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve already set: " + index.toString());
    }
    _priceIndexCurves.put(index, curve);
  }

  /**
   * Sets the curve associated to an issuer.
   * @param issuer The issuer name.
   * @param curve The curve.
   */
  public void setCurve(final String issuer, final YieldAndDiscountCurve curve) {
    Validate.notNull(issuer, "issuer");
    Validate.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuer)) {
      throw new IllegalArgumentException("Issuer curve already set: " + issuer);
    }
    _issuerCurves.put(issuer, curve);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    Validate.notNull(ccy, "Currency");
    Validate.notNull(curve, "curve");
    if (!_discountingCurves.containsKey(ccy)) {
      throw new IllegalArgumentException("Currency discounting curve not in set: " + ccy);
    }
    _discountingCurves.put(ccy, curve);
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param index The price index.
   * @param curve The price curve for the index.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final IndexPrice index, final PriceIndexCurve curve) {
    Validate.notNull(index, "Price index");
    Validate.notNull(curve, "curve");
    if (!_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve not in set: " + index);
    }
    _priceIndexCurves.put(index, curve);
  }

}
