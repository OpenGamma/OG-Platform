/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapParameters;

/**
 * Implementation of a provider of Black smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent.
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 */
public class BlackSmileCapProviderDiscount extends BlackSmileCapProvider {

  /**
   * @param multicurveProvider The multi-curve provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapProviderDiscount(final MulticurveProviderDiscount multicurveProvider, final BlackSmileCapParameters parameters) {
    super(multicurveProvider, parameters);
  }

  @Override
  public BlackSmileCapProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new BlackSmileCapProviderDiscount(multicurveProvider, getBlackParameters());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}
