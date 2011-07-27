/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexUtils {
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");

  public static String getFormattedStrike(final double strike, final Pair<Currency, Currency> pair) {
    if (pair.getFirst().compareTo(pair.getSecond()) < 0) {
      return STRIKE_FORMATTER.format(strike) + " " + pair.getFirst() + "/" + pair.getSecond();
    }
    if (pair.getFirst().compareTo(pair.getSecond()) > 0) {
      return STRIKE_FORMATTER.format(1. / strike) + " " + pair.getSecond() + "/" + pair.getFirst();
    }
    throw new OpenGammaRuntimeException("Currencies were equal");
  }
}
