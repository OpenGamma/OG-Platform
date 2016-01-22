/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a present value curve sensitivity for multi-curves framework.
 */
public class MulticurveSensitivity implements Serializable {

  /**
   * The map containing the sensitivity to the yield (continuously compounded) (for discounting and issuer specific curves).
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityYieldDiscounting;
  /**
   * The map containing the sensitivity to forward curve. The sensitivity will depend on the way the curve is described (discount factor curve, forward rate, ...)
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<ForwardSensitivity>> _sensitivityForward;

  // TODO: Replace the curve names by some curve ID, maybe some UniqueIdentifiable objects

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public MulticurveSensitivity() {
    _sensitivityYieldDiscounting = new HashMap<>();
    _sensitivityForward = new HashMap<>();
  }

  /**
   * Constructor from a yield discounting map and a forward map. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   */
  private MulticurveSensitivity(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<ForwardSensitivity>> sensitivityForward) {
    _sensitivityYieldDiscounting = sensitivityYieldDiscounting;
    _sensitivityForward = sensitivityForward;
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static MulticurveSensitivity of(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<ForwardSensitivity>> sensitivityForward) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    return new MulticurveSensitivity(sensitivityYieldDiscounting, sensitivityForward);
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The map is used directly.
   * @param sensitivityYieldDiscounting The map.
   * @return The sensitivity.
   */
  public static MulticurveSensitivity ofYieldDiscounting(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    return new MulticurveSensitivity(sensitivityYieldDiscounting, new HashMap<String, List<ForwardSensitivity>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map. The map is used directly.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static MulticurveSensitivity ofForward(final Map<String, List<ForwardSensitivity>> sensitivityForward) {
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    return new MulticurveSensitivity(new HashMap<String, List<DoublesPair>>(), sensitivityForward);
  }

  public static MulticurveSensitivity ofForward(final String curveName, final ForwardSensitivity pointSensitivity) {
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(pointSensitivity);
    final HashMap<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(curveName, listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

  /**
   * Gets the discounting curve sensitivities.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getYieldDiscountingSensitivities() {
    return _sensitivityYieldDiscounting;
  }

  /**
   * Gets the forward curve sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<ForwardSensitivity>> getForwardSensitivities() {
    return _sensitivityForward;
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public MulticurveSensitivity plus(final MulticurveSensitivity other) {
    ArgumentChecker.notNull(other, "sensitivity");
    final Map<String, List<DoublesPair>> resultDsc = MulticurveSensitivityUtils.plus(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting);
    final Map<String, List<ForwardSensitivity>> resultFwd = MulticurveSensitivityUtils.plusFwd(_sensitivityForward, other._sensitivityForward);
    return new MulticurveSensitivity(resultDsc, resultFwd);
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public MulticurveSensitivity multipliedBy(final double factor) {
    final Map<String, List<DoublesPair>> resultDsc = MulticurveSensitivityUtils.multipliedBy(_sensitivityYieldDiscounting, factor);
    final Map<String, List<ForwardSensitivity>> resultFwd = MulticurveSensitivityUtils.multipliedByFwd(_sensitivityForward, factor);
    return new MulticurveSensitivity(resultDsc, resultFwd);
  }

  /**
   * Create a new sensitivity object by a product of two sensitivities
   * @param other The other sensitivity
   * @return The new sensitivity
   */
  public MulticurveSensitivity productOf(final MulticurveSensitivity other) {
    final Map<String, List<DoublesPair>> resultDsc = MulticurveSensitivityUtils.productOf(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting);
    final Map<String, List<ForwardSensitivity>> resultFwd = MulticurveSensitivityUtils.productOfFwd(_sensitivityForward, other._sensitivityForward);
    return new MulticurveSensitivity(resultDsc, resultFwd);
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times.
   * @return The cleaned sensitivity.
   */
  public MulticurveSensitivity cleaned() {
    final Map<String, List<DoublesPair>> resultDsc = MulticurveSensitivityUtils.cleaned(_sensitivityYieldDiscounting);
    final Map<String, List<ForwardSensitivity>> resultFwd = MulticurveSensitivityUtils.cleanedFwd(_sensitivityForward);
    return new MulticurveSensitivity(resultDsc, resultFwd);
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times. The total value below the tolerance threshold are removed.
   * @param tolerance The tolerance.
   * @return The cleaned sensitivity.
   */
  public MulticurveSensitivity cleaned(final double tolerance) {
    final Map<String, List<DoublesPair>> resultDsc = MulticurveSensitivityUtils.cleaned(_sensitivityYieldDiscounting, tolerance);
    final Map<String, List<ForwardSensitivity>> resultFwd = MulticurveSensitivityUtils.cleanedFwd(_sensitivityForward, tolerance);
    return new MulticurveSensitivity(resultDsc, resultFwd);
  }

  @Override
  public String toString() {
    return _sensitivityYieldDiscounting.toString() + "\n" + _sensitivityForward.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivityForward.hashCode();
    result = prime * result + _sensitivityYieldDiscounting.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MulticurveSensitivity)) {
      return false;
    }
    final MulticurveSensitivity other = (MulticurveSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivityForward, other._sensitivityForward)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting)) {
      return false;
    }
    return true;
  }

}
