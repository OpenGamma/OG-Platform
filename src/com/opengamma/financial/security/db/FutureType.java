/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.FutureSecurityVisitor;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;

public enum FutureType {
  AGRICULTURE,
  BOND,
  FX,
  ENERGY,
  INTEREST_RATE,
  METAL;
  
  public static FutureType identify (final FutureSecurity object) {
    return object.accept (new FutureSecurityVisitor<FutureType> () {

      @Override
      public FutureType visitBondFutureSecurity(BondFutureSecurity security) {
        return BOND;
      }

      @Override
      public FutureType visitFXFutureSecurity(
          FXFutureSecurity security) {
        return FX;
      }

      @Override
      public FutureType visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        return INTEREST_RATE;
      }

      @Override
      public FutureType visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        return AGRICULTURE;
      }

      @Override
      public FutureType visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return ENERGY;
      }

      @Override
      public FutureType visitMetalFutureSecurity(MetalFutureSecurity security) {
        return METAL;
      }

    });
  }
  
  public static interface Visitor<T> {
    public T visitAgricultureFutureType ();
    public T visitBondFutureType ();
    public T visitCurrencyFutureType ();
    public T visitEnergyFutureType ();
    public T visitInterestRateFutureType ();
    public T visitMetalFutureType ();
  }
  
  public <T> T accept (final Visitor<T> visitor) {
    switch (this) {
    case AGRICULTURE : return visitor.visitAgricultureFutureType ();
    case BOND : return visitor.visitBondFutureType ();
    case ENERGY : return visitor.visitEnergyFutureType ();
    case FX : return visitor.visitCurrencyFutureType ();
    case INTEREST_RATE : return visitor.visitInterestRateFutureType ();
    case METAL : return visitor.visitMetalFutureType ();
    default : throw new OpenGammaRuntimeException ("unexpected FutureType: " + this);
    }
  }
  
}
