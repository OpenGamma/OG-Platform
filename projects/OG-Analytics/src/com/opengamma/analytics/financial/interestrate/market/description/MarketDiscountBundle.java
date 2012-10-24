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
import java.util.TreeSet;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

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
   * A map with one (forward) curve by ON index.
   */
  private final Map<IndexON, YieldAndDiscountCurve> _forwardONCurves;
  /**
   * A map with one (forward) curve by Ibor index.
   */
  private final Map<IborIndex, YieldAndDiscountCurve> _forwardIborCurves;
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
    _forwardIborCurves = new LinkedHashMap<IborIndex, YieldAndDiscountCurve>();
    _forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>();
    _priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>();
    _issuerCurves = new LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve>();
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public MarketDiscountBundle(final FXMatrix fxMatrix) {
    _discountingCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>();
    _forwardIborCurves = new LinkedHashMap<IborIndex, YieldAndDiscountCurve>();
    _forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>();
    _priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>();
    _issuerCurves = new LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve>();
    _fxMatrix = fxMatrix;
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
  public MarketDiscountBundle(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves,
      final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
    _discountingCurves = discountingCurves;
    _forwardIborCurves = forwardIborCurves;
    _forwardONCurves = forwardONCurves;
    _priceIndexCurves = priceIndexCurves;
    _issuerCurves = issuerCurves;
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param issuerCurves A map with issuer discounting curves.
   */
  public MarketDiscountBundle(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves,
      final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves) {
    _discountingCurves = discountingCurves;
    _forwardIborCurves = forwardIborCurves;
    _forwardONCurves = forwardONCurves;
    _priceIndexCurves = priceIndexCurves;
    _issuerCurves = issuerCurves;
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor from exiting maps. The given maps are used for the new market (the same maps are used, not copied).
   * @param market The existing market.
   */
  public MarketDiscountBundle(final MarketDiscountBundle market) {
    _discountingCurves = market._discountingCurves;
    _forwardIborCurves = market._forwardIborCurves;
    _forwardONCurves = market._forwardONCurves;
    _priceIndexCurves = market._priceIndexCurves;
    _issuerCurves = market._issuerCurves;
    _fxMatrix = market._fxMatrix;
  }

  @Override
  public MarketDiscountBundle copy() {
    final LinkedHashMap<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>(_discountingCurves);
    final LinkedHashMap<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(_forwardIborCurves);
    final LinkedHashMap<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>(_forwardONCurves);
    final LinkedHashMap<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<IndexPrice, PriceIndexCurve>(_priceIndexCurves);
    final LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<Pair<String, Currency>, YieldAndDiscountCurve>(_issuerCurves);
    final FXMatrix fxMatrix = new FXMatrix(_fxMatrix);
    return new MarketDiscountBundle(discountingCurves, forwardIborCurves, forwardONCurves, priceIndexCurves, issuerCurves, fxMatrix);
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy).getDiscountFactor(time);
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
  }

  @Override
  public String getName(final Currency ccy) {
    if (_discountingCurves.containsKey(ccy)) {
      return _discountingCurves.get(ccy).getName();
    }
    throw new IllegalArgumentException("Currency discounting curve not found: " + ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _discountingCurves.keySet();
  }

  @Override
  public double[] parameterSensitivity(final Currency ccy, final List<DoublesPair> pointSensitivity) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(ccy);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        final double[] sensi1Point = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters(final Currency ccy) {
    return _discountingCurves.get(ccy).getNumberOfParameters();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    if (_forwardIborCurves.containsKey(index)) {
      return (_forwardIborCurves.get(index).getDiscountFactor(startTime) / _forwardIborCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime) {
    throw new UnsupportedOperationException("The Curve implementation of the Market bundle does not support the forward rate without end time and accrual factor.");
  }

  @Override
  public String getName(final IborIndex index) {
    if (_forwardIborCurves.containsKey(index)) {
      return _forwardIborCurves.get(index).getName();
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _forwardIborCurves.keySet();
  }

  @Override
  public double[] parameterSensitivity(final IborIndex index, final List<MarketForwardSensitivity> pointSensitivity) {
    final YieldAndDiscountCurve curve = _forwardIborCurves.get(index);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final MarketForwardSensitivity timeAndS : pointSensitivity) {
        final double startTime = timeAndS.getStartTime();
        final double endTime = timeAndS.getEndTime();
        final double accrualFactor = timeAndS.getAccrualFactor();
        final double forwardBar = timeAndS.getValue();
        // Implementation note: only the sensitivity to the forward is available. The sensitivity to the pseudo-discount factors need to be computed.
        final double dfForwardStart = curve.getDiscountFactor(startTime);
        final double dfForwardEnd = curve.getDiscountFactor(endTime);
        final double dFwddyStart = -startTime * dfForwardStart / (dfForwardEnd * accrualFactor);
        final double dFwddyEnd = endTime * dfForwardStart / (dfForwardEnd * accrualFactor);
        final double[] sensiPtStart = curve.getInterestRateParameterSensitivity(startTime);
        final double[] sensiPtEnd = curve.getInterestRateParameterSensitivity(endTime);
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += dFwddyStart * sensiPtStart[loopparam] * forwardBar;
          result[loopparam] += dFwddyEnd * sensiPtEnd[loopparam] * forwardBar;
        }
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters(final IborIndex index) {
    return _forwardIborCurves.get(index).getNumberOfParameters();
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    if (_forwardONCurves.containsKey(index)) {
      return (_forwardONCurves.get(index).getDiscountFactor(startTime) / _forwardONCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward ON curve not found: " + index);
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime) {
    throw new UnsupportedOperationException("The Curve implementation of the Market bundle does not support the forward rate without end time and accrual factor.");
  }

  @Override
  public String getName(final IndexON index) {
    if (_forwardONCurves.containsKey(index)) {
      return _forwardONCurves.get(index).getName();
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _forwardONCurves.keySet();
  }

  @Override
  public double[] parameterSensitivity(final IndexON index, final List<MarketForwardSensitivity> pointSensitivity) {
    final YieldAndDiscountCurve curve = _forwardONCurves.get(index);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final MarketForwardSensitivity timeAndS : pointSensitivity) {
        final double startTime = timeAndS.getStartTime();
        final double endTime = timeAndS.getEndTime();
        final double accrualFactor = timeAndS.getAccrualFactor();
        final double forwardBar = timeAndS.getValue();
        // Implementation note: only the sensitivity to the forward is available. The sensitivity to the pseudo-discount factors need to be computed.
        final double dfForwardStart = curve.getDiscountFactor(startTime);
        final double dfForwardEnd = curve.getDiscountFactor(endTime);
        final double dFwddyStart = -startTime * dfForwardStart / (dfForwardEnd * accrualFactor);
        final double dFwddyEnd = endTime * dfForwardStart / (dfForwardEnd * accrualFactor);
        final double[] sensiPtStart = curve.getInterestRateParameterSensitivity(startTime);
        final double[] sensiPtEnd = curve.getInterestRateParameterSensitivity(endTime);
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += dFwddyStart * sensiPtStart[loopparam] * forwardBar;
          result[loopparam] += dFwddyEnd * sensiPtEnd[loopparam] * forwardBar;
        }
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters(final IndexON index) {
    return _forwardONCurves.get(index).getNumberOfParameters();
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

  @Override
  public double getDiscountFactor(final Pair<String, Currency> issuerCcy, final Double time) {
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
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
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
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    if (_forwardIborCurves.containsKey(index)) {
      return _forwardIborCurves.get(index);
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    if (_forwardONCurves.containsKey(index)) {
      return _forwardONCurves.get(index);
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
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

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _priceIndexCurves.keySet();
  }

  @Override
  public Set<Pair<String, Currency>> getIssuersCcy() {
    return _issuerCurves.keySet();
  }

  @Override
  /**
   * Returns all curves names. The order is the natural order of String.
   */
  public Set<String> getAllNames() {
    final Set<String> names = new TreeSet<String>();
    final Set<Currency> ccySet = _discountingCurves.keySet();
    for (final Currency ccy : ccySet) {
      names.add(_discountingCurves.get(ccy).getName());
    }
    final Set<IborIndex> indexSet = _forwardIborCurves.keySet();
    for (final IborIndex index : indexSet) {
      names.add(_forwardIborCurves.get(index).getName());
    }
    final Set<IndexON> indexONSet = _forwardONCurves.keySet();
    for (final IndexON index : indexONSet) {
      names.add(_forwardONCurves.get(index).getName());
    }
    final Set<IndexPrice> priceSet = _priceIndexCurves.keySet();
    for (final IndexPrice price : priceSet) {
      names.add(_priceIndexCurves.get(price).getName());
    }
    final Set<Pair<String, Currency>> issuerSet = _issuerCurves.keySet();
    for (final Pair<String, Currency> issuer : issuerSet) {
      names.add(_issuerCurves.get(issuer).getName());
    }
    return names;
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(ccy, "currency");
    ArgumentChecker.notNull(curve, "curve");
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
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(curve, "curve");
    if (_forwardIborCurves.containsKey(index)) {
      throw new IllegalArgumentException("Ibor index forward curve already set: " + index.toString());
    }
    _forwardIborCurves.put(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(curve, "curve");
    if (_forwardONCurves.containsKey(index)) {
      throw new IllegalArgumentException("ON index forward curve already set: " + index.toString());
    }
    _forwardONCurves.put(index, curve);
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

  /**
   * Sets the curve associated to an issuer.
   * @param issuer The issuer name.
   * @param ccy The currency.
   * @param curve The curve.
   */
  public void setCurve(final String issuer, final Currency ccy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuer, "issuer");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(Pair.of(issuer, ccy))) {
      throw new IllegalArgumentException("Issuer curve already set: " + issuer);
    }
    _issuerCurves.put(ObjectsPair.of(issuer, ccy), curve);
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   * TODO: REVIEW: Should we check that the curve are already present?
   */
  public void setAll(final MarketDiscountBundle other) {
    ArgumentChecker.notNull(other, "Market bundle");
    _discountingCurves.putAll(other._discountingCurves);
    _forwardIborCurves.putAll(other._forwardIborCurves);
    _forwardONCurves.putAll(other._forwardONCurves);
    _priceIndexCurves.putAll(other._priceIndexCurves);
    _issuerCurves.putAll(other._issuerCurves);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(curve, "curve");
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
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(curve, "curve");
    if (!_forwardIborCurves.containsKey(index)) {
      throw new IllegalArgumentException("Forward curve not in set: " + index);
    }
    _forwardIborCurves.put(index, curve);
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
