/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public abstract class EquityOptionSecurity extends FinancialSecurity implements Option {
  public static final String EQUITY_OPTION_TYPE="EQUITY_OPTION";
  private OptionType _optionType;
  private double _strike;
  private Expiry _expiry;
  private SecurityKey _underlying;
  private Currency _currency;
  // TODO: jim 23-Sep-2009 -- Add support for regions/countries

  public EquityOptionSecurity(OptionType optionType, double strike, Expiry expiry, SecurityKey underlying, Currency currency) {
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlying = underlying;
    _currency = currency;
    setSecurityType(EQUITY_OPTION_TYPE);
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
  
  public SecurityKey getUnderlying() {
    return _underlying;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public abstract <T> T accept(OptionVisitor<T> visitor);
}
