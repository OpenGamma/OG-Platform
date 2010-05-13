/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class PoweredEquityOptionSecurity extends EquityOptionSecurity implements PoweredOption {

  private final double _power;

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public PoweredEquityOptionSecurity(final OptionType optionType, final double strike,
 final Expiry expiry, final double power, final Identifier underlyingIdentityKey,
      final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, pointValue, exchange);
    _power = power;
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitPoweredOption(this);
  }

  @Override
  public double getPower() {
    return _power;
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitPoweredEquityOptionSecurity(this);
  }

}
