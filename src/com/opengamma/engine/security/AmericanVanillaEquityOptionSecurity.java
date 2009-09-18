/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class AmericanVanillaEquityOptionSecurity extends EquityOptionSecurity implements AmericanVanillaOption {

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public AmericanVanillaEquityOptionSecurity(OptionType optionType,
      double strike, Expiry expiry) {
    super(optionType, strike, expiry);
  }

  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

}
