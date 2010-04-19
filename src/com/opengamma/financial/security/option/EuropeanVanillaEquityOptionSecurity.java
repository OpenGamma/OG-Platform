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
public class EuropeanVanillaEquityOptionSecurity extends EquityOptionSecurity
    implements EuropeanVanillaOption {

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public EuropeanVanillaEquityOptionSecurity(OptionType optionType,
      double strike, Expiry expiry, Identifier underlyingIdentityKey, Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, exchange);
  }

  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

  @Override
  public <T> T accept(EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaEquityOptionSecurity(this);
  }

}
