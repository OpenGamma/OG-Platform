/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.sensitivity;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a the sensitivity of the present value to specific parameters or market quotes.
 */
public class ParameterSensitivity {

  /**
   * The matrix algebra used for the sensitivities (mainly adding and multiplying by a scalar factor).
   */
  private static final CommonsMatrixAlgebra MATRIX = new CommonsMatrixAlgebra();

  /**
   * The map containing the sensitivity. The map linked a pair curve (String)/currency to vector of sensitivities (sensitivities to parameters/inputs).
   * The sensitivity is expressed in the currency of the pair. 
   */
  private final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> _sensitivity;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public ParameterSensitivity() {
    _sensitivity = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
  }

  /**
   * Private constructor.
   * @param sensitivity The map with the sensitivities. The map is used directly, not copied.
   */
  public ParameterSensitivity(LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity) {
    _sensitivity = sensitivity;
  }

  /**
   * Create a copy of the sensitivity and add a given named sensitivity to it.
   * @param nameCcy The name and the currency.
   * @param sensitivity The sensitivity to add.
   * @return The total sensitivity.
   */
  public ParameterSensitivity plus(final Pair<String, Currency> nameCcy, final DoubleMatrix1D sensitivity) {
    ArgumentChecker.notNull(nameCcy, "Name/currency");
    ArgumentChecker.notNull(sensitivity, "Matrix");
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    result.putAll(_sensitivity);
    if (result.containsKey(nameCcy)) {
      result.put(nameCcy, (DoubleMatrix1D) MATRIX.add(result.get(nameCcy), sensitivity));
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
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    result.putAll(_sensitivity);
    for (final Pair<String, Currency> nameCcy : other._sensitivity.keySet()) {
      if (result.containsKey(nameCcy)) {
        result.put(nameCcy, (DoubleMatrix1D) MATRIX.add(result.get(nameCcy), other._sensitivity.get(nameCcy)));
      } else {
        result.put(nameCcy, other._sensitivity.get(nameCcy));
      }
    }
    return new ParameterSensitivity(result);
  }

  /**
   * Create a copy of the object with all the sensitivities multiplied by a common factor.
   * @param factor The factor.
   * @return The multiplied sensitivity.
   */
  public ParameterSensitivity multiplyBy(final double factor) {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    for (final Pair<String, Currency> nameCcy : _sensitivity.keySet()) {
      result.put(nameCcy, (DoubleMatrix1D) MATRIX.scale(_sensitivity.get(nameCcy), factor));
    }
    return new ParameterSensitivity(result);
  }

  /**
   * Create a new parameter sensitivity with the new sensitivity with all the values in a common currency.
   * @param fxMatrix The matrix with relevant exchange rates.
   * @param ccy The currency in which the sensitivity is converted.
   * @return The converted sensitivity.
   */
  public ParameterSensitivity convert(final FXMatrix fxMatrix, final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(fxMatrix, "FX Matrix");
    ParameterSensitivity result = new ParameterSensitivity();
    for (final Pair<String, Currency> nameCcy : _sensitivity.keySet()) {
      final double fxRate = fxMatrix.getFxRate(nameCcy.getSecond(), ccy);
      Pair<String, Currency> nameCcyNew = new ObjectsPair<String, Currency>(nameCcy.getFirst(), ccy);
      DoubleMatrix1D sensiNew = (DoubleMatrix1D) MATRIX.scale(_sensitivity.get(nameCcy), fxRate);
      result = result.plus(nameCcyNew, sensiNew);
    }
    return result;
  }

  /**
   * Convert the parameter sensitivity into a matrix (DoubleMatrix1D). 
   * The matrix is composed of the sensitivity vectors (currency is ignored) one after the other. 
   * The matrix order is the natural one for the <String, Currency> key (as implemented in TreeSet). 
   * TODO: REVIEW if this is the correct order. Do we need externally provided list of name/currency in case some are not present in the sensitivity?
   * @return The sensitivity matrix.
   */
  public DoubleMatrix1D toMatrix() {
    double[] psArray = new double[0];
    Set<Pair<String, Currency>> pairs = _sensitivity.keySet();
    TreeSet<Pair<String, Currency>> pairsOrdered = new TreeSet<Pair<String, Currency>>(pairs);
    for (final Pair<String, Currency> nameCcy : pairsOrdered) {
      psArray = ArrayUtils.addAll(psArray, _sensitivity.get(nameCcy).getData());
    }
    return new DoubleMatrix1D(psArray);
  }

  /**
   * Returns the sensitivity for a given name/currency pair.
   * @param nameCcy The name and the currency.
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
    return _sensitivity.get(new ObjectsPair<String, Currency>(name, ccy));
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

  /**
   * Compare two sensitivities with a given tolerance.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean compare(final ParameterSensitivity sensi1, final ParameterSensitivity sensi2, final double tolerance) {
    Validate.notNull(sensi1, "sensitivity");
    Validate.notNull(sensi2, "sensitivity");
    if (!sensi1._sensitivity.keySet().equals(sensi1._sensitivity.keySet())) {
      return false;
    }
    for (final Pair<String, Currency> nameCcy : sensi1._sensitivity.keySet()) {
      if (sensi2._sensitivity.containsKey(nameCcy)) {
        if (MATRIX.getNormInfinity(MATRIX.add(sensi1._sensitivity.get(nameCcy), MATRIX.scale(sensi2._sensitivity.get(nameCcy), -1.0))) > tolerance) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ParameterSensitivity other = (ParameterSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
