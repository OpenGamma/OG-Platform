/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.option.definition.BlackSwaptionParameters;

/**
 * Interface for pricing swaptions using the Black method.
 */
public interface BlackSwaptionProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  BlackSwaptionProviderInterface copy();

  /**
   * Returns the Black parameters.
   * @return The parameters
   */
  BlackSwaptionParameters getBlackParameters();

}
