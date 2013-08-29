/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public final class PresentValueYearOnYearBlackNormalInflationCalculator extends InstrumentDerivativeVisitorDelegate<BlackSmileCapInflationYearOnYearProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueYearOnYearBlackNormalInflationCalculator INSTANCE = new PresentValueYearOnYearBlackNormalInflationCalculator();

  /**
   * Constructor.
   */
  private PresentValueYearOnYearBlackNormalInflationCalculator() {
    super(new BlackSmileCapInflationYearOnYearProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueYearOnYearBlackNormalInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod.getInstance();
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  //-----     Caplet/Floorlet Year on Year     -----

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValue(cap, black);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValue(cap, black);
  }

  //     -----      Cap/Floor Year on Year     -----
  // TO DO
}
