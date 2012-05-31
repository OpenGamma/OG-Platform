/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalId;
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
    BASE_ORDER.put(Currency.EUR, 1);
    BASE_ORDER.put(Currency.GBP, 2);
    BASE_ORDER.put(Currency.AUD, 3);
    BASE_ORDER.put(Currency.NZD, 4);
    BASE_ORDER.put(Currency.USD, 5);
    BASE_ORDER.put(Currency.CHF, 6);
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  // TODO: review: Should Bbg code be in Financial?
  public static final ExternalId getSpotIdentifier(final FXOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (convertToPutCurrency) {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  // TODO: review: Should Bbg code be in Financial?
  public static final ExternalId getSpotIdentifier(final FXOptionSecurity fxOptionSecurity) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxForwardSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  // TODO: review: Should Bbg code be in Financial?
  public static final ExternalId getSpotIdentifier(final FXForwardSecurity fxForwardSecurity) {
    final Currency putCurrency = fxForwardSecurity.getPayCurrency();
    final Currency callCurrency = fxForwardSecurity.getReceiveCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxDigitalOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  // TODO: review: Should Bbg code be in Financial?
  public static final ExternalId getSpotIdentifier(final FXDigitalOptionSecurity fxDigitalOptionSecurity) {
    final Currency putCurrency = fxDigitalOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxDigitalOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = ExternalSchemes.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
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
    // TODO: Review what to do when none of the currencies is in the given list
    return true;
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
