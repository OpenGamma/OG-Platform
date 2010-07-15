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
 * A security modeling an option.
 */
public abstract class OptionSecurity extends FinancialSecurity implements Option {

  /**
   * 
   */
  protected static final String OPTIONTYPE_KEY = "optionType";
  /**
   * 
   */
  protected static final String STRIKE_KEY = "strike";
  /**
   * 
   */
  protected static final String EXPIRY_KEY = "expiry";
  /**
   * 
   */
  protected static final String UNDERLYINGIDENTIFIER_KEY = "underlyingIdentifier";
  /**
   * 
   */
  protected static final String CURRENCY_KEY = "currency";

  /** The option type. */
  private final OptionType _optionType;
  /** The strike. */
  private final double _strike;
  /** The expiry. */
  private final Expiry _expiry;
  /** The underlying identifier. */
  private final Identifier _underlyingIdentifier;
  /** The currency. */
  private final Currency _currency;

  /**
   * Creates an option.
   * @param securityType  the security type
   * @param optionType  the option type
   * @param strike  the strike
   * @param expiry  the expiry
   * @param underlyingIdentifier  the underlying identifier
   * @param currency  the currency
   */
  public OptionSecurity(
      final String securityType, final OptionType optionType, final double strike,
      final Expiry expiry, final Identifier underlyingIdentifier, final Currency currency) {
    super(securityType);
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentifier = underlyingIdentifier;
    _currency = currency;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the option type.
   * @return the option type
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * Gets the underlying identifier.
   * @return the underlying identifier
   */
  public Identifier getUnderlyingIdentifier() {
    return _underlyingIdentifier;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept(OptionVisitor<T> visitor);

  public abstract <T> T accept(OptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitOptionSecurity(this);
  }

}
