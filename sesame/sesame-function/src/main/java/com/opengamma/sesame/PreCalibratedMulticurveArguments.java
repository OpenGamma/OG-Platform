/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Scenario arguments that provide pre-calibrated curves to {@link PreCalibratedMulticurveFn}.
 * <p>
 * This contains pre-calibrated multicurve bundles keyed by the bundle name.
 */
public class PreCalibratedMulticurveArguments
    implements ScenarioArgument<PreCalibratedMulticurveArguments, PreCalibratedMulticurveFn> {

  private final Map<String, MulticurveBundle> _multicurves;

  /**
   * @param multicurves multicurve bundles, keyed by bundle name
   */
  public PreCalibratedMulticurveArguments(Map<String, MulticurveBundle> multicurves) {
    _multicurves = ImmutableMap.copyOf(multicurves);
  }

  @Override
  public Class<PreCalibratedMulticurveFn> getFunctionType() {
    return PreCalibratedMulticurveFn.class;
  }

  /**
   * Returns the bundle with the specified name if available.
   *
   * @param name the bundle name
   * @return a success result with the named bundle, or a failure if no bundle is available with the specified name
   */
  public Result<MulticurveBundle> getMulticurveBundle(String name) {
    ArgumentChecker.notEmpty(name, "name");

    if (_multicurves.containsKey(name)) {
      return Result.success(_multicurves.get(name));
    } else {
      return Result.failure(FailureStatus.MISSING_DATA, "No multicurve available named {}", name);
    }
  }
}
