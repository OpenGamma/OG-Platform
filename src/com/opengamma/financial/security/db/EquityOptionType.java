/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquityOptionSecurityVisitor;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;

public enum EquityOptionType {
  AMERICAN,
  EUROPEAN,
  POWERED;
  
  public static EquityOptionType identify (EquityOptionSecurity object) {
    return object.accept (new EquityOptionSecurityVisitor<EquityOptionType> () {

      @Override
      public EquityOptionType visitAmericanVanillaEquityOptionSecurity(
          AmericanVanillaEquityOptionSecurity security) {
        return AMERICAN;
      }

      @Override
      public EquityOptionType visitEuropeanVanillaEquityOptionSecurity(
          EuropeanVanillaEquityOptionSecurity security) {
        return EUROPEAN;
      }

      @Override
      public EquityOptionType visitPoweredEquityOptionSecurity(
          PoweredEquityOptionSecurity security) {
        return POWERED;
      }
    });
  }
  
  public static interface Visitor<T> {
    public T visitAmericanEquityOptionType ();
    public T visitEuropeanEquityOptionType ();
    public T visitPoweredEquityOptionType ();
  }
  
  public <T,V> T accept (final Visitor<T> visitor) {
    switch (this) {
    case AMERICAN :
      return visitor.visitAmericanEquityOptionType ();
    case EUROPEAN :
      return visitor.visitEuropeanEquityOptionType ();
    case POWERED :
      return visitor.visitPoweredEquityOptionType ();
    default :
      throw new OpenGammaRuntimeException ("unexpected enum value " + this);
    }
  }
  
}