/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface for LMM parameters in one currency and multi-curves provider.
 */
public class LiborMarketModelDisplacedDiffusionProvider {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The Hull-White one factor model parameters.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _parameters;
  /**
   * The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  private final Currency _ccy;

  /**
   * Constructor from exiting multicurveProvider and LMM parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The LMM parameters.
   * @param ccy The currency for which the LMM parameters are valid (LMM on the discounting curve).
   */
  public LiborMarketModelDisplacedDiffusionProvider(final MulticurveProviderInterface multicurves, LiborMarketModelDisplacedDiffusionParameters parameters, Currency ccy) {
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccy = ccy;
  }

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  public LiborMarketModelDisplacedDiffusionProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new LiborMarketModelDisplacedDiffusionProvider(multicurveProvider, getLMMParameters(), getLMMCurrency());
  }

  /**
   * Returns the Hull-White one factor model parameters.
   * @return The parameters.
   */
  public LiborMarketModelDisplacedDiffusionParameters getLMMParameters() {
    return _parameters;
  }

  /**
   * Returns the currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   * @return The currency.
   */
  public Currency getLMMCurrency() {
    return _ccy;
  }

  /**
   * Returns the MulticurveProvider from which the HullWhiteOneFactorProvider is composed.
   * @return The multi-curves provider.
   */
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

}
