/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public abstract class FutureOptionSecurity extends ExchangeTradedOptionSecurity {
  public static final String FUTURE_OPTION_TYPE = "FUTURE_OPTION";
  private final boolean _isMargined;

  public FutureOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final DomainSpecificIdentifier underlyingIdentityKey, final Currency currency,
      final String exchange, final boolean isMargined) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, exchange);
    setSecurityType(FUTURE_OPTION_TYPE);
    _isMargined = isMargined;
  }

  public boolean isMargined() {
    return _isMargined;
  }

  public abstract <T> T accept(FutureOptionSecurityVisitor<T> visitor);

  @Override
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return accept((FutureOptionSecurityVisitor<T>) visitor);
  }
}
