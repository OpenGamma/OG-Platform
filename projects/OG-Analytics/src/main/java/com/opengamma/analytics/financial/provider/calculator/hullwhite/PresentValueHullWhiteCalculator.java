/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueHullWhiteCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueHullWhiteCalculator INSTANCE = new PresentValueHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueHullWhiteCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves);
    } catch (Exception e) {
      return derivative.accept(PVDC, multicurves.getMulticurveProvider());
    }
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFuture(final InterestRateFuture future, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.presentValue(future, multicurves);
  }

}
