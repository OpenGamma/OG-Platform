/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation for Black parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class BlackSTIRFuturesExpLogMoneynessProviderDiscount extends BlackSTIRFuturesExpLogMoneynessProvider {

  /**
   * @param multicurve The multicurve provider.
   * @param parameters The Black parameters.
   * @param iborIndex The Ibor index underlying the provider.
   */
  public BlackSTIRFuturesExpLogMoneynessProviderDiscount(final MulticurveProviderDiscount multicurve, final Surface<Double, Double, Double> parameters,
      final IborIndex iborIndex) {
    super(multicurve, parameters, iborIndex);
  }

  @Override
  public BlackSTIRFuturesExpLogMoneynessProviderDiscount copy() {
    final MulticurveProviderDiscount multicurve = getMulticurveProvider().copy();
    return new BlackSTIRFuturesExpLogMoneynessProviderDiscount(multicurve, getBlackParameters(), getFuturesIndex());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}
