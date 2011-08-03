/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;

/**
 * 
 * @param <T> The type of the object returned from the visitor
 */
public abstract class AbstractFutureSecurityVisitor<T> implements FutureSecurityVisitor<T> {

  @Override
  public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitBondFutureSecurity(final BondFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitFXFutureSecurity(final FXFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public T visitStockFutureSecurity(final StockFutureSecurity security) {
    throw new NotImplementedException();
  }
}
