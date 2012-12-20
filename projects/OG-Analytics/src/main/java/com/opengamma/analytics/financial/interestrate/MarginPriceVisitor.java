/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;

/**
 * Provides the reference margin price,
 * for futures, options and other exchange traded securities that are margined
 */
public class MarginPriceVisitor extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /** The method unique instance. */
  private static final MarginPriceVisitor INSTANCE = new MarginPriceVisitor();

  /** @return the unique instance of the class */
  public static MarginPriceVisitor getInstance() {
    return INSTANCE;
  }

  /** Constructor. */
  MarginPriceVisitor() {
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    return option.getReferencePrice();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return future.getReferencePrice();
  }

  @Override
  public Double visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final YieldCurveBundle curves) {
    return future.getReferencePrice();
  }

  @Override
  public Double visitBondFuture(BondFuture future, final YieldCurveBundle curves) {
    return future.getReferencePrice();
  }
}
