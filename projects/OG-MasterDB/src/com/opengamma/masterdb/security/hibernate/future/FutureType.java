/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;

public enum FutureType {
  AGRICULTURE,
  BOND,
  FX,
  ENERGY,
  INTEREST_RATE,
  METAL,
  STOCK,
  INDEX;
  
  public static FutureType identify(final FutureSecurity object) {
    return object.accept(new FutureSecurityVisitor<FutureType>() {

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

      @Override
      public FutureType visitIndexFutureSecurity(IndexFutureSecurity security) {
        return INDEX;
      }

      @Override
      public FutureType visitStockFutureSecurity(StockFutureSecurity security) {
        return STOCK;
      }

    });
  }
  
  public static interface Visitor<T> {
    T visitAgricultureFutureType();
    T visitBondFutureType();
    T visitFXFutureType();
    T visitEnergyFutureType();
    T visitInterestRateFutureType();
    T visitMetalFutureType();
    T visitIndexFutureType();
    T visitStockFutureType();
  }
  
  public <T> T accept(final Visitor<T> visitor) {
    switch (this) {
      case AGRICULTURE:
        return visitor.visitAgricultureFutureType();
      case BOND:
        return visitor.visitBondFutureType();
      case ENERGY:
        return visitor.visitEnergyFutureType();
      case FX:
        return visitor.visitFXFutureType();
      case INTEREST_RATE:
        return visitor.visitInterestRateFutureType();
      case METAL:
        return visitor.visitMetalFutureType();
      case INDEX:
        return visitor.visitIndexFutureType();
      case STOCK:
        return visitor.visitStockFutureType();
      default:
        throw new OpenGammaRuntimeException("unexpected FutureType: " + this);
    }
  }
  
}
