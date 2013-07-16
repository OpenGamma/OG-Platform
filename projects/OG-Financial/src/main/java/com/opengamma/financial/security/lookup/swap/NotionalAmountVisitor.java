/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;

/**
 * Visitor to obtain the IR notional amount.
 */
/* package */ class NotionalAmountVisitor implements NotionalVisitor<Double> {

  @Override
  public Double visitCommodityNotional(CommodityNotional notional) {
    return null;
  }

  @Override
  public Double visitInterestRateNotional(InterestRateNotional notional) {
    return notional.getAmount();
  }

  @Override
  public Double visitSecurityNotional(SecurityNotional notional) {
    return null;
  }

  @Override
  public Double visitVarianceSwapNotional(VarianceSwapNotional notional) {
    return null;
  }

}
