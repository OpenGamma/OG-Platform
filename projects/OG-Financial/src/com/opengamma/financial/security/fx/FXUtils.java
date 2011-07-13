/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for handling FX
 */
public class FXUtils {
  /**
   * Returns an IdentifierBundle containing all known identifiers for the spot rate of this FXSecurity
   * @param fxSecurity the fx securityO
   * @return an IdentifierBundle containing identifiers for the spot rate, not null
   */
  public static final IdentifierBundle getSpotIdentifiers(FXSecurity fxSecurity) {
    Currency payCurrency = fxSecurity.getPayCurrency();
    Currency receiveCurrency = fxSecurity.getReceiveCurrency();
    Identifier bloomberg = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    return IdentifierBundle.of(bloomberg); 
  }
}
