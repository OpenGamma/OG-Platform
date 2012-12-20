/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.sensitivity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Class containing the sensitivity of the present value to specific parameters or market quotes and methods for manipulating these data. 
 * The vector of sensitivities is stored with reference to a curve name and currency pair, with the sensitivity amounts assumed to be in the currency of the pair. 
 * The order of the sensitivities are important for curve calibration, so the data are stored in such a way as to preserve the order of insertion. 
 */
public class ParameterSensitivity {
  /**
   * The map containing the sensitivity. The map links a pair of curve name and currency to a vector of sensitivities (sensitivities to parameters/inputs).
   * The sensitivity is expressed in the currency of the pair. 
   */
  private final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> _sensitivity;

  /**
   * Default constructor, creating an empty LinkedHashMap for the sensitivity.
   */
  public ParameterSensitivity() {
    _sensitivity = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
  }

  /**
   * Constructor taking a map.
   * @param sensitivity The map with the sensitivities, not null. The map is copied. 
   */
  public ParameterSensitivity(final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    _sensitivity = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>(sensitivity);
  }

  /**
   * Static constructor from a map. The map is copied.
   * @param sensitivity A map of name / currency pairs to vector of sensitivities, not null 
   * @return An instance of ParameterSensitivity
   */
  public static ParameterSensitivity of(final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    return new ParameterSensitivity(sensitivity);
  }

  /**
   * Create a copy of the sensitivity and add a given named sensitivity to it. If the name / currency pair is in the map, the two sensitivity matrices are added.
   * Otherwise, a new entry is put into the map
   * @param nameCcy The name and the currency, not null
   * @param sensitivity The sensitivity to add, not null
   * @return The total sensitivity.
   */
  public ParameterSensitivity plus(final Pair<String, Currency> nameCcy, final DoubleMatrix1D sensitivity) {
    ArgumentChecker.notNull(nameCcy, "Name/currency");
    ArgumentChecker.notNull(sensitivity, "Matrix");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    result.putAll(_sensitivity);
    if (result.containsKey(nameCcy)) {
      result.put(nameCcy, (DoubleMatrix1D) algebra.add(result.get(nameCcy), sensitivity));
    } else {
      result.put(nameCcy, sensitivity);
    }
    return new ParameterSensitivity(result);
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public ParameterSensitivity plus(final ParameterSensitivity other) {
    ArgumentChecker.notNull(other, "Sensitivity to add");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    result.putAll(_sensitivity);
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : other.getSensitivities().entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      if (result.containsKey(nameCcy)) {
        result.put(nameCcy, (DoubleMatrix1D) algebra.add(result.get(nameCcy), entry.getValue()));
      } else {
        result.put(nameCcy, entry.getValue());
      }
    }
    return new ParameterSensitivity(result);
  }

  /**
   * Create a copy of the object with all the sensitivities multiplied by a common factor.
   * @param factor The factor.
   * @return The multiplied sensitivity.
   */
  public ParameterSensitivity multipliedBy(final double factor) {
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    for (final Pair<String, Currency> nameCcy : _sensitivity.keySet()) {
      result.put(nameCcy, (DoubleMatrix1D) algebra.scale(_sensitivity.get(nameCcy), factor));
    }
    return new ParameterSensitivity(result);
  }

  /**
   * Create a new parameter sensitivity with the new sensitivity with all the values in a common currency.
   * @param fxMatrix The matrix with relevant exchange rates, not null
   * @param ccy The currency in which the sensitivity is converted, not null
   * @return The converted sensitivity.
   */
  public ParameterSensitivity converted(final FXMatrix fxMatrix, final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(fxMatrix, "FX Matrix");
    ParameterSensitivity result = new ParameterSensitivity();
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : _sensitivity.entrySet()) {
      final Pair<String, Currency> nameCcy = entry.getKey();
      final double fxRate = fxMatrix.getFxRate(nameCcy.getSecond(), ccy);
      final Pair<String, Currency> nameCcyNew = Pair.of(nameCcy.getFirst(), ccy);
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
    return _sensitivity.get(Pair.of(name, ccy));
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
    if (!(obj instanceof ParameterSensitivity)) {
      return false;
    }
    final ParameterSensitivity other = (ParameterSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
