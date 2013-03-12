/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapParameters;

/**
 * Interface for pricing cap/floor using the Black method.
 */
public interface BlackSmileCapProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackSmileCapProviderInterface copy();

  /**
   * Returns the Black parameters.
   * @return The parameters
   */
  BlackSmileCapParameters getBlackParameters();

}
