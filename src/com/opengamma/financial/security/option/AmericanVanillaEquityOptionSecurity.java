/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * An American equity option security.
 */
public class AmericanVanillaEquityOptionSecurity extends EquityOptionSecurity implements AmericanVanillaOption {

  /**
   * Creates the security.
   * @param optionType the type of option CALL or PUT
   * @param strike the strike price
   * @param expiry the expire date
   * @param underlyingIdentifier Identifier for underlying equity
   * @param currency currency in which it trades
   * @param pointValue the option point value
   * @param exchange exchange where the option trades
   */
  public AmericanVanillaEquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentifier,
      final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, pointValue, exchange);
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
