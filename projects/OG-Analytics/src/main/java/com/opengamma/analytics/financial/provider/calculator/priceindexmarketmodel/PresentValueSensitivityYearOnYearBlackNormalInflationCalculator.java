/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;

/**
 * 
 */
public final class PresentValueSensitivityYearOnYearBlackNormalInflationCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackSmileCapInflationYearOnYearProviderInterface, MultipleCurrencyInflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSensitivityYearOnYearBlackNormalInflationCalculator INSTANCE = new PresentValueSensitivityYearOnYearBlackNormalInflationCalculator();

  /**
   * Constructor.
   */
  private PresentValueSensitivityYearOnYearBlackNormalInflationCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSensitivityYearOnYearBlackNormalInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod.getInstance();
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  //-----     Caplet/Floorlet Year on Year     -----

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation cap,
      final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValueCurveSensitivity(cap, black);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValueCurveSensitivity(cap, black);
  }

  //     -----      Cap/Floor Year on Year     -----
  // TO DO

}
