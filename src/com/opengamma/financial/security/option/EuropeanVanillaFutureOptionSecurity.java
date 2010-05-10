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
 * A European future option security.
 */
public class EuropeanVanillaFutureOptionSecurity extends FutureOptionSecurity implements EuropeanVanillaOption {

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
  public EuropeanVanillaFutureOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String exchange, final boolean isMargined) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, exchange, isMargined);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final FutureOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaFutureOptionSecurity(this);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

}
