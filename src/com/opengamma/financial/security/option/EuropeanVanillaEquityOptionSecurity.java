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
  public EuropeanVanillaEquityOptionSecurity(final OptionType optionType,
 final double strike, final Expiry expiry, final Identifier underlyingIdentityKey,
      final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, pointValue, exchange);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaEquityOptionSecurity(this);
  }

}
