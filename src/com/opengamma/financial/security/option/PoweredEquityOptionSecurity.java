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

  private double _power;

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public PoweredEquityOptionSecurity(OptionType optionType, double strike,
      Expiry expiry, double power, Identifier underlyingIdentityKey, Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, exchange);
    _power = power;
  }

  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitPoweredOption(this);
  }

  @Override
  public double getPower() {
    return _power;
  }

  @Override
  public <T> T accept(EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitPoweredEquityOptionSecurity(this);
  }

}
