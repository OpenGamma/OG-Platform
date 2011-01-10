/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.BondOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OptionOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionSecurityVisitor;
import com.opengamma.financial.security.option.SwapOptionSecurity;

/**
 * Type of the security.
 */
public enum OptionSecurityType {

  /** Bond */
  BOND,
  /** Equity */
  EQUITY,
  /** Future */
  FUTURE,
  /** FX */
  FX,
  /** Option */
  OPTION,
  /** Swap */
  SWAP;

  /**
   * Determines the security type from a security object.
   * @param object  the security object
   * @return the security types, not null
   */
  public static OptionSecurityType identify(OptionSecurity object) {
    return object.accept(new OptionSecurityVisitor<OptionSecurityType>() {

      @Override
      public OptionSecurityType visitBondOptionSecurity(BondOptionSecurity security) {
        return BOND;
      }

      @Override
      public OptionSecurityType visitEquityOptionSecurity(EquityOptionSecurity security) {
        return EQUITY;
      }

      @Override
      public OptionSecurityType visitFutureOptionSecurity(FutureOptionSecurity security) {
        return FUTURE;
      }

      @Override
      public OptionSecurityType visitFXOptionSecurity(FXOptionSecurity security) {
        return FX;
      }

      @Override
      public OptionSecurityType visitOptionOptionSecurity(OptionOptionSecurity security) {
        return OPTION;
      }

      @Override
      public OptionSecurityType visitSwapOptionSecurity(SwapOptionSecurity security) {
        return SWAP;
      }
    });
  }

  /**
   * Accepts the visitor.
   * @param <T>  the visitor type
   * @param visitor  the visitor
   * @return the result
   */
  public <T> T accept(final OptionSecurityVisitor<T> visitor) {
    switch (this) {
      case BOND:
        return visitor.visitBondOptionSecurity(null);
      case EQUITY:
        return visitor.visitEquityOptionSecurity(null);
      case FUTURE:
        return visitor.visitFutureOptionSecurity(null);
      case FX:
        return visitor.visitFXOptionSecurity(null);
      case OPTION:
        return visitor.visitOptionOptionSecurity(null);
      case SWAP:
        return visitor.visitSwapOptionSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected enum value " + this);
    }
  }

}
