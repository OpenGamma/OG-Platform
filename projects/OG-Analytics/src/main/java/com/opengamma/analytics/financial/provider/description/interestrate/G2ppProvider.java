/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a provider with discounting, forward and G2++ parameters.
 */
public class G2ppProvider implements G2ppProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The G2++ one factor model parameters.
   */
  private final G2ppPiecewiseConstantParameters _parameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyG2pp;

  /**
   * Constructor from exiting multicurveProvider and G2++ parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The G2++ parameters.
   * @param ccyG2pp The currency for which the G2++ parameters are valid (G2++ on the discounting curve).
   */
  public G2ppProvider(final MulticurveProviderInterface multicurves, final G2ppPiecewiseConstantParameters parameters, final Currency ccyG2pp) {
    ArgumentChecker.notNull(multicurves, "multi-curve provider");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(ccyG2pp, "currency");
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccyG2pp = ccyG2pp;
  }

  @Override
  public G2ppProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new G2ppProvider(multicurveProvider, _parameters, _ccyG2pp);
  }

  @Override
  public G2ppPiecewiseConstantParameters getG2ppParameters() {
    return _parameters;
  }

  @Override
  public Currency getG2ppCurrency() {
    return _ccyG2pp;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

}
