/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 */
public final class PresentValueBasisPointCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBasisPointCurveSensitivityDiscountingCalculator INSTANCE = new PresentValueBasisPointCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBasisPointCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBasisPointCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterProviderInterface multicurve) {
    return METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, multicurve.getMulticurveProvider());
  }

}
