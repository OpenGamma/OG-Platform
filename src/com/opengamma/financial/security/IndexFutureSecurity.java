/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class IndexFutureSecurity extends FutureSecurity {
  private final Identifier _underlyingIdentifier;

  public IndexFutureSecurity(final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _underlyingIdentifier = underlyingIdentifier;
  }

  public Identifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }

  @Override
  public <T> T accept(final FutureSecurityVisitor<T> visitor) {
    return visitor.visitIndexFutureSecurity(this);
  }

}
