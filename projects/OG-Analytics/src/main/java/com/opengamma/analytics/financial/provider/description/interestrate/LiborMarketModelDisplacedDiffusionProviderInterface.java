/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface for a LMM parameters provider for one underlying.
 */
public interface LiborMarketModelDisplacedDiffusionProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  LiborMarketModelDisplacedDiffusionProviderInterface copy();

  /**
   * Returns the LMM parameters.
   * @return The parameters.
   */
  LiborMarketModelDisplacedDiffusionParameters getLMMParameters();

  /**
   * Returns the currency for which the LMM parameters are valid (LMM on the discounting curve).
   * @return The currency.
   */
  Currency getLMMCurrency();

}
