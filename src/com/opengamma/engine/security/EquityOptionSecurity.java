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
public abstract class EquityOptionSecurity extends DefaultSecurity implements Option {
  private OptionType _optionType;
  private double _strike;
  private Expiry _expiry;

  public EquityOptionSecurity(OptionType optionType, double strike, Expiry expiry) {
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
  }

  /**
   * @return the optionType
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @return the expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }
  
  public abstract <T> T accept(OptionVisitor<T> visitor);
}
