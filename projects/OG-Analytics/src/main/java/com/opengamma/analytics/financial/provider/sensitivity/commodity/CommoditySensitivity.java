/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.commodity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivityUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a present value curve sensitivity for commodity and multi-curves framework.
 */
public class CommoditySensitivity {

  /**
   * The multi-curves sensitivity object.
   */
  private final MulticurveSensitivity _multicurveSensitivity;
  /**
   * The map containing the sensitivity to the c.
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityCommodityForwardCurve;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public CommoditySensitivity() {
    _multicurveSensitivity = new MulticurveSensitivity();
    _sensitivityCommodityForwardCurve = new HashMap<>();
  }

  private CommoditySensitivity(final MulticurveSensitivity multicurveSensitivity, final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {
    _multicurveSensitivity = multicurveSensitivity;
    _sensitivityCommodityForwardCurve = sensitivityCommodityForwardCurve;
  }

  /**
   * Constructor from a yield discounting map, a forward map and a price index curve of sensitivity. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityCommodityForwardCurve The map.
   */
  private CommoditySensitivity(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<ForwardSensitivity>> sensitivityForward,
      final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {
    _multicurveSensitivity = MulticurveSensitivity.of(sensitivityYieldDiscounting, sensitivityForward);
    _sensitivityCommodityForwardCurve = sensitivityCommodityForwardCurve;
  }

  /**
   * Constructor from a multicurveSensitivity and a sensitivityPriceCurve. The maps are used directly.
   * @param multicurveSensitivity The multicurveSensitivity.
   * @param sensitivityCommodityForwardCurve The map.
   * @return The sensitivity.
   */
  public static CommoditySensitivity of(final MulticurveSensitivity multicurveSensitivity, final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {

    ArgumentChecker.notNull(multicurveSensitivity, "multicurve sensitivity");
    ArgumentChecker.notNull(sensitivityCommodityForwardCurve, "Sensitivity commodity forward curve");
    return new CommoditySensitivity(multicurveSensitivity, sensitivityCommodityForwardCurve);
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityCommodityForwardCurve The map.
   * @return The sensitivity.
   */
  public static CommoditySensitivity of(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<ForwardSensitivity>> sensitivityForward,
      final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    ArgumentChecker.notNull(sensitivityCommodityForwardCurve, "Sensitivity commodity forward curve");
    return new CommoditySensitivity(sensitivityYieldDiscounting, sensitivityForward, sensitivityCommodityForwardCurve);
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The map is used directly.
   * @param sensitivityYieldDiscounting The map.
   * @return The sensitivity.
   */
  public static CommoditySensitivity ofYieldDiscounting(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    return new CommoditySensitivity(sensitivityYieldDiscounting, new HashMap<String, List<ForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a price map. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityCommodityForwardCurve The map.
   * @return The sensitivity.
   */
  public static CommoditySensitivity of(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting,
      final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityCommodityForwardCurve, "Sensitivity commodity forward curve");
    return new CommoditySensitivity(sensitivityYieldDiscounting, new HashMap<String, List<ForwardSensitivity>>(), sensitivityCommodityForwardCurve);
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The map is used directly.
   * @param sensitivityCommodityForwardCurve The map.
   * @return The sensitivity.
   */
  public static CommoditySensitivity ofCommodityForwardValue(final Map<String, List<DoublesPair>> sensitivityCommodityForwardCurve) {
    ArgumentChecker.notNull(sensitivityCommodityForwardCurve, "Sensitivity commodity forward index");
    return new CommoditySensitivity(new HashMap<String, List<DoublesPair>>(), new HashMap<String, List<ForwardSensitivity>>(), sensitivityCommodityForwardCurve);
  }

  /**
   * Gets the MulticurveSensitivity.
   * @return The sensitivity map
   */
  public MulticurveSensitivity getMulticurveSensitivity() {
    return _multicurveSensitivity;
  }

  /**
   * Gets the discounting curve sensitivities.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getYieldDiscountingSensitivities() {
    return _multicurveSensitivity.getYieldDiscountingSensitivities();
  }

  /**
   * Gets the forward curve sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<ForwardSensitivity>> getForwardSensitivities() {
    return _multicurveSensitivity.getForwardSensitivities();
  }

  /**
   * Gets the commodity forward curve sensitivity map.
   * @return The sensitivity map wrapped in an unmodifiable map
   */
  public Map<String, List<DoublesPair>> getCommodityForwardCurveSensitivities() {
    return Collections.unmodifiableMap(_sensitivityCommodityForwardCurve);
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public CommoditySensitivity plus(final CommoditySensitivity other) {
    ArgumentChecker.notNull(other, "sensitivity");
    final MulticurveSensitivity resultMulticurve = _multicurveSensitivity.plus(other._multicurveSensitivity);
    final Map<String, List<DoublesPair>> resultPrice = MulticurveSensitivityUtils.plus(_sensitivityCommodityForwardCurve, other._sensitivityCommodityForwardCurve);
    return new CommoditySensitivity(resultMulticurve, resultPrice);
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public CommoditySensitivity multipliedBy(final double factor) {
    final MulticurveSensitivity resultMulticurve = _multicurveSensitivity.multipliedBy(factor);
    final Map<String, List<DoublesPair>> resultPrice = MulticurveSensitivityUtils.multipliedBy(_sensitivityCommodityForwardCurve, factor);
    return new CommoditySensitivity(resultMulticurve, resultPrice);
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times.
   * @return The cleaned sensitivity.
   */
  public CommoditySensitivity cleaned() {
    final MulticurveSensitivity resultMulticurve = _multicurveSensitivity.cleaned();
    final Map<String, List<DoublesPair>> resultPrice = MulticurveSensitivityUtils.cleaned(_sensitivityCommodityForwardCurve);
    return new CommoditySensitivity(resultMulticurve, resultPrice);
  }

  /**
   * Gets the multicurve sensitivities
   * @return The multicurve sensitivities
   */
  public MulticurveSensitivity getMulticurveSensitivities() {
    return _multicurveSensitivity;
  }

}
