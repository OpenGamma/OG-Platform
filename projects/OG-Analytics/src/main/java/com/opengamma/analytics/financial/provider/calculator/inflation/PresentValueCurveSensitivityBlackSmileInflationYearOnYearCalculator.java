/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;

/**
 * Calculator of the present value curve sensitivity as a multiple currency amount for inflation year on year cap floor without convexity adjustment.
 */
public final class PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackSmileCapInflationYearOnYearProviderInterface, MultipleCurrencyInflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator INSTANCE = new PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackSmileInflationYearOnYearCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */

  private static final CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod.getInstance();
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  //-----     Caplet/Floorlet year on year     -----

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation cap,
      final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValueCurveSensitivity(cap, black);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValueCurveSensitivity(cap, black);
  }

  //-----     Cap/Floor year on year     -----
  //  TODO :  implementation of cap/floor

}
