/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.future.*;

/**
 * Adapter for visiting all concrete asset classes.
 *
 * @param <T> Return type for visitor.
 */
public class FutureSecurityVisitorAdapter<T> implements FutureSecurityVisitor<T> {

  @Override
  public T visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitBondFutureSecurity(BondFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }


  @Override
  public T visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitEquityFutureSecurity(EquityFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitFXFutureSecurity(FXFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitIndexFutureSecurity(IndexFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitMetalFutureSecurity(MetalFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitStockFutureSecurity(StockFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }
}
