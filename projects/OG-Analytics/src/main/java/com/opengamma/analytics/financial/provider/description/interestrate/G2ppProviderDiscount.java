/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.util.money.Currency;

/**
 * Class describing a provider with discounting, forward and G2++ parameters.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class G2ppProviderDiscount extends G2ppProvider {

  /**
   * Constructor from exiting multicurveProvider and G2++ parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The G2++ parameters.
   * @param ccyG2pp The currency for which the G2++ parameters are valid (G2++ on the discounting curve).
   */
  public G2ppProviderDiscount(final MulticurveProviderDiscount multicurves, final G2ppPiecewiseConstantParameters parameters, final Currency ccyG2pp) {
    super(multicurves, parameters, ccyG2pp);
  }

  @Override
  public G2ppProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new G2ppProviderDiscount(multicurveProvider, getG2ppParameters(), getG2ppCurrency());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }
}
