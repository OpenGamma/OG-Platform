/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexUtils {
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");
  private static final Map<Currency, Integer> BASE_ORDER = new HashMap<Currency, Integer>();
  static {
    BASE_ORDER.put(Currency.EUR, 1);
    BASE_ORDER.put(Currency.GBP, 2);
    BASE_ORDER.put(Currency.AUD, 3);
    // TODO: NZD missing in currencies
    BASE_ORDER.put(Currency.USD, 5);
    BASE_ORDER.put(Currency.CHF, 6);
  }

  public static String getFormattedStrike(final double strike, final Pair<Currency, Currency> pair) {
    if (pair.getFirst().compareTo(pair.getSecond()) < 0) {
      return STRIKE_FORMATTER.format(strike) + " " + pair.getFirst() + "/" + pair.getSecond();
    }
    if (pair.getFirst().compareTo(pair.getSecond()) > 0) {
      return STRIKE_FORMATTER.format(1. / strike) + " " + pair.getSecond() + "/" + pair.getFirst();
    }
    throw new OpenGammaRuntimeException("Currencies were equal");
  }

  /**
   * Indicator that the currencies are in the standard base/quote order.
   * @param currency1 The first currency.
   * @param currency2 The second currency. 
   * @return The indicator.
   */
  public static boolean isBaseCurrency(final Currency currency1, final Currency currency2) {
    if (BASE_ORDER.containsKey(currency1) && BASE_ORDER.containsKey(currency2)) {
      return (BASE_ORDER.get(currency1) < BASE_ORDER.get(currency2));
    }
    if (BASE_ORDER.containsKey(currency1)) {
      return true;
    }
    if (BASE_ORDER.containsKey(currency2)) {
      return false;
    }
    // TODO: currency not in the order
    return true;
  }

  public static CurrencyLabelledMatrix1D getMultipleCurrencyAmountAsMatrix(final MultipleCurrencyAmount mca) {
    Validate.notNull(mca, "multiple currency amount");
    final int n = mca.size();
    final Currency[] keys = new Currency[n];
    final double[] values = new double[n];
    int i = 0;
    for (final CurrencyAmount ca : mca) {
      keys[i] = ca.getCurrency();
      values[i++] = ca.getAmount();
    }
    return new CurrencyLabelledMatrix1D(keys, values);
  }
}
