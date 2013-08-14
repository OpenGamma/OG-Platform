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
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class InflationIssuerProviderDiscount implements InflationIssuerProviderInterface {

  /**
   * The multicurve provider.
   */
  private final InflationProviderDiscount _inflationProvider;
  /**
   * A map with issuer discounting curves.
   */
  private final Map<Pair<String, Currency>, YieldAndDiscountCurve> _issuerCurves;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public InflationIssuerProviderDiscount() {
    _inflationProvider = new InflationProviderDiscount();
    _issuerCurves = new LinkedHashMap<>();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public InflationIssuerProviderDiscount(final FXMatrix fxMatrix) {
    _inflationProvider = new InflationProviderDiscount(fxMatrix);
    _issuerCurves = new LinkedHashMap<>();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param issuerCurves A map with issuer discounting curves.
   * @param fxMatrix The FXMatrix.
   */
  public InflationIssuerProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves,
      final FXMatrix fxMatrix) {
    _inflationProvider = new InflationProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, priceIndexCurves, fxMatrix);
    _issuerCurves = issuerCurves;
  }

  /**
   * Constructor from exiting multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param inflation The inflation provider.
   * @param issuerCurves A map with issuer discounting curves.
   */
  public InflationIssuerProviderDiscount(final InflationProviderDiscount inflation, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves) {
    _inflationProvider = inflation;
    _issuerCurves = issuerCurves;
  }

  @Override
  public InflationIssuerProviderDiscount copy() {
    final InflationProviderDiscount inflationProvider = _inflationProvider.copy();
    final LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<>(_issuerCurves);
    return new InflationIssuerProviderDiscount(inflationProvider, issuerCurves);
  }

  /**
   * Sets the price index curve for a price index.
   * @param issuerCcy The issuer/currency pair.
   * @param curve The curve.
   */
  public void setCurve(final Pair<String, Currency> issuerCcy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuerCcy, "Name-currency");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuerCcy)) {
      throw new IllegalArgumentException("Issuer/currency curve already set: " + issuerCcy.toString());
    }
    _issuerCurves.put(issuerCcy, curve);
  }

  public void setCurve(final String issuer, final Currency ccy, final YieldAndDiscountCurve curve) {
    setCurve(new ObjectsPair<>(issuer, ccy), curve);
  }

  @Override
  public double getDiscountFactor(final Pair<String, Currency> issuerCcy, final Double time) {
    return _issuerCurves.get(issuerCcy).getDiscountFactor(time);
  }

  @Override
  public Set<Pair<String, Currency>> getIssuersCcy() {
    return _issuerCurves.keySet();
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _inflationProvider.getMulticurveProvider();
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return _inflationProvider;
  }

  public YieldAndDiscountCurve getCurve(final Pair<String, Currency> issuerCcy) {
    return _issuerCurves.get(issuerCcy);
  }

  //     =====     Methods related to InflationProvider     =====

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    return _inflationProvider.getPriceIndex(index, time);
  }

  @Override
  public String getName(final IndexPrice index) {
    return _inflationProvider.getName(index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(final IndexPrice index) {
    return _inflationProvider.getCurve(index);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _inflationProvider.getPriceIndexes();
  }

  /**
   * Sets the price index curve for a price index.
   * @param index The price index.
   * @param curve The curve.
   */
  public void setCurve(final IndexPrice index, final PriceIndexCurve curve) {
    _inflationProvider.setCurve(index, curve);
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param index The price index.
   * @param curve The price curve for the index.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IndexPrice index, final PriceIndexCurve curve) {
    _inflationProvider.replaceCurve(index, curve);
  }

  //     =====     Methods related to MulticurveProvider     =====

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _inflationProvider.getDiscountFactor(ccy, time);
  }

  @Override
  public String getName(final Currency ccy) {
    return _inflationProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _inflationProvider.getCurrencies();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IborIndex index) {
    return _inflationProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _inflationProvider.getIndexesIbor();
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _inflationProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _inflationProvider.getIndexesON();
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return _inflationProvider.getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return _inflationProvider.getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return _inflationProvider.getCurve(index);
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(index, curve);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _inflationProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _inflationProvider.replaceCurve(index, curve);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _inflationProvider.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _inflationProvider.getFxRates();
  }

  //     =====     Methods related to All     =====

  @Override
  /**
   * Returns all curves names. The order is the natural order of String.
   */
  public Set<String> getAllNames() {
    final Set<String> names = new TreeSet<>();
    names.addAll(_inflationProvider.getAllNames());
    final Set<Pair<String, Currency>> issuerSet = _issuerCurves.keySet();
    for (final Pair<String, Currency> issuer : issuerSet) {
      names.add(_issuerCurves.get(issuer).getName());
    }
    return names;
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   * TODO: REVIEW: Should we check that the curve are already present?
   */
  public void setAll(final InflationIssuerProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _inflationProvider.setAll(other.getInflationProvider());
    _issuerCurves.putAll(other._issuerCurves);
  }

  //     =====     Convenience methods     =====

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final Pair<String, Currency> replacement) {
    return _inflationProvider.withDiscountFactor(ccy, _issuerCurves.get(replacement));
  }

}
