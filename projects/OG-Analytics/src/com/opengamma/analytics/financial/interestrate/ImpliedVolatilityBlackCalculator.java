/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;

/**
 * Interpolates, for interest rate instruments using Black model, and returns the implied volatility required.
 */
public final class ImpliedVolatilityBlackCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The method unique instance.
   */
  private static final ImpliedVolatilityBlackCalculator INSTANCE = new ImpliedVolatilityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ImpliedVolatilityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ImpliedVolatilityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod METHOD_MARGINED_FUTUREOPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_PHYSICAL.impliedVolatility(swaption, curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final YieldCurveBundle curves) {
    return METHOD_MARGINED_FUTUREOPTION.impliedVolatility(option, curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    return METHOD_MARGINED_FUTUREOPTION.impliedVolatility(option.getUnderlyingOption(), curves);
  }

}
