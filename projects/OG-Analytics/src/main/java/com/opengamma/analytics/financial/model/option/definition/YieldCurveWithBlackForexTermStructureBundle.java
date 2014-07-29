/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class describing a yield curve bundle with Black term structure volatility for Forex options.
 * @deprecated Parent class is deprecated
 */
@Deprecated
public class YieldCurveWithBlackForexTermStructureBundle extends ForexOptionDataBundle<BlackForexTermStructureParameters> {

  public static YieldCurveWithBlackForexTermStructureBundle from(final YieldCurveBundle ycBundle, final BlackForexTermStructureParameters termStructure, final Pair<Currency, Currency> currencyPair) {
    return new YieldCurveWithBlackForexTermStructureBundle(ycBundle, termStructure, currencyPair);
  }

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
    final Pair<Currency, Currency> currencyPair = Pairs.of(getCurrencyPair().getFirst(), getCurrencyPair().getSecond());
    return new YieldCurveWithBlackForexTermStructureBundle(curves, termStructure, currencyPair);
  }

  @Override
  public YieldCurveWithBlackForexTermStructureBundle with(final YieldCurveBundle ycBundle) {
    return new YieldCurveWithBlackForexTermStructureBundle(ycBundle, getVolatilityModel(), getCurrencyPair());
  }

  @Override
  public YieldCurveWithBlackForexTermStructureBundle with(final BlackForexTermStructureParameters volatilityModel) {
    return new YieldCurveWithBlackForexTermStructureBundle(this, volatilityModel, getCurrencyPair());
  }

}
