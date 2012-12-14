/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Implementation for swaption SABR parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class SABRCapProviderDiscount extends SABRCapProvider {

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The SABR parameters.
   * @param index The cap/floor index.
   */
  public SABRCapProviderDiscount(MulticurveProviderDiscount multicurveProvider, SABRInterestRateParameters parameters, final IborIndex index) {
    super(multicurveProvider, parameters, index);
  }

  @Override
  public SABRCapProviderInterface copy() {
    MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new SABRCapProviderDiscount(multicurveProvider, getSABRParameter(), getSABRIndex());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}
