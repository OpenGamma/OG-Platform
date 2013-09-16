/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in {@link YieldAndDiscountCurve}.
 */
public class MulticurveProviderDiscount implements MulticurveProviderInterface {

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
   * The matrix containing the exchange rates.
   */
  private FXMatrix _fxMatrix;
  /**
   * Map of all curves used in the provider. The order is ???
   */
  private Map<String, YieldAndDiscountCurve> _allCurves;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public MulticurveProviderDiscount() {
    // TODO: Do we need a LinkedHashMap or a more efficient Map could be used?
    _discountingCurves = new LinkedHashMap<>();
    _forwardIborCurves = new LinkedHashMap<>();
    _forwardONCurves = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    setAllCurves();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix, not null
   */
  public MulticurveProviderDiscount(final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(fxMatrix, "FX matrix");
    _discountingCurves = new LinkedHashMap<>();
    _forwardIborCurves = new LinkedHashMap<>();
    _forwardONCurves = new LinkedHashMap<>();
    _fxMatrix = fxMatrix;
    setAllCurves();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency, not null
   * @param forwardIborCurves A map with one (forward) curve by Ibor index, not null
   * @param forwardONCurves A map with one (forward) curve by ON index, not null
   * @param fxMatrix The FXMatrix, not null
   */
  public MulticurveProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(discountingCurves, "discounting curve");
    ArgumentChecker.notNull(forwardIborCurves, "forward ibor curve");
    ArgumentChecker.notNull(forwardONCurves, "forward overnight curve");
    ArgumentChecker.notNull(fxMatrix, "FX matrix");
    _discountingCurves = discountingCurves;
    _forwardIborCurves = forwardIborCurves;
    _forwardONCurves = forwardONCurves;
    _fxMatrix = fxMatrix;
    setAllCurves();
  }

  /**
   * Constructor from exiting maps. The given maps are used for the new market (the same maps are used, not copied).
   * @param market The existing market, not null
   */
  public MulticurveProviderDiscount(final MulticurveProviderDiscount market) {
    ArgumentChecker.notNull(market, "market");
    _discountingCurves = market._discountingCurves;
    _forwardIborCurves = market._forwardIborCurves;
    _forwardONCurves = market._forwardONCurves;
    _fxMatrix = market._fxMatrix;
    setAllCurves();
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return this;
  }

  @Override
  public MulticurveProviderDiscount copy() {
    final LinkedHashMap<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>(_discountingCurves);
    final LinkedHashMap<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>(_forwardIborCurves);
    final LinkedHashMap<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>(_forwardONCurves);
    final FXMatrix fxMatrix = new FXMatrix(_fxMatrix);
    return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
  }

  /**
   * Adds all curves to a single map containing (curve name, curve) elements.
   */
  private void setAllCurves() {
    _allCurves = new LinkedHashMap<>();
    final Set<Currency> ccySet = _discountingCurves.keySet();
    for (final Currency ccy : ccySet) {
      final String name = _discountingCurves.get(ccy).getName();
      _allCurves.put(name, _discountingCurves.get(ccy));
    }
    final Set<IborIndex> indexSet = _forwardIborCurves.keySet();
    for (final IborIndex index : indexSet) {
      final String name = _forwardIborCurves.get(index).getName();
      _allCurves.put(name, _forwardIborCurves.get(index));
    }
    final Set<IndexON> indexONSet = _forwardONCurves.keySet();
    for (final IndexON index : indexONSet) {
      final String name = _forwardONCurves.get(index).getName();
      _allCurves.put(name, _forwardONCurves.get(index));
    }
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    final YieldAndDiscountCurve curve = _allCurves.get(name);
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
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    final YieldAndDiscountCurve curve = _allCurves.get(name);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final ForwardSensitivity timeAndS : pointSensitivity) {
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
  public Integer getNumberOfParameters(final String name) {
    return _allCurves.get(name).getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _allCurves.get(name).getUnderlyingCurvesNames();
  }

  /**
   * Gets a named curve.
   * @param name The name
   * @return The curve, null if not found
   */
  public YieldAndDiscountCurve getCurve(final String name) {
    return _allCurves.get(name);
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
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    if (_forwardIborCurves.containsKey(index)) {
      return (_forwardIborCurves.get(index).getDiscountFactor(startTime) / _forwardIborCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
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
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    if (_forwardONCurves.containsKey(index)) {
      return (_forwardONCurves.get(index).getDiscountFactor(startTime) / _forwardONCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward ON curve not found: " + index);
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
   * Returns all curves names. The order is the natural order of String.
   * @return The names.
   */
  @Override
  public Set<String> getAllNames() {
    return _allCurves.keySet();
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
    setAllCurves();
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
    setAllCurves();
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
    setAllCurves();
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   */
  //TODO: REVIEW: Should we check that the curve are already present?
  public void setAll(final MulticurveProviderDiscount other) {
    ArgumentChecker.notNull(other, "Market bundle");
    _discountingCurves.putAll(other._discountingCurves);
    _forwardIborCurves.putAll(other._forwardIborCurves);
    _forwardONCurves.putAll(other._forwardONCurves);
    setAllCurves();
  }

  /**
   * Sets the FX matrix.
   * @param fxMatrix The FX matrix, not null
   */
  public void setForexMatrix(final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(fxMatrix, "FX matrix");
    _fxMatrix = fxMatrix;
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
    setAllCurves();
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
    setAllCurves();
  }

  /**
   * Replaces the forward curve for a given ON index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(curve, "curve");
    if (!_forwardONCurves.containsKey(index)) {
      throw new IllegalArgumentException("Forward curve not in set: " + index);
    }
    _forwardONCurves.put(index, curve);
    setAllCurves();
  }

  /**
   * Remove the discounting curve for a given currency.
   * @param ccy The currency.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void removeCurve(final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    if (!_discountingCurves.containsKey(ccy)) {
      throw new IllegalArgumentException("Currency discounting curve not in set: " + ccy);
    }
    _discountingCurves.remove(ccy);
    setAllCurves();
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

  /**
   * Returns an unmodifiable copy of the currency to discounting curves map.
   * @return The discounting curve map
   */
  public Map<Currency, YieldAndDiscountCurve> getDiscountingCurves() {
    return Collections.unmodifiableMap(_discountingCurves);
  }

  /**
   * Returns an unmodifiable copy of the ibor index to forward curves map.
   * @return The forward ibor curve map
   */
  public Map<IborIndex, YieldAndDiscountCurve> getForwardIborCurves() {
    return Collections.unmodifiableMap(_forwardIborCurves);
  }

  /**
   * Returns an unmodifiable copy of the overnight index to forward curves map.
   * @return The forward overnight curve map
   */
  public Map<IndexON, YieldAndDiscountCurve> getForwardONCurves() {
    return Collections.unmodifiableMap(_forwardONCurves);
  }

  /**
   * Returns a new provider with the discounting curve for a particular currency replaced.
   * @param ccy The currency, not null
   * @param replacement The replacement discounting curve, not null
   * @return A new provider with the discounting curve for the currency replaced by the input curve.
   */
  public MulticurveProviderDiscount withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    ArgumentChecker.notNull(ccy, "currency");
    ArgumentChecker.notNull(replacement, "replacement");
    // REVIEW: Is this too slow for the pricing of cash-flows?
    final Map<Currency, YieldAndDiscountCurve> newDiscountCurves = new LinkedHashMap<>(_discountingCurves);
    newDiscountCurves.put(ccy, replacement); //TODO think about ccy not existing in current map
    final MulticurveProviderDiscount decorated = new MulticurveProviderDiscount(newDiscountCurves, _forwardIborCurves, _forwardONCurves, _fxMatrix);
    return decorated;
  }

  /**
   * Returns a new provider with the curve for a particular ibor index replaced.
   * @param index The ibor index, not null
   * @param replacement The replacement ibor index curve, not null
   * @return A new provider with the ibor index curve replaced by the input curve.
   */
  public MulticurveProviderDiscount withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(replacement, "replacement");
    final Map<IborIndex, YieldAndDiscountCurve> newForwardCurves = new LinkedHashMap<>(_forwardIborCurves);
    newForwardCurves.put(index, replacement);
    final MulticurveProviderDiscount decorated = new MulticurveProviderDiscount(_discountingCurves, newForwardCurves, _forwardONCurves, _fxMatrix);
    return decorated;
  }

  /**
   * Returns a new provider with the curve for a particular overnight index replaced.
   * @param index The overnight index, not null
   * @param replacement The replacement overnight index curve, not null
   * @return A new provider with the overnight index curve replaced by the input curve.
   */
  public MulticurveProviderDiscount withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(replacement, "replacement");
    final Map<IndexON, YieldAndDiscountCurve> newForwardCurves = new LinkedHashMap<>(_forwardONCurves);
    newForwardCurves.put(index, replacement);
    final MulticurveProviderDiscount decorated = new MulticurveProviderDiscount(_discountingCurves, _forwardIborCurves, newForwardCurves, _fxMatrix);
    return decorated;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _discountingCurves.hashCode();
    result = prime * result + _forwardIborCurves.hashCode();
    result = prime * result + _forwardONCurves.hashCode();
    result = prime * result + _fxMatrix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MulticurveProviderDiscount)) {
      return false;
    }
    final MulticurveProviderDiscount other = (MulticurveProviderDiscount) obj;
    if (!ObjectUtils.equals(_discountingCurves, other._discountingCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardIborCurves, other._forwardIborCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardONCurves, other._forwardONCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_fxMatrix, other._fxMatrix)) {
      return false;
    }
    return true;
  }

}
