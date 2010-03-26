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
public class FXOptionSecurity extends OTCOptionSecurity {
  public static final String FX_OPTION_TYPE = "FX_OPTION";

  public FXOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final DomainSpecificIdentifier underlyingIdentityKey, final Currency currency,
      final String counterparty) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, counterparty);
    setSecurityType(FX_OPTION_TYPE);
  }

  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    // TODO Auto-generated method stub
    return null;
  }
}
