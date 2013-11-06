/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.irs;

import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapNotionalVisitor;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;

/**
 * Visitor to obtain the IR notional amount.
 */
public class InterestRateSwapNotionalAmountVisitor implements InterestRateSwapNotionalVisitor<Integer,  Double> {

  public Double visitCommodityNotional(CommodityNotional notional, Integer period) {
    return null;
  }

  public Double visitInterestRateNotional(InterestRateNotional notional, Integer period) {
    return notional.getAmount();
  }

  public Double visitSecurityNotional(SecurityNotional notional, Integer period) {
    return null;
  }

  public Double visitVarianceSwapNotional(VarianceSwapNotional notional, Integer period) {
    return null;
  }

  public Double visitInterestRateSwapNotional(InterestRateSwapNotional notional, Integer period) {
    return notional.getAmount(period);
  }

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
