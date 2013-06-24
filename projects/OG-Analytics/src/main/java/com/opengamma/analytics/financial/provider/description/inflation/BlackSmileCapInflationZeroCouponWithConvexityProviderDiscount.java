/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;

/**
 * Implementation of a provider of Black smile for zero-coupon inflation options with convexity adjustment. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying.
 */
public class BlackSmileCapInflationZeroCouponWithConvexityProviderDiscount extends BlackSmileCapInflationZeroCouponWithConvexityProvider {

  /**
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   * @param inflationConvexityAdjutmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public BlackSmileCapInflationZeroCouponWithConvexityProviderDiscount(InflationProviderDiscount inflation, final BlackSmileCapInflationZeroCouponParameters parameters,
      final InflationConvexityAdjustmentParameters inflationConvexityAdjutmentsParameters, final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    super(inflation, parameters, inflationConvexityAdjutmentsParameters, blackSmileIborCapParameters);
  }

  @Override
  public BlackSmileCapInflationZeroCouponWithConvexityProviderDiscount copy() {
    InflationProviderDiscount inflation = getInflationProvider().copy();
    return new BlackSmileCapInflationZeroCouponWithConvexityProviderDiscount(inflation, getBlackParameters(), getInflationConvexityAdjustmentParameters(), getBlackSmileIborCapParameters());
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return (InflationProviderDiscount) super.getInflationProvider();
  }

}
