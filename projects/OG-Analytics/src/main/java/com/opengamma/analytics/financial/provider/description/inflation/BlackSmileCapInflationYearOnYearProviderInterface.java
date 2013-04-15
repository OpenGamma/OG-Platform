/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *  Interface for pricing inflation zero-coupon cap/floor using the Black method.
 */
public interface BlackSmileCapInflationYearOnYearProviderInterface extends ParameterProviderInterface {
  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackSmileCapInflationYearOnYearProviderInterface copy();

  /**
   * Returns the Black parameters.
   * @return The parameters
   */
  BlackSmileCapInflationYearOnYearParameters getBlackParameters();

  /**
   * Returns the inflation provider.
   * @return The inflation provider
   */
  InflationProviderInterface getInflationProvider();

}
