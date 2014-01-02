/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.tuple.DoublesPair;

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

  /**
   * Gets the sensitivity to the inflation parameters.
   * @param name The name of the curve
   * @param pointSensitivity The nodal point sensitivities
   * @return The sensitivity to the inflation parameters
   */
  double[] parameterInflationSensitivity(String name, List<DoublesPair> pointSensitivity);

}
