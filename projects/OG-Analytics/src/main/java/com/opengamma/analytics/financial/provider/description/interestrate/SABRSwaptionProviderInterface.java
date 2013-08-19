/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Interface for swaption SABR parameters provider for one underlying.
 */
public interface SABRSwaptionProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  SABRSwaptionProviderInterface copy();

  /**
   * Gets the parameters of the SABR grid.
   * @return The SABR parameters.
   */
  SABRInterestRateParameters getSABRParameter();

  /**
   * Returns the swap generator for which the SABR data is valid, i.e. the data is calibrated to swaption on vanilla swaps with the description contains in the generator.
   * @return The generator.
   */
  GeneratorSwapFixedIbor getSABRGenerator();

}
