/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class MarketDiscountBundle implements IMarketBundle {

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
  private final Map<Pair<String, Currency>, YieldAndDiscountCurve> _issuerCurves;
  /**
   * The matrix containing the exchange rates.
   */
  private final FXMatrix _fxMatrix;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public MarketDiscountBundle() {
    _discountingCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>();
    _forwardCurves = new LinkedHashMap<IndexDeposit, YieldAndDiscountCurve>();
    _priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>();
    _issuerCurves = new LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve>();
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardCurves A map with one (forward) curve by Ibor/OIS index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param issuerCurves A map with issuer discounting curves.
   * @param fxMatrix The FXMatrix.
   */
  public MarketDiscountBundle(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IndexDeposit, YieldAndDiscountCurve> forwardCurves,
      final Map<IndexPrice, PriceIndexCurve> priceIndexCurves, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
    _discountingCurves = discountingCurves;
    _forwardCurves = forwardCurves;
    _priceIndexCurves = priceIndexCurves;
    _issuerCurves = issuerCurves;
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardCurves A map with one (forward) curve by Ibor/OIS index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param issuerCurves A map with issuer discounting curves.
   */
  public MarketDiscountBundle(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IndexDeposit, YieldAndDiscountCurve> forwardCurves,
      final Map<IndexPrice, PriceIndexCurve> priceIndexCurves, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves) {
    _discountingCurves = discountingCurves;
    _forwardCurves = forwardCurves;
    _priceIndexCurves = priceIndexCurves;
    _issuerCurves = issuerCurves;
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor from exiting maps. The given maps are used for the new market (the same maps are used, not copied).
   * @param market The existing market.
   */
  public MarketDiscountBundle(MarketDiscountBundle market) {
    _discountingCurves = market._discountingCurves;
    _forwardCurves = market._forwardCurves;
    _priceIndexCurves = market._priceIndexCurves;
    _issuerCurves = market._issuerCurves;
    _fxMatrix = market._fxMatrix;
  }

  @Override
  public MarketDiscountBundle copy() {
    final LinkedHashMap<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>(_discountingCurves);
    final LinkedHashMap<IndexDeposit, YieldAndDiscountCurve> forwardCurves = new LinkedHashMap<IndexDeposit, YieldAndDiscountCurve>(_forwardCurves);
    final LinkedHashMap<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>(_priceIndexCurves);
    final LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve>(_issuerCurves);
    final FXMatrix fxMatrix = new FXMatrix(_fxMatrix);
    return new MarketDiscountBundle(discountingCurves, forwardCurves, priceIndexCurves, issuerCurves, fxMatrix);
  }

  @Override
  public double getDiscountFactor(Currency ccy, Double time) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy).getDiscountFactor(time);
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
  }

  @Override
  public String getName(Currency ccy) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy).getName();
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
  }

  @Override
  public Set<Currency> getAllCurrencies() {
    return _discountingCurves.keySet();
  }

  @Override
  public double[] parameterSensitivity(Currency ccy, List<DoublesPair> pointSensitivity) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(ccy);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        double[] sensi1Point = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters(Currency ccy) {
    return _discountingCurves.get(ccy).getNumberOfParameters();
  }

  @Override
  public double getForwardRate(IndexDeposit index, double startTime, double endTime, double accrualFactor) {
    if (_forwardCurves.containsKey(index)) {
      return (_forwardCurves.get(index).getDiscountFactor(startTime) / _forwardCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public double getForwardRate(IndexDeposit index, double startTime) {
    throw new UnsupportedOperationException("The Curve implementation of the Market bundle does not support the forward rate without end time and accrual factor.");
  }

  @Override
  public String getName(IndexDeposit index) {
    if (_forwardCurves.containsKey(index)) {
      return _forwardCurves.get(index).getName();
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public Set<IndexDeposit> getAllIndexDeposit() {
    return _forwardCurves.keySet();
  }

  @Override
  public double[] parameterSensitivity(IndexDeposit index, List<MarketForwardSensitivity> pointSensitivity) {
    final YieldAndDiscountCurve curve = _forwardCurves.get(index);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final MarketForwardSensitivity timeAndS : pointSensitivity) {
        Triple<Double, Double, Double> point = timeAndS.getPoint();
        double forwardBar = timeAndS.getValue();
        // Implementation note: only the sensitivity to the forward is available. The sensitivity to the pseudo-discount factors need to be computed.
        double dfForwardStart = curve.getDiscountFactor(point.getFirst());
        double dfForwardEnd = curve.getDiscountFactor(point.getSecond());
        final double dFwddyStart = -point.getFirst() * dfForwardStart / (dfForwardEnd * point.getThird());
        final double dFwddyEnd = point.getSecond() * dfForwardStart / (dfForwardEnd * point.getThird());
        double[] sensiPtStart = curve.getInterestRateParameterSensitivity(point.getFirst());
        double[] sensiPtEnd = curve.getInterestRateParameterSensitivity(point.getSecond());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += dFwddyStart * sensiPtStart[loopparam] * forwardBar;
          result[loopparam] += dFwddyEnd * sensiPtEnd[loopparam] * forwardBar;
        }
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters(IndexDeposit index) {
    return _forwardCurves.get(index).getNumberOfParameters();
  }

  @Override
  public double getPriceIndex(IndexPrice index, Double time) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getPriceIndex(time);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public String getName(IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getCurve().getName();
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public double getDiscountFactor(final Pair<String, Currency> issuerCcy, Double time) {
    if (_issuerCurves.containsKey(issuerCcy)) {
      return _issuerCurves.get(issuerCcy).getDiscountFactor(time);
    }
    throw new IllegalArgumentException("Issuer/Currency discounting curve not found: " + issuerCcy);
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
  public YieldAndDiscountCurve getCurve(IndexDeposit index) {
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
   * @param issuerCcy The issuer name/currency pair.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Pair<String, Currency> issuerCcy) {
    if (_issuerCurves.containsKey(issuerCcy)) {
      return _issuerCurves.get(issuerCcy);
    }
    throw new IllegalArgumentException("Issuer curve not found: " + issuerCcy);
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
   * Gets the set of issuer names by currency defined in the market.
   * @return The set of issuers names/currencies.
   */
  public Set<Pair<String, Currency>> getIssuers() {
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
  public void setCurve(final IndexDeposit index, final YieldAndDiscountCurve curve) {
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
   * @param ccy The currency.
   * @param curve The curve.
   */
  public void setCurve(final String issuer, final Currency ccy, final YieldAndDiscountCurve curve) {
    Validate.notNull(issuer, "issuer");
    Validate.notNull(curve, "curve");
    if (_issuerCurves.containsKey(Pair.of(issuer, ccy))) {
      throw new IllegalArgumentException("Issuer curve already set: " + issuer);
    }
    _issuerCurves.put(ObjectsPair.of(issuer, ccy), curve);
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
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final IndexDeposit index, final YieldAndDiscountCurve curve) {
    Validate.notNull(index, "Index");
    Validate.notNull(curve, "curve");
    if (!_forwardCurves.containsKey(index)) {
      throw new IllegalArgumentException("Forward curve not in set: " + index);
    }
    _forwardCurves.put(index, curve);
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

  @Override
  public double getFxRate(Currency ccy1, Currency ccy2) {
    return _fxMatrix.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _fxMatrix;
  }

}
