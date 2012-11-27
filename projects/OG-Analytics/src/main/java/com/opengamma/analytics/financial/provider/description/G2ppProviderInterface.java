/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface for G2++ parameters provider for one currency.
 */
public interface G2ppProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  G2ppProviderInterface copy();

  /**
   * Returns the G2++ model parameters.
   * @return The parameters.
   */
  G2ppPiecewiseConstantParameters getG2ppParameters();

  /**
   * Returns the currency for which the G2++ parameters are valid (G2++ on the discounting curve).
   * @return The currency.
   */
  Currency getG2ppCurrency();

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  MulticurveProviderInterface getMulticurveProvider();

}
