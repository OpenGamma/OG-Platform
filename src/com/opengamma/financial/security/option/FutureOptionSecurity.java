/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * A future option security.
 */
public abstract class FutureOptionSecurity extends ExchangeTradedOptionSecurity {

  /**
   * The security type.
   */
  public static final String FUTURE_OPTION_TYPE = "FUTURE_OPTION";

  private final boolean _isMargined;

  public FutureOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier, final Currency currency,
      final String exchange, final boolean isMargined) {
    super(FUTURE_OPTION_TYPE, optionType, strike, expiry, underlyingIdentifier, currency, exchange);
    _isMargined = isMargined;
  }

  public boolean isMargined() {
    return _isMargined;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept(FutureOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept(final ExchangeTradedOptionSecurityVisitor<T> visitor) {
    return accept((FutureOptionSecurityVisitor<T>) visitor);
  }

}
