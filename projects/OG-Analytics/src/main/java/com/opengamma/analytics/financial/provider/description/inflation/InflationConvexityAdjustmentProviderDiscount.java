/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;

/**
 * Implementation of a provider of Black smile for zero-coupon inflation options. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying.
 */
public class InflationConvexityAdjustmentProviderDiscount extends InflationConvexityAdjustmentProvider {

  /**
   * @param inflation The inflation provider.
   * @param inflationConvexityAdjutmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public InflationConvexityAdjustmentProviderDiscount(InflationProviderDiscount inflation, final InflationConvexityAdjustmentParameters inflationConvexityAdjutmentsParameters,
      final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    super(inflation, inflationConvexityAdjutmentsParameters, blackSmileIborCapParameters);
  }

  @Override
  public InflationConvexityAdjustmentProviderDiscount copy() {
    InflationProviderDiscount inflation = getInflationProvider().copy();
    return new InflationConvexityAdjustmentProviderDiscount(inflation, getInflationConvexityAdjustmentParameters(), getBlackSmileIborCapParameters());
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return (InflationProviderDiscount) super.getInflationProvider();
  }

}
