/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * @author emcleod
 */
public class EuropeanVanillaFutureOptionSecurity extends FutureOptionSecurity implements EuropeanVanillaOption {

  public EuropeanVanillaFutureOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingUniqueId,
      final Currency currency, final String exchange, final boolean isMargined) {
    super(optionType, strike, expiry, underlyingUniqueId, currency, exchange, isMargined);
  }

  @Override
  public <T> T accept(final FutureOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaFutureOptionSecurity(this);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

}
