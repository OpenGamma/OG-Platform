/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * Utility methods for handling FX
 */
public class FXUtils {
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");
  private static final Map<Currency, Integer> BASE_ORDER = new HashMap<Currency, Integer>();
  static {
    //TODO get rid of all of this and use CurrencyPairs
    BASE_ORDER.put(Currency.EUR, 1);
    BASE_ORDER.put(Currency.GBP, 2);
    BASE_ORDER.put(Currency.AUD, 3);
    BASE_ORDER.put(Currency.NZD, 4);
    BASE_ORDER.put(Currency.USD, 5);
    BASE_ORDER.put(Currency.CHF, 6);
    BASE_ORDER.put(Currency.CAD, 8);
    BASE_ORDER.put(Currency.SEK, 9);
    BASE_ORDER.put(Currency.NOK, 10);
    BASE_ORDER.put(Currency.JPY, 11);
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
  public static boolean isInBaseQuoteOrder(final Currency currency1, final Currency currency2) {
    if (BASE_ORDER.containsKey(currency1) && BASE_ORDER.containsKey(currency2)) {
      return (BASE_ORDER.get(currency1) < BASE_ORDER.get(currency2));
    }
    if (BASE_ORDER.containsKey(currency1)) {
      return true;
    }
    if (BASE_ORDER.containsKey(currency2)) {
      return false;
    }
    throw new OpenGammaRuntimeException("Base quote order information for " + currency1 + " and " + currency2 + " not available");
  }

  /**
   * Indicator that the currencies are in the standard base/quote order.
   * @param currency1 The first currency.
   * @param currency2 The second currency.
   * @param currencyPairs The currency pairs.
   * @return The indicator.
   */
  public static boolean isInBaseQuoteOrder(final Currency currency1, final Currency currency2, final CurrencyPairs currencyPairs) {
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(currency1, currency2);
    if (currencyPair.getBase().equals(currency1)) {
      return true;
    }
    return false;
  }

  /**
   * Return in the standard base/quote currency from two currencies.
   * @param currency1 The first currency.
   * @param currency2 The second currency.
   * @return The base currency.
   */
  public static Currency baseCurrency(final Currency currency1, final Currency currency2) {
    if (isInBaseQuoteOrder(currency1, currency2)) {
      return currency1;
    }
    return currency2;
  }

  /**
   * Return in the currency which is not the base currency from two currencies.
   * @param currency1 The first currency.
   * @param currency2 The second currency.
   * @return The non-base currency.
   */
  public static Currency nonBaseCurrency(final Currency currency1, final Currency currency2) {
    if (isInBaseQuoteOrder(currency1, currency2)) {
      return currency2;
    }
    return currency1;
  }

  public static CurrencyLabelledMatrix1D getMultipleCurrencyAmountAsMatrix(final MultipleCurrencyAmount mca) {
    ArgumentChecker.notNull(mca, "multiple currency amount");
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

  public static boolean isFXSecurity(final Security security) {
    return security instanceof FXForwardSecurity || security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity
        || security instanceof NonDeliverableFXForwardSecurity || security instanceof NonDeliverableFXOptionSecurity || security instanceof NonDeliverableFXDigitalOptionSecurity;
  }

}
