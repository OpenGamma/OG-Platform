/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexVannaVolgaProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 * @deprecated Use {@link BlackForexVannaVolgaProvider}
 */
@Deprecated
public class SmileDeltaTermStructureVannaVolgaDataBundle extends ForexOptionDataBundle<SmileDeltaTermStructureParameters> {

  public static SmileDeltaTermStructureVannaVolgaDataBundle from(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParameters smile, final Pair<Currency, Currency> currencyPair) {
    return new SmileDeltaTermStructureVannaVolgaDataBundle(ycBundle, smile, currencyPair);
  }
  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle, not null
   * @param smile The smile parameters, not null
   * @param currencyPair The currency pair for which the smile is valid, not null
   */
  public SmileDeltaTermStructureVannaVolgaDataBundle(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParameters smile, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle, smile, currencyPair);
    ArgumentChecker.isTrue(smile.getNumberStrike() == 3, "Vanna-volga methods work only with three strikes; have {}", smile.getNumberStrike());
  }

  @Override
  /**
   * Create a  copy of the bundle.
   * @return The bundle.
   */
  public SmileDeltaTermStructureVannaVolgaDataBundle copy() {
    final YieldCurveBundle curves = getCurvesCopy();
    final SmileDeltaTermStructureParameters smile = getVolatilityModel().copy();
    final Pair<Currency, Currency> currencyPair = Pairs.of(getCurrencyPair().getFirst(), getCurrencyPair().getSecond());
    return new SmileDeltaTermStructureVannaVolgaDataBundle(curves, smile, currencyPair);
  }

  /**
   * Get the volatility at a given time/strike/forward taking the currency pair order in account. See {@link SmileDeltaTermStructureParametersStrikeInterpolation} for the interpolation/extrapolation.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @return The volatility.
   */
  public SmileDeltaParameters getSmile(final Currency ccy1, final Currency ccy2, final double time) {
    ArgumentChecker.notNull(ccy1, "first currency");
    ArgumentChecker.notNull(ccy2, "second currency");
    final SmileDeltaParameters smile = getVolatilityModel().getSmileForTime(time);
    if (ccy1.equals(getCurrencyPair().getFirst()) && ccy2.equals(getCurrencyPair().getSecond())) {
      return smile;
    }
    throw new NotImplementedException("Currency pair is not in expected order " + getCurrencyPair().toString());
  }

  @Override
  public SmileDeltaTermStructureVannaVolgaDataBundle with(final YieldCurveBundle ycBundle) {
    return new SmileDeltaTermStructureVannaVolgaDataBundle(ycBundle, getVolatilityModel(), getCurrencyPair());
  }

  @Override
  public SmileDeltaTermStructureVannaVolgaDataBundle with(final SmileDeltaTermStructureParameters volatilityModel) {
    return new SmileDeltaTermStructureVannaVolgaDataBundle(this, volatilityModel, getCurrencyPair());
  }

}
