/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/** Calculates the contract multiplier, or notional of a single contract. This is used for converting Mathematical Greeks to Position Greeks,
 * the former simply being the latter for a notional of 1.0/
 */
public final class PositionGreekContractMultiplier extends FinancialSecurityVisitorAdapter<Double> {

  /** Static instance */
  private static final PositionGreekContractMultiplier s_instance = new PositionGreekContractMultiplier();

  /**
   * Gets an instance of this calculator
   * @return The (singleton) instance
   */
  public static PositionGreekContractMultiplier getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PositionGreekContractMultiplier() {
  }

  @Override
  public Double visitEquitySecurity(final EquitySecurity security) {
    return 1.0;
  }

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitStockFutureSecurity(final StockFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitBondFutureSecurity(final BondFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return security.getUnitAmount();
  }

  @Override
  public Double visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return security.getPointValue();
  }

  @Override
  public Double visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return security.getPointValue();
  }

  @Override
  public Double visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return security.getPointValue();
  }

  @Override
  public Double visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return security.getPointValue();
  }

  @Override
  public Double visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return security.getPointValue();
  }

}
