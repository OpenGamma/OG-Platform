/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;
import static com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils.multiplySensitivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a the sensitivity of a some value (present value, par rate, etc) to the family of yield curves.
 */
public class InterestRateCurveSensitivity {

  /**
   * The map containing the sensitivity. The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivity;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public InterestRateCurveSensitivity() {
    _sensitivity = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a map of sensitivity.
   * @param sensitivity The map.
   */
  public InterestRateCurveSensitivity(Map<String, List<DoublesPair>> sensitivity) {
    Validate.notNull(sensitivity, "sensitivity");
    this._sensitivity = sensitivity;
  }

  /**
   * Gets the sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getSensitivities() {
    return _sensitivity;
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public InterestRateCurveSensitivity add(InterestRateCurveSensitivity other) {
    return new InterestRateCurveSensitivity(addSensitivity(_sensitivity, other._sensitivity));
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public InterestRateCurveSensitivity multiply(double factor) {
    return new InterestRateCurveSensitivity(multiplySensitivity(_sensitivity, factor));
  }

  /**
   * Return a clean sensitivity by sorting the times and adding the duplicate times.
   * @return The cleaned sensitivity.
   */
  public InterestRateCurveSensitivity clean() {
    return new InterestRateCurveSensitivity(InterestRateCurveSensitivityUtils.clean(_sensitivity, 0, 0));
  }

  /**
   * Return a clean sensitivity by sorting the times and adding the duplicate times.
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed 
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed 
   * @return The cleaned sensitivity.
   */
  public InterestRateCurveSensitivity clean(final double relTol, final double absTol) {
    return new InterestRateCurveSensitivity(InterestRateCurveSensitivityUtils.clean(_sensitivity, relTol, absTol));
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean compare(final InterestRateCurveSensitivity sensi1, final InterestRateCurveSensitivity sensi2, double tolerance) {
    return InterestRateCurveSensitivityUtils.compare(sensi1.getSensitivities(), sensi2.getSensitivities(), tolerance);
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
    InterestRateCurveSensitivity other = (InterestRateCurveSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
