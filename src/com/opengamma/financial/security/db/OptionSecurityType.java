/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

public enum OptionSecurityType {
  AMERICAN_EQUITY,
  AMERICAN_FUTURE,
  EUROPEAN_EQUITY,
  EUROPEAN_FUTURE,
  FX,
  POWERED_EQUITY;
  
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
  
  public static interface Visitor<T> {
    public T visitAmericanEquityOptionType ();
    public T visitAmericanFutureOptionType ();
    public T visitEuropeanEquityOptionType ();
    public T visitEuropeanFutureOptionType ();
    public T visitFXOptionType ();
    public T visitPoweredEquityOptionType ();
  }
  
  public <T,V> T accept (final Visitor<T> visitor) {
    switch (this) {
    case AMERICAN_EQUITY :
      return visitor.visitAmericanEquityOptionType ();
    case AMERICAN_FUTURE :
      return visitor.visitAmericanFutureOptionType ();
    case EUROPEAN_EQUITY :
      return visitor.visitEuropeanEquityOptionType ();
    case EUROPEAN_FUTURE :
      return visitor.visitEuropeanFutureOptionType ();
    case FX :
      return visitor.visitFXOptionType ();
    case POWERED_EQUITY :
      return visitor.visitPoweredEquityOptionType ();
    default :
      throw new OpenGammaRuntimeException ("unexpected enum value " + this);
    }
  }
  
}