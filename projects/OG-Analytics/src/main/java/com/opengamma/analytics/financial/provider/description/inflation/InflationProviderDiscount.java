/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class InflationProviderDiscount implements InflationProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderDiscount _multicurveProvider;
  /**
   * A map with one price curve by price index.
   */
  private final Map<IndexPrice, PriceIndexCurve> _priceIndexCurves;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public InflationProviderDiscount() {
    _multicurveProvider = new MulticurveProviderDiscount();
    _priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public InflationProviderDiscount(final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(fxMatrix);
    _priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param fxMatrix The FXMatrix.
   */
  public InflationProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves, final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _priceIndexCurves = priceIndexCurves;
  }

  /**
   * Constructor from exiting multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider.
   * @param priceIndexCurves The map with price index curves.
   */
  public InflationProviderDiscount(final MulticurveProviderDiscount multicurve, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves) {
    _multicurveProvider = multicurve;
    _priceIndexCurves = priceIndexCurves;
  }

  @Override
  public InflationProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = _multicurveProvider.copy();
    final LinkedHashMap<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>(_priceIndexCurves);
    return new InflationProviderDiscount(multicurveProvider, priceIndexCurves);
  }

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getPriceIndex(time);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public String getName(final IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getCurve().getName();
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(final IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _priceIndexCurves.keySet();
  }

  /**
   * Sets the price index curve for a price index.
   * @param index The price index.
   * @param curve The curve.
   */
  public void setCurve(final IndexPrice index, final PriceIndexCurve curve) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(curve, "curve");
    if (_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve already set: " + index.toString());
    }
    _priceIndexCurves.put(index, curve);
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _multicurveProvider;
  }

  //     =====     Methods related to MulticurveProvider     =====

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _multicurveProvider.getDiscountFactor(ccy, time);
  }

  @Override
  public String getName(final Currency ccy) {
    return _multicurveProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _multicurveProvider.getCurrencies();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IborIndex index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _multicurveProvider.getIndexesIbor();
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _multicurveProvider.getIndexesON();
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return _multicurveProvider.getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return _multicurveProvider.getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return _multicurveProvider.getCurve(index);
  }

  @Override
  /**
   * Returns all curves names. The order is the natural order of String.
   */
  public Set<String> getAllNames() {
    final Set<String> names = new TreeSet<String>();
    names.addAll(_multicurveProvider.getAllNames());
    final Set<IndexPrice> priceSet = _priceIndexCurves.keySet();
    for (final IndexPrice price : priceSet) {
      names.add(_priceIndexCurves.get(price).getName());
    }
    return names;
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   * TODO: REVIEW: Should we check that the curve are already present?
   */
  public void setAll(final InflationProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _multicurveProvider.setAll(other.getMulticurveProvider());
    _priceIndexCurves.putAll(other._priceIndexCurves);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(index, curve);
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param index The price index.
   * @param curve The price curve for the index.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final IndexPrice index, final PriceIndexCurve curve) {
    ArgumentChecker.notNull(index, "Price index");
    ArgumentChecker.notNull(curve, "curve");
    if (!_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve not in set: " + index);
    }
    _priceIndexCurves.put(index, curve);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _multicurveProvider.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _multicurveProvider.getFxRates();
  }

  @Override
  public InflationProviderDiscount withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement) {
    MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withDiscountFactor(ccy, replacement);
    return new InflationProviderDiscount(decoratedMulticurve, _priceIndexCurves);
  }

}
