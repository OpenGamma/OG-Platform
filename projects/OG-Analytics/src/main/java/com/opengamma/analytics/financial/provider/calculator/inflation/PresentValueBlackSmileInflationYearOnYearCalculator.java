/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.method.CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel.BlackSmileCapInflationYearOnYearProviderAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Calculator of the present value as a multiple currency amount for inflation year on year cap floor without convexity adjustment.
 */
public final class PresentValueBlackSmileInflationYearOnYearCalculator extends InstrumentDerivativeVisitorDelegate<BlackSmileCapInflationYearOnYearProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSmileInflationYearOnYearCalculator INSTANCE = new PresentValueBlackSmileInflationYearOnYearCalculator();

  /**
   * Constructor.
   */
  private PresentValueBlackSmileInflationYearOnYearCalculator() {
    super(new BlackSmileCapInflationYearOnYearProviderAdapter<MultipleCurrencyAmount>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSmileInflationYearOnYearCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */

  private static final CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod.getInstance();
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  //-----     Caplet/Floorlet year on year     -----

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValue(cap, black);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValue(cap, black);
  }

  //-----     Cap/Floor year on year     -----
  //  TODO :  implementation of cap/floor
}
