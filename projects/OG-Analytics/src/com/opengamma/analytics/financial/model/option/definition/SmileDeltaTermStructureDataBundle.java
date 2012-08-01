/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 */
public class SmileDeltaTermStructureDataBundle extends ForexOptionDataBundle<SmileDeltaTermStructureParametersStrikeInterpolation> {

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
    final Pair<Currency, Currency> currencyPair = Pair.of(getCurrencyPair().getFirst(), getCurrencyPair().getSecond());
    return new SmileDeltaTermStructureDataBundle(curves, smile, currencyPair);
  }
}
