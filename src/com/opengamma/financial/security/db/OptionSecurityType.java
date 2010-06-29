/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionSecurityVisitor;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;

/**
 * Type of the security.
 */
public enum OptionSecurityType {

  /** American equity. */
  AMERICAN_EQUITY,
  /** American future. */
  AMERICAN_FUTURE,
  /** European equity. */
  EUROPEAN_EQUITY,
  /** European future. */
  EUROPEAN_FUTURE,
  /** Foreign exchange. */
  FX,
  /** Powered equity. */
  POWERED_EQUITY;

  /**
   * Determines the security type from a security object.
   * @param object  the security object
   * @return the security types, not null
   */
  public static OptionSecurityType identify(OptionSecurity object) {
    return object.accept(new OptionSecurityVisitor<OptionSecurityType>() {

      @Override
      public OptionSecurityType visitAmericanVanillaEquityOptionSecurity(
          AmericanVanillaEquityOptionSecurity security) {
        return AMERICAN_EQUITY;
      }

      @Override
      public OptionSecurityType visitEuropeanVanillaEquityOptionSecurity(
          EuropeanVanillaEquityOptionSecurity security) {
        return EUROPEAN_EQUITY;
      }

      @Override
      public OptionSecurityType visitPoweredEquityOptionSecurity(
          PoweredEquityOptionSecurity security) {
        return POWERED_EQUITY;
      }

      @Override
      public OptionSecurityType visitAmericanVanillaFutureOptionSecurity(
          AmericanVanillaFutureOptionSecurity security) {
        return AMERICAN_FUTURE;
      }

      @Override
      public OptionSecurityType visitEuropeanVanillaFutureOptionSecurity(
          EuropeanVanillaFutureOptionSecurity security) {
        return EUROPEAN_FUTURE;
      }

      @Override
      public OptionSecurityType visitFXOptionSecurity(FXOptionSecurity security) {
        return FX;
      }
    });
  }

  /**
   * Vistor pattern for security types.
   * @param <T>  the security type
   */
  public static interface Visitor<T> {

    /**
     * Visits an American equity.
     * @return the result
     */
    T visitAmericanEquityOptionType();

    /**
     * Visits an American future.
     * @return the result
     */
    T visitAmericanFutureOptionType();

    /**
     * Visits an European equity.
     * @return the result
     */
    T visitEuropeanEquityOptionType();

    /**
     * Visits an European future.
     * @return the result
     */
    T visitEuropeanFutureOptionType();

    /**
     * Visits a Foreign Exchange option.
     * @return the result
     */
    T visitFXOptionType();

    /**
     * Visits a powered equity option.
     * @return the result
     */
    T visitPoweredEquityOptionType();
  }

  /**
   * Accepts the visitor.
   * @param <T>  the visitor type
   * @param visitor  the visitor
   * @return the result
   */
  public <T> T accept(final Visitor<T> visitor) {
    switch (this) {
      case AMERICAN_EQUITY:
        return visitor.visitAmericanEquityOptionType();
      case AMERICAN_FUTURE:
        return visitor.visitAmericanFutureOptionType();
      case EUROPEAN_EQUITY:
        return visitor.visitEuropeanEquityOptionType();
      case EUROPEAN_FUTURE:
        return visitor.visitEuropeanFutureOptionType();
      case FX:
        return visitor.visitFXOptionType();
      case POWERED_EQUITY:
        return visitor.visitPoweredEquityOptionType();
      default:
        throw new OpenGammaRuntimeException("unexpected enum value " + this);
    }
  }

}
