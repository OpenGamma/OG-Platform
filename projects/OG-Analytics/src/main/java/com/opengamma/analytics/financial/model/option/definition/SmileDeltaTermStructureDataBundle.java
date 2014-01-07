/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 * @deprecated Use {@link BlackForexSmileProviderDiscount}
 */
@Deprecated
public class SmileDeltaTermStructureDataBundle extends ForexOptionDataBundle<SmileDeltaTermStructureParametersStrikeInterpolation> {

  public static SmileDeltaTermStructureDataBundle from(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    return new SmileDeltaTermStructureDataBundle(ycBundle, smile, currencyPair);
  }

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle.
   * @param smile The smile parameters.
   * @param currencyPair The currency pair for which the smile is valid.
   */
  public SmileDeltaTermStructureDataBundle(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle, smile, currencyPair);
  }

  @Override
  public SmileDeltaTermStructureDataBundle copy() {
    final YieldCurveBundle curves = getCurvesCopy();
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = getVolatilityModel().copy();
    final Pair<Currency, Currency> currencyPair = Pairs.of(getCurrencyPair().getFirst(), getCurrencyPair().getSecond());
    return new SmileDeltaTermStructureDataBundle(curves, smile, currencyPair);
  }

  @Override
  public SmileDeltaTermStructureDataBundle with(final YieldCurveBundle ycBundle) {
    return new SmileDeltaTermStructureDataBundle(ycBundle, getVolatilityModel(), getCurrencyPair());
  }

  @Override
  public SmileDeltaTermStructureDataBundle with(final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel) {
    return new SmileDeltaTermStructureDataBundle(this, volatilityModel, getCurrencyPair());
  }

}
