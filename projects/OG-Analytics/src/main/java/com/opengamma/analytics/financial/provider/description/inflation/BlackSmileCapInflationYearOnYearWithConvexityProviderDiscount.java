/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;

/**
 * Implementation of a provider of Black smile for year on year inflation options with convexity adjustment. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying.
 */
public class BlackSmileCapInflationYearOnYearWithConvexityProviderDiscount extends BlackSmileCapInflationYearOnYearWithConvexityProvider {

  /**
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   *  @param inflationConvexityAdjutmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public BlackSmileCapInflationYearOnYearWithConvexityProviderDiscount(InflationProviderDiscount inflation, final BlackSmileCapInflationYearOnYearParameters parameters,
      final InflationConvexityAdjustmentParameters inflationConvexityAdjutmentsParameters, final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    super(inflation, parameters, inflationConvexityAdjutmentsParameters, blackSmileIborCapParameters);
  }

  @Override
  public BlackSmileCapInflationYearOnYearWithConvexityProviderDiscount copy() {
    InflationProviderDiscount inflation = getInflationProvider().copy();
    return new BlackSmileCapInflationYearOnYearWithConvexityProviderDiscount(inflation, getBlackParameters(), getInflationConvexityAdjustmentParameters(), getBlackSmileIborCapParameters());
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return (InflationProviderDiscount) super.getInflationProvider();
  }

}
