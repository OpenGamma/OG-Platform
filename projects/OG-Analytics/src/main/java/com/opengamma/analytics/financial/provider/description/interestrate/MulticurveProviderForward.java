/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class MulticurveProviderForward implements MulticurveProviderInterface {

  /**
   * A map with one (discounting) curve by currency.
   */
  private final Map<Currency, YieldAndDiscountCurve> _discountingCurves;
  /**
   * A map with one (forward) curve by ON index.
   */
  private final Map<IndexON, YieldAndDiscountCurve> _forwardONCurves;
  /**
   * A map with one (forward) curve by Ibor/OIS index.
   * TODO: Replace the DoublesCurve by a more flexible object, similar to yieldAndDiscountCurve
   */
  private final Map<IborIndex, DoublesCurve> _forwardIborCurves;
  /**
   * The matrix containing the exchange rates.
   */
  private final FXMatrix _fxMatrix;
  /**
   * Map of all curves used in the provider.
   */
  private Map<String, Object> _allCurves;

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public MulticurveProviderForward() {
    // TODO: Do we need a LinkedHashMap or a more efficient Map could be used?
    _discountingCurves = new HashMap<Currency, YieldAndDiscountCurve>();
    _forwardIborCurves = new HashMap<IborIndex, DoublesCurve>();
    _forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>();
    _fxMatrix = new FXMatrix();
    setAllCurves();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public MulticurveProviderForward(final FXMatrix fxMatrix) {
    _discountingCurves = new HashMap<Currency, YieldAndDiscountCurve>();
    _forwardIborCurves = new HashMap<IborIndex, DoublesCurve>();
    _forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>();
    _fxMatrix = fxMatrix;
    setAllCurves();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param fxMatrix The FXMatrix.
   */
  public MulticurveProviderForward(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, DoublesCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final FXMatrix fxMatrix) {
    _discountingCurves = discountingCurves;
    _forwardIborCurves = forwardIborCurves;
    _forwardONCurves = forwardONCurves;
    _fxMatrix = fxMatrix;
    setAllCurves();
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return this;
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param market The existing market.
   */
  public MulticurveProviderForward(MulticurveProviderForward market) {
    _discountingCurves = market._discountingCurves;
    _forwardIborCurves = market._forwardIborCurves;
    _forwardONCurves = market._forwardONCurves;
    _fxMatrix = market._fxMatrix;
    setAllCurves();
  }

  @Override
  public MulticurveProviderForward copy() {
    final LinkedHashMap<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>(_discountingCurves);
    final LinkedHashMap<IborIndex, DoublesCurve> forwardIborCurves = new LinkedHashMap<IborIndex, DoublesCurve>(_forwardIborCurves);
    final LinkedHashMap<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>(_forwardONCurves);
    final FXMatrix fxMatrix = new FXMatrix(_fxMatrix);
    return new MulticurveProviderForward(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
  }

  private void setAllCurves() {
    _allCurves = new LinkedHashMap<String, Object>();
    final Set<Currency> ccySet = _discountingCurves.keySet();
    for (final Currency ccy : ccySet) {
      String name = _discountingCurves.get(ccy).getName();
      _allCurves.put(name, _discountingCurves.get(ccy));
    }
    final Set<IborIndex> indexSet = _forwardIborCurves.keySet();
    for (final IborIndex index : indexSet) {
      String name = _forwardIborCurves.get(index).getName();
      _allCurves.put(name, _forwardIborCurves.get(index));
    }
    final Set<IndexON> indexONSet = _forwardONCurves.keySet();
    for (final IndexON index : indexONSet) {
      String name = _forwardONCurves.get(index).getName();
      _allCurves.put(name, _forwardONCurves.get(index));
    }
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    final Object curveObject = _allCurves.get(name);
    ArgumentChecker.isTrue(curveObject instanceof YieldAndDiscountCurve, "Curve not a YieldAndDiscountCurve, can not computed sensitivity");
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
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
    final Object curveObject = _allCurves.get(name);
    if (curveObject instanceof YieldAndDiscountCurve) {
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
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
    } else {
      ArgumentChecker.isTrue(curveObject instanceof DoublesCurve, "Curve not a DoublesCurve, can not computed sensitivity");
      final DoublesCurve curve = (DoublesCurve) curveObject;
      final int nbParameters = curve.size();
      final double[] result = new double[nbParameters];
      if (pointSensitivity != null && pointSensitivity.size() > 0) {
        for (final ForwardSensitivity timeAndS : pointSensitivity) {
          Double[] sensiPtStart = curve.getYValueParameterSensitivity(timeAndS.getStartTime());
          // Implementation note: the forward rate are indexed by the start date.
          for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
            result[loopparam] += timeAndS.getValue() * sensiPtStart[loopparam];
          }
        }
      }
      return result;
    }
  }

  @Override
  public Integer getNumberOfParameters(String name) {
    final Object curveObject = _allCurves.get(name);
    if (curveObject instanceof YieldAndDiscountCurve) {
      return ((YieldAndDiscountCurve) curveObject).getNumberOfParameters();
    } else {
      ArgumentChecker.isTrue(curveObject instanceof DoublesCurve, "Curve not a DoublesCurve, can not computed sensitivity");
      return ((DoublesCurve) curveObject).size();
    }
  }

  @Override
  public List<String> getUnderlyingCurvesNames(String name) {
    final Object curveObject = _allCurves.get(name);
    if (curveObject instanceof YieldAndDiscountCurve) {
      return ((YieldAndDiscountCurve) curveObject).getUnderlyingCurvesNames();
    } else {
      ArgumentChecker.isTrue(curveObject instanceof DoublesCurve, "Curve not a DoublesCurve, can not computed sensitivity");
      List<String> list = new ArrayList<String>();
      list.add(name);
      return list;
    }
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
  public Set<Currency> getCurrencies() {
    return _discountingCurves.keySet();
  }

  @Override
  public double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor) {
    if (_forwardIborCurves.containsKey(index)) {
      return _forwardIborCurves.get(index).getYValue(startTime);
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
  }

  @Override
  public String getName(IborIndex index) {
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
  public double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor) {
    if (_forwardONCurves.containsKey(index)) {
      return (_forwardONCurves.get(index).getDiscountFactor(startTime) / _forwardONCurves.get(index).getDiscountFactor(endTime) - 1) / accrualFactor;
    }
    throw new IllegalArgumentException("Forward ON curve not found: " + index);
  }

  @Override
  public String getName(IndexON index) {
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
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final DoublesCurve curve) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(curve, "curve");
    if (_forwardIborCurves.containsKey(index)) {
      throw new IllegalArgumentException("Ibor index forward curve already set: " + index.toString());
    }
    _forwardIborCurves.put(index, curve);
    setAllCurves();
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   * TODO: REVIEW: Should we check that the curve are already present?
   */
  public void setAll(MulticurveProviderForward other) {
    ArgumentChecker.notNull(other, "Market bundle");
    _discountingCurves.putAll(other._discountingCurves);
    _forwardIborCurves.putAll(other._forwardIborCurves);
    _forwardONCurves.putAll(other._forwardONCurves);
    setAllCurves();
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
  public DoublesCurve getCurve(IborIndex index) {
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
  public YieldAndDiscountCurve getCurve(IndexON index) {
    if (_forwardONCurves.containsKey(index)) {
      return _forwardONCurves.get(index);
    }
    throw new IllegalArgumentException("Forward curve not found: " + index);
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
  public void replaceCurve(final IborIndex index, final DoublesCurve curve) {
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(curve, "curve");
    if (!_forwardIborCurves.containsKey(index)) {
      throw new IllegalArgumentException("Forward curve not in set: " + index);
    }
    _forwardIborCurves.put(index, curve);
    setAllCurves();
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

  public MulticurveProviderForward withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    // REVIEW: Is this too slow for the pricing of cash-flows?
    Map<Currency, YieldAndDiscountCurve> newDiscountCurves = new LinkedHashMap<Currency, YieldAndDiscountCurve>(_discountingCurves);
    newDiscountCurves.put(ccy, replacement); //TODO think about ccy not existing in current map
    MulticurveProviderForward decorated = new MulticurveProviderForward(newDiscountCurves, _forwardIborCurves, _forwardONCurves, _fxMatrix);
    return decorated;
  }

  public MulticurveProviderForward withForward(final IborIndex index, final DoublesCurve replacement) {
    Map<IborIndex, DoublesCurve> newForwardCurves = new LinkedHashMap<IborIndex, DoublesCurve>(_forwardIborCurves);
    newForwardCurves.put(index, replacement);
    MulticurveProviderForward decorated = new MulticurveProviderForward(_discountingCurves, newForwardCurves, _forwardONCurves, _fxMatrix);
    return decorated;
  }

  public MulticurveProviderForward withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    Map<IndexON, YieldAndDiscountCurve> newForwardCurves = new LinkedHashMap<IndexON, YieldAndDiscountCurve>(_forwardONCurves);
    newForwardCurves.put(index, replacement);
    MulticurveProviderForward decorated = new MulticurveProviderForward(_discountingCurves, _forwardIborCurves, newForwardCurves, _fxMatrix);
    return decorated;
  }

}
