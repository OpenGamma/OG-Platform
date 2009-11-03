/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
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
      double strike, Expiry expiry, String underlyingUniqueId, Currency currency) {
    super(optionType, strike, expiry, underlyingUniqueId, currency);
  }

  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitAmericanVanillaEquityOptionSecurity(this);
  }

}
