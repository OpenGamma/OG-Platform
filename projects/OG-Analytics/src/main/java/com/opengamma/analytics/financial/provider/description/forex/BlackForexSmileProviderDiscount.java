/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Container for the discounting curves and volatility surface needed to price FX options.
 */
public class BlackForexSmileProviderDiscount extends BlackForexSmileProvider {

  /**
   * Constructor from exiting multicurveProvider and volatility model. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param smile Smile.
   * @param currencyPair The currency pair.
   */
  public BlackForexSmileProviderDiscount(final MulticurveProviderDiscount multicurves, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    super(multicurves, smile, currencyPair);
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  @Override
  public BlackForexSmileProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new BlackForexSmileProviderDiscount(multicurveProvider, getVolatility(), getCurrencyPair());
  }

}
