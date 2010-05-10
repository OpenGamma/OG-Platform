/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * A powered equity option security.
 */
public class PoweredEquityOptionSecurity extends EquityOptionSecurity
    implements PoweredOption {

  private final double _power;

  /**
   * Creates the security.
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentifier
   * @param currency
   * @param exchange
   */
  public PoweredEquityOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final double power, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, exchange);
    _power = power;
  }

  //-------------------------------------------------------------------------
  @Override
  public double getPower() {
    return _power;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(OptionVisitor<T> visitor) {
    return visitor.visitPoweredOption(this);
  }

  @Override
  public <T> T accept(EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitPoweredEquityOptionSecurity(this);
  }

}
