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
 */
public abstract class ExchangeTradedOptionSecurity extends OptionSecurity {
  private final String _exchange;
  private final double _pointValue;//TODO this might not be the best place for this

  public ExchangeTradedOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentityKey, final Currency currency,
      final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency);
    _exchange = exchange;
    _pointValue = pointValue;
  }

  public String getExchange() {
    return _exchange;
  }

  public double getPointValue() {
    return _pointValue;
  }

  public abstract <T> T accept (ExchangeTradedOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept (final OptionSecurityVisitor<T> visitor) {
    return accept ((ExchangeTradedOptionSecurityVisitor<T>)visitor);
  }

}
