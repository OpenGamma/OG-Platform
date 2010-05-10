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
 * An American future option security.
 */
public class AmericanVanillaFutureOptionSecurity extends FutureOptionSecurity implements AmericanVanillaOption {

  /**
   * Creates the security.
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentifier
   * @param currency
   * @param exchange
   * @param isMargined
   */
  public AmericanVanillaFutureOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String exchange, final boolean isMargined) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, exchange, isMargined);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final FutureOptionSecurityVisitor<T> visitor) {
    return visitor.visitAmericanVanillaFutureOptionSecurity(this);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

}
