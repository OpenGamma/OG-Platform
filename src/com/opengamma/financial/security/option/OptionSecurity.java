/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * A security modeling an option.
 */
public abstract class OptionSecurity extends FinancialSecurity implements Option {

  private final OptionType _optionType;
  private final double _strike;
  private final Expiry _expiry;
  private final UniqueIdentifier _underlyingIdentifier;
  private final Currency _currency;

  /**
   * Creates a security.
   * @param securityType
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentityKey
   * @param currency
   */
  public OptionSecurity(final String securityType, final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier, final Currency currency) {
    super(securityType);
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentifier = underlyingIdentifier;
    _currency = currency;
  }

  //-------------------------------------------------------------------------
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

  public UniqueIdentifier getUnderlyingSecurity() {
    return _underlyingIdentifier;
  }

  public Currency getCurrency() {
    return _currency;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept(OptionVisitor<T> visitor);

  public abstract <T> T accept(OptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return accept((OptionSecurityVisitor<T>) visitor);
  }

}
