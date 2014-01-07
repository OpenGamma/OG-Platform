/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Interface for STIR SABR parameters provider for one underlying.
 */
public interface SABRSTIRFuturesProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  SABRSTIRFuturesProviderInterface copy();

  /**
   * Gets the parameters of the SABR grid.
   * @return The SABR parameters.
   */
  SABRInterestRateParameters getSABRParameters();

  /**
   * Returns the Ibor Index for which the SABR data is valid, i.e. the data is calibrated to STIR on the given index.
   * @return The generator.
   */
  IborIndex getSABRIndex();

}
