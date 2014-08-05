/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.util.ArgumentChecker;

/**
 * Extension of the LastTimeCalculator that 
 */
public class LastTimeIndexCalculator extends LastTimeCalculator {

  private static final IborIndexVisitor INDEX_VISITOR = IborIndexVisitor.getInstance();
  
  private IndexDeposit _baseLeg;
  
  public LastTimeIndexCalculator(IndexDeposit baseLeg) {
    _baseLeg = ArgumentChecker.notNull(baseLeg, "baseLeg");
  }
  
  @Override
  public Double visitSwap(Swap<?, ?> swap) {
    final double a = swap.getFirstLeg().accept(this);
    final double b = swap.getSecondLeg().accept(this);
    IndexDeposit firstLeg = swap.getFirstLeg().getNthPayment(0).accept(INDEX_VISITOR);
    IndexDeposit secondLeg = swap.getSecondLeg().getNthPayment(0).accept(INDEX_VISITOR);
    if (firstLeg != null && secondLeg != null) {
      if (_baseLeg.equals(firstLeg)) {
        return a;
      } else if (_baseLeg.equals(secondLeg)) {
        return b;
      }
    }
    return Math.max(a, b);
  }
}
