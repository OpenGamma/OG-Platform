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
 * An over-the-counter option security.
 */
public abstract class OTCOptionSecurity extends OptionSecurity {

  private final String _counterparty;

  public OTCOptionSecurity(final String securityType, final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String counterparty) {
    super(securityType, optionType, strike, expiry, underlyingIdentifier, currency);
    _counterparty = counterparty;
  }

  public String getCounterparty() {
    return _counterparty;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept (OTCOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept (OptionSecurityVisitor<T> visitor) {
    return accept ((OTCOptionSecurityVisitor<T>)visitor);
  }

}
