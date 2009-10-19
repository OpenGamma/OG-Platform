/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.securities.Currency;
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
      Expiry expiry, double power, SecurityKey underlying, Currency currency) {
    super(optionType, strike, expiry, underlying, currency);
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

}
