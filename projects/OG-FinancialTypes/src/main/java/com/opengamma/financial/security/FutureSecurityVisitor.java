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
 * @param <T> The return type of the visitor
 */
public interface FutureSecurityVisitor<T> {

  T visitAgricultureFutureSecurity(AgricultureFutureSecurity security);

  T visitBondFutureSecurity(BondFutureSecurity security);

  T visitEnergyFutureSecurity(EnergyFutureSecurity security);

  T visitEquityFutureSecurity(EquityFutureSecurity security);

  T visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security);

  T visitFXFutureSecurity(FXFutureSecurity security);

  T visitIndexFutureSecurity(IndexFutureSecurity security);

  T visitInterestRateFutureSecurity(InterestRateFutureSecurity security);

  T visitMetalFutureSecurity(MetalFutureSecurity security);

  T visitStockFutureSecurity(StockFutureSecurity security);

  T visitDeliverableSwapFutureSecurity(DeliverableSwapFutureSecurity security);
  
  T visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security);
}
