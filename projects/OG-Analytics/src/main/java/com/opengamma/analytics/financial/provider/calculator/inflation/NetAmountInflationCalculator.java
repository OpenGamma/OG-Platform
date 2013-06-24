/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationYearOnYearInterpolationDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationYearOnYearMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the estimated net amount of an inflation (linear) instruments for a given InflationProvider.
 */

public final class NetAmountInflationCalculator extends InstrumentDerivativeVisitorDelegate<InflationProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final NetAmountInflationCalculator INSTANCE = new NetAmountInflationCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static NetAmountInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private NetAmountInflationCalculator() {
    super(new InflationProviderAdapter<MultipleCurrencyAmount>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationZeroCouponMonthlyDiscountingMethod METHOD_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyDiscountingMethod();
  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationZeroCouponInterpolationDiscountingMethod METHOD_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationDiscountingMethod();
  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD_ZC_MONTHLY_GEARING = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD_ZC_INTERPOLATION_GEARING = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();

  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationYearOnYearMonthlyDiscountingMethod METHOD_YEAR_ON_YEAR_MONTHLY = new CouponInflationYearOnYearMonthlyDiscountingMethod();

  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationYearOnYearInterpolationDiscountingMethod METHOD_YEAR_ON_YEAR_INTERPOLATION = new CouponInflationYearOnYearInterpolationDiscountingMethod();

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final InflationProviderInterface market) {
    return METHOD_ZC_MONTHLY.netAmount(coupon, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final InflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION.netAmount(coupon, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final InflationProviderInterface market) {
    return METHOD_ZC_MONTHLY_GEARING.netAmount(coupon, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION_GEARING.netAmount(coupon, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final InflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_MONTHLY.netAmount(coupon, market);
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION.netAmount(coupon, market);
  }

}
