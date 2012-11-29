/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Interface for swaption SABR parameters provider for one underlying.
 */
public interface SABRCapProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  SABRCapProviderInterface copy();

  /**
   * Gets the parameters of the SABR grid.
   * @return The SABR parameters.
   */
  SABRInterestRateParameters getSABRParameter();

  /**
   * Returns the Ibor Index for which the SABR data is valid, i.e. the data is calibrated to cap/floor on the given index.
   * @return The generator.
   */
  IborIndex getSABRIndex();

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  MulticurveProviderInterface getMulticurveProvider();

}
