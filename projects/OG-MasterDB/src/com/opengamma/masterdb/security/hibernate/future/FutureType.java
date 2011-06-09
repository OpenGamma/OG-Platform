/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;

/**
 * Hibernate bean for storage.
 */
public enum FutureType {
  /** Agriculture. */
  AGRICULTURE,
  /** Bond. */
  BOND,
  /** Foreign exchange. */
  FX,
  /** Energy. */
  ENERGY,
  /** Interest rate. */
  INTEREST_RATE,
  /** Metal. */
  METAL,
  /** Stock. */
  STOCK,
  /** Index. */
  INDEX,
  /** Equity. */
  EQUITY,
  /** Equity Index Dividend. */
  EQUITY_INDEX_DIVIDEND;

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
      public FutureType visitEquityFutureSecurity(EquityFutureSecurity security) {
        // TODO Case: Confirm my add of this fix is fine
        return EQUITY;
      }

      @Override
      public FutureType visitEquityIndexDividendFutureSecurity(
          EquityIndexDividendFutureSecurity security) {
        // TODO Case: Confirm my add of this fix is fine
        return EQUITY_INDEX_DIVIDEND;
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

  /**
   * Visitor.
   */
  public static interface Visitor<T> {
    T visitAgricultureFutureType();

    T visitBondFutureType();

    T visitFXFutureType();

    T visitEnergyFutureType();

    T visitInterestRateFutureType();

    T visitMetalFutureType();

    T visitIndexFutureType();

    T visitStockFutureType();

    T visitEquityFutureType();

    T visitEquityIndexDividendFutureType();
  }

  public <T> T accept(final Visitor<T> visitor) {
    switch (this) {
      case AGRICULTURE:
        return visitor.visitAgricultureFutureType();
      case BOND:
        return visitor.visitBondFutureType();
      case ENERGY:
        return visitor.visitEnergyFutureType();
      case EQUITY:
        return visitor.visitEquityFutureType();
      case EQUITY_INDEX_DIVIDEND:
        return visitor.visitEquityIndexDividendFutureType();
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
