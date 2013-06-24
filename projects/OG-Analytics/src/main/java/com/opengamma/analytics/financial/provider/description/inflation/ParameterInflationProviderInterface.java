/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *  Interface for parameter provider with inflation provider.
 */
public interface ParameterInflationProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider. For the curves related data, new maps are created. The parameter related data, the same objects are used.
   * @return The new provider.
   */
  @Override
  ParameterInflationProviderInterface copy();

  /**
   * Returns the inflation provider.
   * @return The inflation provider
   */

  InflationProviderInterface getInflationProvider();

}
