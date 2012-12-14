/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface for Hull-White parameters provider for one currency.
 */
public class HullWhiteOneFactorProvider implements HullWhiteOneFactorProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyHW;

  /**
   * Constructor from exiting multicurveProvider and Hull-White parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The Hull-White one factor parameters.
   * @param ccyHW The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  public HullWhiteOneFactorProvider(final MulticurveProviderInterface multicurves, HullWhiteOneFactorPiecewiseConstantParameters parameters, Currency ccyHW) {
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccyHW = ccyHW;
  }

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  public HullWhiteOneFactorProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new HullWhiteOneFactorProvider(multicurveProvider, getHullWhiteParameters(), getHullWhiteCurrency());
  }

  /**
   * Returns the Hull-White one factor model parameters.
   * @return The parameters.
   */
  @Override
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters() {
    return _parameters;
  }

  /**
   * Returns the currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   * @return The currency.
   */
  @Override
  public Currency getHullWhiteCurrency() {
    return _ccyHW;
  }

  /**
   * Returns the MulticurveProvider from which the HullWhiteOneFactorProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

}
