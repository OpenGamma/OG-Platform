/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a yield curve bundle with Black term structure volatility for Forex options.
 */
public class YieldCurveWithBlackForexTermStructureBundle extends ForexOptionDataBundle<BlackForexTermStructureParameters> {

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle.
   * @param termStructure The term structure parameters.
   * @param currencyPair The currency pair for which the smile is valid.
   */
  public YieldCurveWithBlackForexTermStructureBundle(final YieldCurveBundle ycBundle, final BlackForexTermStructureParameters termStructure, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle, termStructure, currencyPair);
  }

  @Override
  public YieldCurveWithBlackForexTermStructureBundle copy() {
    final YieldCurveBundle curves = getCurvesCopy();
    final BlackForexTermStructureParameters termStructure = new BlackForexTermStructureParameters(getVolatilityModel().getVolatilityCurve());
    final Pair<Currency, Currency> currencyPair = Pair.of(getCurrencyPair().getFirst(), getCurrencyPair().getSecond());
    return new YieldCurveWithBlackForexTermStructureBundle(curves, termStructure, currencyPair);
  }

}
