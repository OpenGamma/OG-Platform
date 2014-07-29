/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class containing curve and volatility data sufficient to price swaptions using the Black method.
 * The forward rates are computed using discount factors.
 */
public class BlackSwaptionFlatProvider implements BlackSwaptionFlatProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multiCurveProvider;
  /**
   * The Black volatility surface for swaption.
   */
  private final BlackFlatSwaptionParameters _blackParameters;

  /**
   * Constructor.
   * @param multicurves The multi-curves provider, not null
   * @param blackParameters The Black parameters, not null
   */
  public BlackSwaptionFlatProvider(final MulticurveProviderInterface multicurves, final BlackFlatSwaptionParameters blackParameters) {
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(blackParameters, "blackParameters");
    _multiCurveProvider = multicurves;
    _blackParameters = blackParameters;
  }

  @Override
  public BlackSwaptionFlatProviderInterface copy() {
    final MulticurveProviderInterface curves = _multiCurveProvider.copy();
    final BlackFlatSwaptionParameters black = _blackParameters; //TODO copy these parameters
    return new BlackSwaptionFlatProvider(curves, black);
  }

  @Override
  public BlackFlatSwaptionParameters getBlackParameters() {
    return _blackParameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multiCurveProvider;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multiCurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multiCurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multiCurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _blackParameters.hashCode();
    result = prime * result + _multiCurveProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSwaptionFlatProvider)) {
      return false;
    }
    final BlackSwaptionFlatProvider other = (BlackSwaptionFlatProvider) obj;
    if (!ObjectUtils.equals(_blackParameters, other._blackParameters)) {
      return false;
    }
    if (!ObjectUtils.equals(_multiCurveProvider, other._multiCurveProvider)) {
      return false;
    }
    return true;
  }

}
