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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityHullWhiteCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteCalculator INSTANCE = new PresentValueCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

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
