/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;

/**
 *  Interface for pricing inflation zero-coupon cap/floor using the Black method.
 */
public interface BlackSmileCapInflationZeroCouponProviderInterface extends ParameterInflationProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackSmileCapInflationZeroCouponProviderInterface copy();

  /**
   * Returns the Black parameters.
   * @return The parameters
   */
  BlackSmileCapInflationZeroCouponParameters getBlackParameters();
}
