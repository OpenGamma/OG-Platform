/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a provider with discounting, forward and Black cap/floor parameters.
 */
public class BlackSmileCapProvider implements BlackSmileCapProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurves;
  /**
   * The Black parameters.
   */
  private final BlackSmileCapParameters _parameters;

  /**
   * Constructor.
   * @param multicurves The multi-curves provider, not null
   * @param parameters The Black parameters, not null
   */
  public BlackSmileCapProvider(final MulticurveProviderInterface multicurves, final BlackSmileCapParameters parameters) {
    ArgumentChecker.notNull(multicurves, "multicurvesProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    _multicurves = multicurves;
    _parameters = parameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurves;
  }

  @Override
  public BlackSmileCapProviderInterface copy() {
    final MulticurveProviderInterface multicurves = _multicurves.copy();
    return new BlackSmileCapProvider(multicurves, _parameters);
  }

  @Override
  public BlackSmileCapParameters getBlackParameters() {
    return _parameters;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurves.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurves.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurves.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _multicurves.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSmileCapProvider)) {
      return false;
    }
    final BlackSmileCapProvider other = (BlackSmileCapProvider) obj;
    if (!ObjectUtils.equals(_multicurves, other._multicurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
