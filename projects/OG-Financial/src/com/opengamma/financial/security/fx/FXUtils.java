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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for handling FX
 */
public class FXUtils {

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
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
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
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
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
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
    } else {
      bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
    }
    return bloomberg;
  }


}
