/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapParameters;
import com.opengamma.util.ArgumentChecker;

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
   * @param multicurves The multi-curves provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapProvider(final MulticurveProviderInterface multicurves, final BlackSmileCapParameters parameters) {
    ArgumentChecker.notNull(multicurves, "Multi-curve provider");
    _multicurves = multicurves;
    _parameters = parameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurves;
  }

  @Override
  public BlackSmileCapProviderInterface copy() {
    MulticurveProviderInterface multicurves = _multicurves.copy();
    return new BlackSmileCapProvider(multicurves, _parameters);
  }

  @Override
  public BlackSmileCapParameters getBlackParameters() {
    return _parameters;
  }

}
