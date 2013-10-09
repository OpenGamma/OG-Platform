/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationZeroCouponInterpolationBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationZeroCouponMonthlyBlackSmileMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel.BlackSmileCapInflationZeroCouponProviderAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount for inflation zero coupon cap floor without convexity adjustment.
 */
public final class PresentValueBlackSmileInflationZeroCouponCalculator extends InstrumentDerivativeVisitorDelegate<BlackSmileCapInflationZeroCouponProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSmileInflationZeroCouponCalculator INSTANCE = new PresentValueBlackSmileInflationZeroCouponCalculator();

  /**
   * Constructor.
   */
  private PresentValueBlackSmileInflationZeroCouponCalculator() {
    super(new BlackSmileCapInflationZeroCouponProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSmileInflationZeroCouponCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */

  private static final CapFloorInflationZeroCouponInterpolationBlackSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationZeroCouponInterpolationBlackSmileMethod.getInstance();
  private static final CapFloorInflationZeroCouponMonthlyBlackSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationZeroCouponMonthlyBlackSmileMethod.getInstance();

  //-----     Cap/Floor Zero Coupon     -----

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation cap, final BlackSmileCapInflationZeroCouponProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValue(cap, black);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly cap, final BlackSmileCapInflationZeroCouponProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValue(cap, black);
  }

}
