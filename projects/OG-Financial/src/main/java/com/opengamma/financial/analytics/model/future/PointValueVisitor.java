/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
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
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * If a FinancialSecurity has a pointValue, or unitNotional, attached to the contract,
 * this visitor will provide it.
 */
public class PointValueVisitor extends FinancialSecurityVisitorAdapter<Double> {
  
  private static final PointValueVisitor INSTANCE = new PointValueVisitor();
  
  public static final PointValueVisitor getInstance() {
    return INSTANCE;
  }
    
  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitBondFutureSecurity(final BondFutureSecurity security) {
    return security.getUnitAmount();
  }


  @Override
  public Double visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitFXFutureSecurity(final FXFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitStockFutureSecurity(final StockFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return security.getUnitAmount();
  }
  
  @Override
  public Double visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return security.getUnitAmount();
  }  

  @Override  
  public Double visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return security.getPointValue();
  }
  @Override
  public Double visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return security.getPointValue();
  }
  
  @Override
  public Double visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return security.getPointValue();
  }
  
  @Override
  public Double visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return security.getPointValue();
  }
  
  @Override
  public Double visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return security.getPointValue();
  }

  @Override
  public Double visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return security.getPointValue();
  }
  
}
