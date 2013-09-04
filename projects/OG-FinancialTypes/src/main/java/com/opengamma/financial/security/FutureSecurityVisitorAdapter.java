/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;

/**
 * Adapter for visiting all concrete asset classes.
 *
 * @param <T> Return type for visitor.
 */
public class FutureSecurityVisitorAdapter<T> implements FutureSecurityVisitor<T> {

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitBondFutureSecurity(final BondFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }


  @Override
  public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitFXFutureSecurity(final FXFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitStockFutureSecurity(final StockFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }

  @Override
  public T visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");
  }
  
  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass().getName() + ") is not supporting " + security.getClass().getName() + " security.");    
  }
}
