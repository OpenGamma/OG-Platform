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
 * An American equity option security.
 */
public class AmericanVanillaEquityOptionSecurity extends EquityOptionSecurity
    implements AmericanVanillaOption {

  /**
   * Creates the security.
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentifier
   * @param currency
   * @param exchange
   */
  public AmericanVanillaEquityOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, exchange);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitAmericanVanillaEquityOptionSecurity(this);
  }

}
