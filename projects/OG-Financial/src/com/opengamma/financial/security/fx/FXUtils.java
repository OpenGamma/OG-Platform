/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.model.forex.ForexUtils;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for handling FX
 */
public class FXUtils {

  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXSecurity
   * @param fxSecurity the fx security
   * @param convertToPayCurrency whether to get the code that will convert a value to the pay currency
   * @return an IdentifierBundle containing identifiers for the spot rate, not null
   */
  public static final IdentifierBundle getSpotIdentifiers(final FXSecurity fxSecurity, final boolean convertToPayCurrency) {
    final Currency payCurrency = fxSecurity.getPayCurrency();
    final Currency receiveCurrency = fxSecurity.getReceiveCurrency();
    Identifier bloomberg;
    if (convertToPayCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(receiveCurrency.getCode() + payCurrency.getCode() + " Curncy");
    }
    return IdentifierBundle.of(bloomberg);
  }

  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an IdentifierBundle containing identifiers for the spot rate, not null
   */
  public static final IdentifierBundle getSpotIdentifiers(final FXOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    Identifier bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return IdentifierBundle.of(bloomberg);
  }

  //TODO remove this
  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final Identifier getSpotIdentifier(final FXBarrierOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    Identifier bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXOptionSecurity
   * @param fxOptionSecurity the fx option security
   * @param convertToPutCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final Identifier getSpotIdentifier(final FXOptionSecurity fxOptionSecurity, final boolean convertToPutCurrency) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    Identifier bloomberg;
    if (convertToPutCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXOptionSecurity. 
   * The identifier respect the market base/quote currencies.
   * @param fxOptionSecurity the fx option security
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final Identifier getSpotIdentifier(final FXOptionSecurity fxOptionSecurity) {
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    Identifier bloomberg;
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }

  //TODO remove this
  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXSecurity
   * @param fxSecurity the fx security
   * @param convertToPayCurrency whether to get the code that will convert a value to the put currency
   * @return an Identifier containing identifier for the spot rate, not null
   */
  public static final Identifier getSpotIdentifier(final FXSecurity fxSecurity, final boolean convertToPayCurrency) {
    final Currency payCurrency = fxSecurity.getPayCurrency();
    final Currency receiveCurrency = fxSecurity.getReceiveCurrency();
    Identifier bloomberg;
    if (convertToPayCurrency) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(receiveCurrency.getCode() + payCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }
}
