/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBjerksundStenslandCalculator extends PresentValueCalculator {
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();

  @Override
  public Double visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return PREMIUM_BOND_FUTURE_OPTION.presentValue(option, curves).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitBondFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }
}
