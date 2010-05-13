/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 */
public abstract class OptionSecurity extends FinancialSecurity implements Option {
  private final OptionType _optionType;
  private final double _strike;
  private final Expiry _expiry;
  private final Identifier _underlyingIdentityKey;
  private final Currency _currency;

  public OptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentityKey, final Currency currency) {
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentityKey = underlyingIdentityKey;
    _currency = currency;
  }

  /**
   * @return the optionType
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @return the expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  public Identifier getUnderlyingIdentityKey() {
    return _underlyingIdentityKey;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public abstract <T> T accept(OptionVisitor<T> visitor);

  public abstract <T> T accept(OptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept (final FinancialSecurityVisitor<T> visitor) {
    return accept ((OptionSecurityVisitor<T>)visitor);
  }

}
