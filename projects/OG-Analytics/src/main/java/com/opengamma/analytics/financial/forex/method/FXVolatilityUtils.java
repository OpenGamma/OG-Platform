/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * @deprecated {@link SmileDeltaTermStructureDataBundle} is deprecated
 */
@Deprecated
public class FXVolatilityUtils {

  public static double getVolatility(final SmileDeltaTermStructureDataBundle data, final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    ArgumentChecker.notNull(data, "data");
    final Pair<Currency, Currency> currencyPair = data.getCurrencyPair();
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = data.getVolatilityModel();
    if ((ccy1 == currencyPair.getFirst()) && (ccy2 == currencyPair.getSecond())) {
      return smile.getVolatility(time, strike, forward);
    }
    if ((ccy2 == currencyPair.getFirst()) && (ccy1 == currencyPair.getSecond())) {
      return smile.getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2 + ", have " + data.getCurrencyMap().values());
  }

  public static VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final SmileDeltaTermStructureDataBundle data, final Currency ccy1, final Currency ccy2,
      final double time, final double strike, final double forward) {
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    ArgumentChecker.notNull(data, "data");
    final Pair<Currency, Currency> currencyPair = data.getCurrencyPair();
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = data.getVolatilityModel();
    if ((ccy1 == currencyPair.getFirst()) && (ccy2 == currencyPair.getSecond())) {
      return smile.getVolatilityAndSensitivities(time, strike, forward);
    }
    if ((ccy2 == currencyPair.getFirst()) && (ccy1 == currencyPair.getSecond())) {
      return smile.getVolatilityAndSensitivities(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2 + ", have " + data.getCurrencyMap().values());
  }
}
