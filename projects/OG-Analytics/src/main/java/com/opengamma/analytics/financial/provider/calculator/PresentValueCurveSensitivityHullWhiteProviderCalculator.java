/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityHullWhiteProviderCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteProviderCalculator INSTANCE = new PresentValueCurveSensitivityHullWhiteProviderCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteProviderCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityHullWhiteProviderCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final PresentValueCurveSensitivityDiscountingProviderCalculator PVCSDC = PresentValueCurveSensitivityDiscountingProviderCalculator.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves);
    } catch (Exception e) {
      return derivative.accept(PVCSDC, multicurves.getMulticurveProvider());
    }
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFuture(final InterestRateFuture future, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.presentValueCurveSensitivity(future, multicurves);
  }

}
