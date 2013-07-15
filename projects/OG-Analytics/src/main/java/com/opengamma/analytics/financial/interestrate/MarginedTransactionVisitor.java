/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;

/**
 * Provides the reference margin price,
 * for futures, options and other exchange traded securities that are margined
 */
public class MarginedTransactionVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Boolean> {

  /** The method unique instance. */
  private static final MarginedTransactionVisitor INSTANCE = new MarginedTransactionVisitor();

  /** @return the unique instance of the class */
  public static MarginedTransactionVisitor getInstance() {
    return INSTANCE;
  }

  /** Constructor. */
  MarginedTransactionVisitor() {
  }

  @Override
  public Boolean visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    return true;
  }

  @Override
  public Boolean visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return true;
  }

  @Override
  public Boolean visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final YieldCurveBundle curves) {
    return true;
  }

  @Override
  public Boolean visitBondFuture(BondFuture future, final YieldCurveBundle curves) {
    return true;
  }
}
