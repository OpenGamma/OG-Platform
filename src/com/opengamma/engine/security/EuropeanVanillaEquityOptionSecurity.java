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
public class EuropeanVanillaEquityOptionSecurity extends EquityOptionSecurity
    implements EuropeanVanillaOption {

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public EuropeanVanillaEquityOptionSecurity(OptionType optionType,
      double strike, Expiry expiry) {
    super(optionType, strike, expiry);
  }

  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

}
