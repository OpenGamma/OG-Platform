/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * A bundle of curves and forex exchange rates used for pricing. The curves are stored as a map <String, YieldAndDiscountCurve>.
 */
public class YieldCurveBundle {

  /**
   * The map used to store the curves.
   */
  private final Map<String, YieldAndDiscountCurve> _curves;
  /**
   * A map linking each curve in the bundle to its currency.
   */
  private final Map<String, Currency> _curveCurrency;
  /**
   * The matrix containing the exchange rates.
   */
  private final FXMatrix _fxMatrix;

  /**
   * Constructor. An empty map is created for the curves and an empty FXMatrix.
   */
  public YieldCurveBundle() {
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    _curveCurrency = new HashMap<String, Currency>();
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor from existing currency map and existing fxMatrix. A new curve map is created. The currency map and FXMatrix are directly used.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currencies.
   */
  public YieldCurveBundle(final FXMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    _curveCurrency = curveCurrency;
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from an existing map. A new map is created.
   * @param curvesMap The map.
   */
  public YieldCurveBundle(final Map<String, ? extends YieldAndDiscountCurve> curvesMap) {
    this();
    if (curvesMap != null) {
      Validate.noNullElements(curvesMap.keySet());
      Validate.noNullElements(curvesMap.values());
      _curves.putAll(curvesMap);
    }
  }

  /**
   * Constructor from an array of names and curves. The two arrays should have the same length. 
   * The names and curves are linked in the order of the arrays.
   * @param names The names.
   * @param curves The curves.
   */
  public YieldCurveBundle(final String[] names, final YieldAndDiscountCurve[] curves) {
    this();
    Validate.notNull(names);
    Validate.notNull(curves);
    Validate.isTrue(names.length == curves.length, "Different number of names and curves");
    Validate.noNullElements(names);
    Validate.noNullElements(curves);
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
    }
  }

  /**
   * Constructor from existing curve and currency maps and existing fxMatrix. A new map is created.
   * @param curvesMap The map of curves.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currencies.
   */
  public YieldCurveBundle(final Map<String, ? extends YieldAndDiscountCurve> curvesMap, final FXMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
    ArgumentChecker.isTrue(curvesMap.keySet().equals(curveCurrency.keySet()), "The maps for curves and currencies should have the same list of curves names.");
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>(curvesMap);
    _curveCurrency = new HashMap<String, Currency>(curveCurrency);
    _fxMatrix = new FXMatrix(fxMatrix);
  }

  /**
   * Constructor from an array of names and curves. The two arrays should have the same length. 
   * The names and curves are linked in the order of the arrays.
   * @param names The names.
   * @param curves The curves.
   * @param currencies The currency associated to each curve.
   */
  public YieldCurveBundle(final String[] names, final YieldAndDiscountCurve[] curves, final Currency[] currencies) {
    this();
    Validate.notNull(names);
    Validate.notNull(curves);
    Validate.isTrue(names.length == curves.length, "Different number of names and curves");
    Validate.isTrue(names.length == currencies.length, "Different number of names and currencies");
    Validate.noNullElements(names);
    Validate.noNullElements(curves);
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
      _curveCurrency.put(names[i], currencies[i]);
    }
  }

  /**
   * Constructor from a bundle. A new map is created.
   * @param bundle A bundle.
   */
  public YieldCurveBundle(YieldCurveBundle bundle) {
    Validate.notNull(bundle);
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>(bundle._curves);
    _curveCurrency = new HashMap<String, Currency>(bundle._curveCurrency);
    _fxMatrix = new FXMatrix(bundle._fxMatrix);
  }

  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names.
   * @return The bundle.
   */
  public YieldCurveBundle copy() {
    return new YieldCurveBundle(this);
  }

  /**
   * Add a new curve to the bundle. 
   * @param name The curve name.
   * @param curve The curve.
   * @throws IllegalArgumentException if curve name already present 
   */
  public void setCurve(final String name, final YieldAndDiscountCurve curve) {
    Validate.notNull(name, "name");
    Validate.notNull(curve, "curve");
    if (_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve already set: " + name);
    }
    _curves.put(name, curve);
  }

  /**
   * Replace an existing curve with a new one.
   * @param name The curve name.
   * @param curve The curve.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final String name, final YieldAndDiscountCurve curve) {
    Validate.notNull(name, "name");
    Validate.notNull(curve, "curve");
    if (!_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve not in set" + name);
    }
    _curves.put(name, curve);
  }

  public void addAll(final YieldCurveBundle other) {
    _curves.putAll(other._curves);
  }

  public YieldAndDiscountCurve getCurve(final String name) {
    if (_curves.containsKey(name)) {
      return _curves.get(name);
    }
    throw new IllegalArgumentException("Named yield curve not found: " + name);
  }

  /**
   * Returns the map with the curves.
   * @return The map.
   */
  public Map<String, YieldAndDiscountCurve> getCurvesMap() {
    return _curves;
  }

  /**
   * Returns the number of curves in the bundle.
   * @return The number of curves.
   */
  public int size() {
    return _curves.size();
  }

  public Set<String> getAllNames() {
    return _curves.keySet();
  }

  public Boolean containsName(final String curveName) {
    return _curves.containsKey(curveName);
  }

  /**
   * Gets map linking each curve in the bundle to its currency.
   * @return The map.
   */
  public Map<String, Currency> getCcyMap() {
    return _curveCurrency;
  }

  /**
   * Return the currency associated to a given curve.
   * @param curveName The curve name.
   * @return The currency.
   */
  public Currency getCurveCurrency(final String curveName) {
    Currency ccy = _curveCurrency.get(curveName);
    if (ccy == null) {
      throw new IllegalArgumentException("Named yield curve not in map: " + curveName);
    }
    return ccy;
  }

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _fxMatrix.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  public FXMatrix getFxRates() {
    return _fxMatrix;
  }

  /**
   * Convert a multiple currency amount into a amount in a given currency.
   * @param amount The multiple currency amount.
   * @param ccy The currency for the conversion.
   * @return The amount.
   */
  public CurrencyAmount convert(final MultipleCurrencyAmount amount, final Currency ccy) {
    return _fxMatrix.convert(amount, ccy);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curves == null) ? 0 : _curves.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveBundle other = (YieldCurveBundle) obj;
    return ObjectUtils.equals(_curves, other._curves);
  }

}
