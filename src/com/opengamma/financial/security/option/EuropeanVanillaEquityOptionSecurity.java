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
 * A European equity option security.
 */
public class EuropeanVanillaEquityOptionSecurity extends EquityOptionSecurity implements EuropeanVanillaOption {

  /**
   * Creates the security.
   * @param optionType the option type (PUT or CALL)
   * @param strike the strike price
   * @param expiry Expire date of option
   * @param underlyingIdentifier the identifier for underlying security
   * @param currency the security currency
   * @param pointValue the option point value
   * @param exchange the exchange where security trades
   */
  public EuropeanVanillaEquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentifier,
      final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, pointValue, exchange);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaEquityOptionSecurity(this);
  }

}
