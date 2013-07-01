/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;

/**
 * Interface for inflation convexity adjustments.
 */
public interface InflationConvexityAdjustmentProviderInterface extends ParameterInflationProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  InflationConvexityAdjustmentProviderInterface copy();

  /**
   * Returns the Inflation Convexity Adjustment parameters.
   * @return The parameters
   */
  InflationConvexityAdjustmentParameters getInflationConvexityAdjustmentParameters();

  /**
   * Returns the Black volatility surface used in cap/floor ibor.
   * @return The parameters
   */
  BlackFlatCapFloorParameters getBlackSmileIborCapParameters();

}
