/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
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
    // TODO: NZD missing in currencies
    BASE_ORDER.put(Currency.USD, 5);
    BASE_ORDER.put(Currency.CHF, 6);
  }

  private static ExternalId getSpotIdentifier(final FXForwardSecurity fxForwardSecurity, final boolean convertToPayCurrency) {
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    ExternalId bloomberg;
    if (convertToPayCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(receiveCurrency.getCode() + payCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXForwardSecurity
   * @param fxForwardSecurity the fx forward security
   * @param convertToPayCurrency whether to get the code that will convert a value to the pay currency
   * @return a bundle containing identifiers for the spot rate, not null
   */
  public static final ExternalIdBundle getSpotIdentifiers(final FXForwardSecurity fxForwardSecurity, final boolean convertToPayCurrency) {
    return ExternalIdBundle.of(getSpotIdentifier(fxForwardSecurity, convertToPayCurrency));
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return a bundle containing identifiers for the spot rate, not null
   */
  public static final ExternalIdBundle getSpotIdentifiers(final FXOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return ExternalIdBundle.of(bloomberg);
  }

  //TODO remove this
  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getSpotIdentifier(final FXBarrierOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getInverseSpotIdentifier(final FXBarrierOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getSpotIdentifier(final FXOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getSpotIdentifier(final FXOptionSecurity fxOptionSecurity) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXBarrierOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxBarrierOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getSpotIdentifier(final FXBarrierOptionSecurity fxBarrierOptionSecurity) {
    final Currency putCurrency = fxBarrierOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxBarrierOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns a bundle containing all known identifiers for the spot rate of this FXOptionSecurity.
   * The identifier respect the market base/quote currencies.
   * @param fxOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final ExternalId getInverseSpotIdentifier(final FXOptionSecurity fxOptionSecurity) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    ExternalId bloomberg;
    if (isInBaseQuoteOrder(putCurrency, callCurrency)) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
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

  public static boolean isFXSecurity(final Security security) {
    return security instanceof FXForwardSecurity
        || security instanceof FXOptionSecurity
        || security instanceof FXBarrierOptionSecurity
        || security instanceof FXDigitalOptionSecurity;
  }

}
