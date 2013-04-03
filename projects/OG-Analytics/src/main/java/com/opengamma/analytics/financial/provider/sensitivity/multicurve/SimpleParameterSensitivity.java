/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing the sensitivity of value to specific parameters or market quotes and methods for manipulating these data.
 * The vector of sensitivities is stored with reference to a curve name.
 */
// TODO: The string should be replace by a curveId at some stage.
public class SimpleParameterSensitivity {
  /**
   * The map containing the sensitivity. The map links the curve name to a vector of sensitivities (sensitivities to parameters/inputs).
   */
  // TODO: Do we need a linked hash map?
  private final LinkedHashMap<String, DoubleMatrix1D> _sensitivity;

  /**
   * Default constructor, creating an empty LinkedHashMap for the sensitivity.
   */
  public SimpleParameterSensitivity() {
    _sensitivity = new LinkedHashMap<>();
  }

  /**
   * Constructor taking a map.
   * @param sensitivity The map with the sensitivities, not null. A new map is created.
   */
  public SimpleParameterSensitivity(final LinkedHashMap<String, DoubleMatrix1D> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    _sensitivity = new LinkedHashMap<>(sensitivity);
  }

  /**
   * Create a copy of the sensitivity and add a given named sensitivity to it. If the name / currency pair is in the map, the two sensitivity matrices are added.
   * Otherwise, a new entry is put into the map
   * @param name The name. Not null.
   * @param sensitivity The sensitivity to add, not null
   * @return The total sensitivity.
   */
  public SimpleParameterSensitivity plus(final String name, final DoubleMatrix1D sensitivity) {
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(sensitivity, "Matrix");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<String, DoubleMatrix1D> result = new LinkedHashMap<>();
    result.putAll(_sensitivity);
    if (result.containsKey(name)) {
      result.put(name, (DoubleMatrix1D) algebra.add(result.get(name), sensitivity));
    } else {
      result.put(name, sensitivity);
    }
    return new SimpleParameterSensitivity(result);
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public SimpleParameterSensitivity plus(final SimpleParameterSensitivity other) {
    ArgumentChecker.notNull(other, "Sensitivity to add");
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<String, DoubleMatrix1D> result = new LinkedHashMap<>();
    result.putAll(_sensitivity);
    for (final Map.Entry<String, DoubleMatrix1D> entry : other.getSensitivities().entrySet()) {
      final String name = entry.getKey();
      if (result.containsKey(name)) {
        result.put(name, (DoubleMatrix1D) algebra.add(result.get(name), entry.getValue()));
      } else {
        result.put(name, entry.getValue());
      }
    }
    return new SimpleParameterSensitivity(result);
  }

  /**
   * Create a copy of the object with all the sensitivities multiplied by a common factor.
   * @param factor The factor.
   * @return The multiplied sensitivity.
   */
  public SimpleParameterSensitivity multipliedBy(final double factor) {
    final MatrixAlgebra algebra = MatrixAlgebraFactory.COMMONS_ALGEBRA;
    final LinkedHashMap<String, DoubleMatrix1D> result = new LinkedHashMap<>();
    for (final String nameCcy : _sensitivity.keySet()) {
      result.put(nameCcy, (DoubleMatrix1D) algebra.scale(_sensitivity.get(nameCcy), factor));
    }
    return new SimpleParameterSensitivity(result);
  }

  /**
   * Returns the sensitivities wrapped in an unmodifiable map
   * @return The sensitivities
   */
  public Map<String, DoubleMatrix1D> getSensitivities() {
    return Collections.unmodifiableMap(_sensitivity);
  }

  /**
   * Returns the sensitivity for a given name.
   * @param name The name.
   * @return The sensitivity.
   */
  public DoubleMatrix1D getSensitivity(final String name) {
    ArgumentChecker.notNull(name, "Name");
    return _sensitivity.get(name);
  }

  /**
   * Returns a set with all the curve names.
   * @return The set of names.
   */
  public Set<String> getAllNames() {
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
    if (!(obj instanceof SimpleParameterSensitivity)) {
      return false;
    }
    final SimpleParameterSensitivity other = (SimpleParameterSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
