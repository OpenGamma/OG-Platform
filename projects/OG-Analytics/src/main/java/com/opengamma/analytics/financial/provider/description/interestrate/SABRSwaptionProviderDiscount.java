/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Implementation for swaption SABR parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class SABRSwaptionProviderDiscount extends SABRSwaptionProvider {

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The SABR parameters.
   * @param generator The underlying swaps generators.
   */
  public SABRSwaptionProviderDiscount(MulticurveProviderDiscount multicurveProvider, SABRInterestRateParameters parameters, GeneratorSwapFixedIbor generator) {
    super(multicurveProvider, parameters, generator);
  }

  @Override
  public SABRSwaptionProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new SABRSwaptionProviderDiscount(multicurveProvider, getSABRParameter(), getSABRGenerator());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}
