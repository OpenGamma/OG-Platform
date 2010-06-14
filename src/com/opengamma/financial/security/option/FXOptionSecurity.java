/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * A foreign exchange option security.
 */
public class FXOptionSecurity extends OTCOptionSecurity {

  /**
   * The security type.
   */
  public static final String FX_OPTION_TYPE = "FX_OPTION";

  private final Currency _putCurrency;
  private final Currency _callCurrency;

  public FXOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry, final Identifier underlyingIdentifier,
      final Currency domesticCurrency, final String counterparty, final Currency putCurrency, final Currency callCurrency) {
    super(FX_OPTION_TYPE, optionType, strike, expiry, underlyingIdentifier, domesticCurrency, counterparty);
    _putCurrency = putCurrency;
    _callCurrency = callCurrency;
  }

  public Currency getPutCurrency() {
    return _putCurrency;
  }

  public Currency getCallCurrency() {
    return _callCurrency;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T accept(final OTCOptionSecurityVisitor<T> visitor) {
    return visitor.visitFXOptionSecurity(this);
  }

}
