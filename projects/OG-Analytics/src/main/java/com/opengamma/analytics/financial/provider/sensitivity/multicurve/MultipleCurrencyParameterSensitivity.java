/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class containing the sensitivity of the present value to specific parameters or market quotes and methods for manipulating these data.
 * The vector of sensitivities is stored with reference to a curve name and currency pair, with the sensitivity amounts assumed to be in the currency of the pair.
 */
public class MultipleCurrencyParameterSensitivity {
  /**
   * The map containing the sensitivity. The map links a pair of curve name and currency to a vector of sensitivities (sensitivities to parameters/inputs).
   * The sensitivity is expressed in the currency of the pair.
   */
  private final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> _sensitivity;

  /**
   * Default constructor, creating an empty LinkedHashMap for the sensitivity.
   */
  public MultipleCurrencyParameterSensitivity() {
    _sensitivity = new LinkedHashMap<>();
  }

  /**
   * Constructor taking a map. A new map is created.
   * @param sensitivity The map with the sensitivities, not null. The map is copied.
   */
  public MultipleCurrencyParameterSensitivity(final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    _sensitivity = new LinkedHashMap<>(sensitivity);
  }

  /**
   * Static constructor from a map. A new map is created.
   * @param sensitivity A map of name / currency pairs to vector of sensitivities, not null
   * @return An new instance of ParameterSensitivity
   */
  public static MultipleCurrencyParameterSensitivity of(final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    return new MultipleCurrencyParameterSensitivity(sensitivity);
  }

  /**
   * Constructor from a simple sensitivity and a currency.
   * @param single The Simple parameter sensitivity
   * @param ccy The currency.
   * @return The multiple currency sensitivity.
   */
  public static MultipleCurrencyParameterSensitivity of(final SimpleParameterSensitivity single, final Currency ccy) {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensi = new LinkedHashMap<>();
    for (final String name : single.getAllNames()) {
      sensi.put(Pairs.of(name, ccy), single.getSensitivity(name));
    }
    return MultipleCurrencyParameterSensitivity.of(sensi);
  }

  /**
   * Create a copy of the sensitivity and add a given named sensitivity to it. If the name / currency pair is in the map, the two sensitivity matrices are added.
   * Otherwise, a new entry is put into the map
   * @param nameCcy The name and the currency, not null
   * @param sensitivity The sensitivity to add, not null
   * @return The total sensitivity.
   */
  public MultipleCurrencyParameterSensitivity plus(final Pair<String, Currency> nameCcy, final DoubleMatrix1D sensitivity) {
    ArgumentChecker.notNull(nameCcy, "Name/currency");
    ArgumentChecker.notNull(sensitivity, "Matrix");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<>();
    result.putAll(_sensitivity);
    if (result.containsKey(nameCcy)) {
      result.put(nameCcy, (DoubleMatrix1D) algebra.add(result.get(nameCcy), sensitivity));
    } else {
      result.put(nameCcy, sensitivity);
    }
    return new MultipleCurrencyParameterSensitivity(result);
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public MultipleCurrencyParameterSensitivity plus(final MultipleCurrencyParameterSensitivity other) {
    ArgumentChecker.notNull(other, "Sensitivity to add");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<>();
    result.putAll(_sensitivity);
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : other.getSensitivities().entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      if (result.containsKey(nameCcy)) {
        result.put(nameCcy, (DoubleMatrix1D) algebra.add(result.get(nameCcy), entry.getValue()));
      } else {
        result.put(nameCcy, entry.getValue());
      }
    }
    return new MultipleCurrencyParameterSensitivity(result);
  }

  /**
   * Create a copy of the object with all the sensitivities multiplied by a common factor.
   * @param factor The factor.
   * @return The multiplied sensitivity.
   */
  public MultipleCurrencyParameterSensitivity multipliedBy(final double factor) {
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<>();
    for (final Pair<String, Currency> nameCcy : _sensitivity.keySet()) {
      result.put(nameCcy, (DoubleMatrix1D) algebra.scale(_sensitivity.get(nameCcy), factor));
    }
    return new MultipleCurrencyParameterSensitivity(result);
  }

  /**
   * Create a new parameter sensitivity with the new sensitivity with all the values in a common currency.
   * @param fxMatrix The matrix with relevant exchange rates, not null
   * @param ccy The currency in which the sensitivity is converted, not null
   * @return The converted sensitivity.
   */
  public MultipleCurrencyParameterSensitivity converted(final FXMatrix fxMatrix, final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(fxMatrix, "FX Matrix");
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : _sensitivity.entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      final double fxRate = fxMatrix.getFxRate(nameCcy.getSecond(), ccy);
      final Pair<String, Currency> nameCcyNew = Pairs.of(nameCcy.getFirst(), ccy);
      final DoubleMatrix1D sensitivityNew = (DoubleMatrix1D) algebra.scale(entry.getValue(), fxRate);
      result = result.plus(nameCcyNew, sensitivityNew);
    }
    return result;
  }

  /**
   * Returns the sensitivities wrapped in an unmodifiable map
   * @return The sensitivities
   */
  public Map<Pair<String, Currency>, DoubleMatrix1D> getSensitivities() {
    return Collections.unmodifiableMap(_sensitivity);
  }

  /**
   * Returns the sensitivities for a particular curve name. An unmodifiable map
   * of Currency -> Sensitivities will be returned. Note that this implementation
   * will not be efficient if there are a large number of curves.
   *
   * @param name the name of the curve to get sensitivities for
   * @return map of sensitivities by currency
   */
  public Map<Currency, DoubleMatrix1D> getSensitivityByName(String name) {
    Map<Currency, DoubleMatrix1D> matches = new HashMap<>();
    for (Entry<Pair<String, Currency>, DoubleMatrix1D> entry : _sensitivity.entrySet()) {
      final Pair<String, Currency> curveName = entry.getKey();
      if (curveName.getFirst().equals(name)) {
        matches.put(curveName.getSecond(), entry.getValue());
      }
    }
    return Collections.unmodifiableMap(matches);
  }

  /**
   * Returns the sensitivity for a given name/currency pair.
   * @param nameCcy The name and the currency, not null
   * @return The sensitivity.
   */
  public DoubleMatrix1D getSensitivity(final Pair<String, Currency> nameCcy) {
    ArgumentChecker.notNull(nameCcy, "Name");
    return _sensitivity.get(nameCcy);
  }

  /**
   * Returns the sensitivity for a given name.
   * @param name The name.
   * @param ccy The currency.
   * @return The sensitivity.
   */
  public DoubleMatrix1D getSensitivity(final String name, final Currency ccy) {
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(ccy, "Currency");
    return _sensitivity.get(Pairs.of(name, ccy));
  }

  /**
   * Returns a map<Pair<String, Currency>, Double> with the total sensitivity with respect to each curve/currency.
   * @return The map.
   */
  public Map<Pair<String, Currency>, Double> totalSensitivityByCurveCurrency() {
    final HashMap<Pair<String, Currency>, Double> s = new HashMap<>();
    for (final Entry<Pair<String, Currency>, DoubleMatrix1D> entry : _sensitivity.entrySet()) {
      double total = 0.0;
      for (int loopi = 0; loopi < entry.getValue().getNumberOfElements(); loopi++) {
        total += entry.getValue().getEntry(loopi);
      }
      s.put(entry.getKey(), total);
    }
    return s;
  }

  /**
   * Returns the total sensitivity to all curves, in a given currency.
   * @param fxMatrix The FX matrix will the exchange rates.
   * @param ccy The currency for the conversion.
   * @return The sensitivity.
   */
  public double totalSensitivity(final FXMatrix fxMatrix, final Currency ccy) {
    double total = 0.0;
    for (final Entry<Pair<String, Currency>, DoubleMatrix1D> entry : _sensitivity.entrySet()) {
      final double fx = fxMatrix.getFxRate(entry.getKey().getSecond(), ccy);
      for (int loopi = 0; loopi < entry.getValue().getNumberOfElements(); loopi++) {
        total += entry.getValue().getEntry(loopi) * fx;
      }
    }
    return total;
  }

  /**
   * Returns a set with all the curve names.
   * @return The set of names.
   */
  public Set<Pair<String, Currency>> getAllNamesCurrency() {
    return _sensitivity.keySet();
  }

  @Override
  public String toString() {
    return _sensitivity.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MultipleCurrencyParameterSensitivity)) {
      return false;
    }
    final MultipleCurrencyParameterSensitivity other = (MultipleCurrencyParameterSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
