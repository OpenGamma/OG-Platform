/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.irs;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapNotionalVisitor;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;

/**
 * Visitor to obtain the IR notional amount.
 */
public class InterestRateSwapNotionalAmountVisitor implements InterestRateSwapNotionalVisitor<LocalDate,  Double> {

  @Override
  public Double visitCommodityNotional(CommodityNotional notional, LocalDate period) {
    return null;
  }

  @Override
  public Double visitInterestRateNotional(InterestRateNotional notional, LocalDate period) {
    return notional.getAmount();
  }

  @Override
  public Double visitSecurityNotional(SecurityNotional notional, LocalDate period) {
    return null;
  }

  @Override
  public Double visitVarianceSwapNotional(VarianceSwapNotional notional, LocalDate period) {
    return null;
  }

  @Override
  public Double visitInterestRateSwapNotional(InterestRateSwapNotional notional, LocalDate period) {
    return notional.getAmount(period);
  }

  @Override
  public Double visitInterestRateSwapNotional(InterestRateSwapNotional notional) {
    throw new UnsupportedOperationException("InterestRateSwapNotional requires a period to get the notional for: " + notional);
  }

  @Override
  public Double visitInterestRateNotional(InterestRateNotional notional) {
    return notional.getAmount();
  }



  @Override
  public Double visitCommodityNotional(CommodityNotional notional) {
    throw new UnsupportedOperationException(notional + " not supported by InterestRateSwapNotionalAmountVisitor visitor");
  }

  @Override
  public Double visitSecurityNotional(SecurityNotional notional) {
    throw new UnsupportedOperationException(notional + " not supported by InterestRateSwapNotionalAmountVisitor visitor");
  }

  @Override
  public Double visitVarianceSwapNotional(VarianceSwapNotional notional) {
    throw new UnsupportedOperationException(notional + " not supported by InterestRateSwapNotionalAmountVisitor visitor");
  }
}
