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
public class AmericanVanillaEquityOptionSecurity extends EquityOptionSecurity implements AmericanVanillaOption {

  /**
   * @param optionType
   * @param strike
   * @param expiry
   */
  public AmericanVanillaEquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingUniqueId,
 final Currency currency,
      final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingUniqueId, currency, pointValue, exchange);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitAmericanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitAmericanVanillaEquityOptionSecurity(this);
  }
}
