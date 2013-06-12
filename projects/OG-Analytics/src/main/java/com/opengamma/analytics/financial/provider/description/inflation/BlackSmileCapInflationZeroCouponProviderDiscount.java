/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;

/**
 * Implementation of a provider of Black smile for zero-coupon inflation options. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying.
 */
public class BlackSmileCapInflationZeroCouponProviderDiscount extends BlackSmileCapInflationZeroCouponProvider {

  /**
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapInflationZeroCouponProviderDiscount(InflationProviderDiscount inflation, final BlackSmileCapInflationZeroCouponParameters parameters) {
    super(inflation, parameters);
  }

  @Override
  public BlackSmileCapInflationZeroCouponProviderDiscount copy() {
    InflationProviderDiscount inflation = getInflationProvider().copy();
    return new BlackSmileCapInflationZeroCouponProviderDiscount(inflation, getBlackParameters());
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return (InflationProviderDiscount) super.getInflationProvider();
  }

}
