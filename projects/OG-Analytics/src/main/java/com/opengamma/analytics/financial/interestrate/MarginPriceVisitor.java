/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;

/**
 * Provides the reference margin price,
 * for futures, options and other exchange traded securities that are margined. <p>
 * This is typically last night's close price, but may, on the trade date itself, be the trade price.<p>
 */
public class MarginPriceVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {

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
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    return option.getReferencePrice();
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    return future.getReferencePrice();
  }

  @Override
  public Double visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    return future.getReferencePrice();
  }

  @Override
  public Double visitBondFuture(final BondFuture future) {
    return future.getReferencePrice();
  }

}
