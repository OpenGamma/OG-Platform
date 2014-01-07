/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationZeroCouponInterpolationBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationZeroCouponMonthlyBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;

/**
 * 
 */
public final class PresentValueSensitivityZeroCouponBlackInflationCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackSmileCapInflationZeroCouponProviderInterface, MultipleCurrencyInflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSensitivityZeroCouponBlackInflationCalculator INSTANCE = new PresentValueSensitivityZeroCouponBlackInflationCalculator();

  /**
   * Constructor.
   */
  private PresentValueSensitivityZeroCouponBlackInflationCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSensitivityZeroCouponBlackInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorInflationZeroCouponInterpolationBlackSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationZeroCouponInterpolationBlackSmileMethod.getInstance();
  private static final CapFloorInflationZeroCouponMonthlyBlackSmileMethod METHOD_CAPFLOOR_MONTHLY = CapFloorInflationZeroCouponMonthlyBlackSmileMethod.getInstance();

  //-----     Cap/Floor Zero Coupon     -----

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation cap,
      final BlackSmileCapInflationZeroCouponProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValueCurveSensitivity(cap, black);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly cap, final BlackSmileCapInflationZeroCouponProviderInterface black) {
    return METHOD_CAPFLOOR_MONTHLY.presentValueCurveSensitivity(cap, black);
  }

}
