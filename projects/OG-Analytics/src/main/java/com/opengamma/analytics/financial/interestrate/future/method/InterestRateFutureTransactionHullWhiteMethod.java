/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005.
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureTransactionHullWhiteMethod}
 */
@Deprecated
public final class InterestRateFutureTransactionHullWhiteMethod extends InterestRateFutureTransactionMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureTransactionHullWhiteMethod INSTANCE = new InterestRateFutureTransactionHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureTransactionHullWhiteMethod() {
  }

  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_SECURITY = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  //  /**
  //   * The Hull-White model.
  //   */
  //  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    final double pv = presentValueFromPrice(future, METHOD_SECURITY.price(future.getUnderlyingFuture(), curves));
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future");
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle with Hull-White data");
    return presentValue((InterestRateFutureTransaction) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  @Override
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return presentValueCurveSensitivity(future, METHOD_SECURITY.priceCurveSensitivity(future.getUnderlyingFuture(), curves));
  }

}
