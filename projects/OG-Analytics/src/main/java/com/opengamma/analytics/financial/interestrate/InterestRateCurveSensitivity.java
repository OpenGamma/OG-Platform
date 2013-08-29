/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.multiplySensitivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class containing data describing the sensitivity of some analytic value (present value, par rate, etc.) to a family of yield curves.
 */
public class InterestRateCurveSensitivity {

  /**
   * The map containing the sensitivity. The map links the curve name to a list of pairs of cash flow time and sensitivity value.
   */
  private final Map<String, List<DoublesPair>> _sensitivity;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public InterestRateCurveSensitivity() {
    _sensitivity = new HashMap<>();
  }

  /**
   * Constructor from a map of sensitivity. The map links the curve name to a list of pairs of cash flow time and sensitivity value.
   * @param sensitivity The map, not null
   */
  public InterestRateCurveSensitivity(final Map<String, List<DoublesPair>> sensitivity) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    _sensitivity = new HashMap<>(sensitivity);
  }

  /**
   * Builder from a curve name and a list of sensitivities.
   * @param name The name, not null
   * @param sensitivityCurve The sensitivity as a list, not null
   * @return The interest rate curve sensitivity.
   */
  public static InterestRateCurveSensitivity of(final String name, final List<DoublesPair> sensitivityCurve) {
    ArgumentChecker.notNull(name, "Curve name");
    ArgumentChecker.notNull(sensitivityCurve, "sensitivity");
    final HashMap<String, List<DoublesPair>> ircs = new HashMap<>();
    ircs.put(name, sensitivityCurve);
    return new InterestRateCurveSensitivity(ircs);
  }

  /**
   * Gets the sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getSensitivities() {
    return _sensitivity;
  }

  /**
   * Returns the set of curve names in the interest rate sensitivities.
   * @return The set of curve names.
   */
  public Set<String> getCurves() {
    return _sensitivity.keySet();
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public InterestRateCurveSensitivity plus(final InterestRateCurveSensitivity other) {
    ArgumentChecker.notNull(other, "Curve sensitivity");
    return new InterestRateCurveSensitivity(addSensitivity(_sensitivity, other._sensitivity));
  }

  /**
   * Create a copy of the sensitivity and add a list representing the sensitivity to a specific curve.=.
   * @param curveName  The name of the curve the sensitivity of which is added. Not null.
   * @param list The sensitivity as a list. Not null.
   * @return The total sensitivity.
   */
  public InterestRateCurveSensitivity plus(final String curveName, final List<DoublesPair> list) {
    ArgumentChecker.notNull(curveName, "Curve name");
    ArgumentChecker.notNull(list, "Sensitivity as list");
    return new InterestRateCurveSensitivity(addSensitivity(_sensitivity, curveName, list));
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public InterestRateCurveSensitivity multipliedBy(final double factor) {
    return new InterestRateCurveSensitivity(multiplySensitivity(_sensitivity, factor));
  }

  //REVIEW emcleod 23/10/2012 These next two methods look like an argument for splitting this class into two, with
  // the first acting more like a list (i.e. not allowing duplicate times) and the type that has been cleaned looking
  // more like a set. Probably worth splitting the two.
  /**
   * Return a new sensitivity cleaned by sorting the times and adding the values at the duplicate times.
   * @return The cleaned sensitivity.
   */
  public InterestRateCurveSensitivity cleaned() {
    return new InterestRateCurveSensitivity(InterestRateCurveSensitivityUtils.clean(_sensitivity, 0, 0));
  }

  /**
   * Return a new sensitivity cleaned by sorting the times and adding the values at the duplicate times.
   * @param relTol Relative tolerance. If the net divided by gross sensitivity is less than this it is ignored/removed
   * @param absTol Absolute tolerance. If the net sensitivity is less than this it is ignored/removed
   * @return The cleaned sensitivity.
   */
  public InterestRateCurveSensitivity cleaned(final double relTol, final double absTol) {
    return new InterestRateCurveSensitivity(InterestRateCurveSensitivityUtils.clean(_sensitivity, relTol, absTol));
  }

  /**
   * Returns a map<String, Double> with the total sensitivity with respect to each curve.
   * @return The map.
   */
  public Map<String, Double> totalSensitivityByCurve() {
    final HashMap<String, Double> s = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : _sensitivity.entrySet()) {
      double total = 0.0;
      for (final DoublesPair p : entry.getValue()) {
        total += p.second;
      }
      s.put(entry.getKey(), total);
    }
    return s;
  }

  /**
   * Returns the total sensitivity to all curves.
   * @return The sensitivity.
   */
  public double totalSensitivity() {
    double total = 0.0;
    for (final List<DoublesPair> pairs : _sensitivity.values()) {
      for (final DoublesPair p : pairs) {
        total += p.second;
      }
    }
    return total;
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
    if (!(obj instanceof InterestRateCurveSensitivity)) {
      return false;
    }
    final InterestRateCurveSensitivity other = (InterestRateCurveSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
