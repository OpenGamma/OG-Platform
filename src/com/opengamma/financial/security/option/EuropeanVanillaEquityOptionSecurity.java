/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * A European equity option security.
 */
public class EuropeanVanillaEquityOptionSecurity extends EquityOptionSecurity
    implements EuropeanVanillaOption {

  /**
   * Creates the security.
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentifier
   * @param currency
   * @param exchange
   */
  public EuropeanVanillaEquityOptionSecurity(OptionType optionType,
      double strike, Expiry expiry, UniqueIdentifier underlyingIdentifier, Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, exchange);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

  @Override
  public <T> T accept(EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaEquityOptionSecurity(this);
  }

}
