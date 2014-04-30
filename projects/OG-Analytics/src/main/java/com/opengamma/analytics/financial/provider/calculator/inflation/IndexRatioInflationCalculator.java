/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationYearOnYearInterpolationDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationYearOnYearInterpolationWithMarginDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationYearOnYearMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationYearOnYearMonthlyWithMarginDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationZeroCouponInterpolationDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationZeroCouponInterpolationGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationZeroCouponMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;

/**
 * 
 */
public final class IndexRatioInflationCalculator extends InstrumentDerivativeVisitorAdapter<InflationProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final IndexRatioInflationCalculator INSTANCE = new IndexRatioInflationCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static IndexRatioInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private IndexRatioInflationCalculator() {
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
   * Pricing method for year on year coupon with monthly reference index.
   */
  private static final CouponInflationYearOnYearMonthlyDiscountingMethod METHOD_YEAR_ON_YEAR_MONTHLY = new CouponInflationYearOnYearMonthlyDiscountingMethod();
  /**
   * Pricing method for year on year coupon with interpolated reference index.
   */
  private static final CouponInflationYearOnYearInterpolationDiscountingMethod METHOD_YEAR_ON_YEAR_INTERPOLATION = new CouponInflationYearOnYearInterpolationDiscountingMethod();

  /**
   * Pricing method for year on year coupon with monthly and with margin reference index.
   */
  private static final CouponInflationYearOnYearMonthlyWithMarginDiscountingMethod METHOD_YEAR_ON_YEAR_MONTHLY_WITH_MARGIN = new CouponInflationYearOnYearMonthlyWithMarginDiscountingMethod();
  /**
   * Pricing method for year on year coupon with interpolated and with margin reference index.
   */
  private static final CouponInflationYearOnYearInterpolationWithMarginDiscountingMethod METHOD_YEAR_ON_YEAR_INTERPOLATION_WITH_MARGIN =
      new CouponInflationYearOnYearInterpolationWithMarginDiscountingMethod();

  @Override
  public Double visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final InflationProviderInterface market) {
    return METHOD_ZC_MONTHLY.indexEstimation(coupon, market) / coupon.getIndexStartValue();
  }

  @Override
  public Double visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final InflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION.indexEstimation(coupon, market) / coupon.getIndexStartValue();
  }

  @Override
  public Double visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final InflationProviderInterface market) {
    return METHOD_ZC_MONTHLY_GEARING.indexEstimation(coupon, market) / coupon.getIndexStartValue();
  }

  @Override
  public Double visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION_GEARING.indexEstimation(coupon, market) / coupon.getIndexStartValue();
  }

  @Override
  public Double visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final InflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_MONTHLY.indexEstimationEnd(coupon, market) / METHOD_YEAR_ON_YEAR_MONTHLY.indexEstimationStart(coupon, market);
  }

  @Override
  public Double visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION.indexEstimationEnd(coupon, market) / METHOD_YEAR_ON_YEAR_INTERPOLATION.indexEstimationStart(coupon, market);
  }

  @Override
  public Double visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final InflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_MONTHLY_WITH_MARGIN.indexEstimationEnd(coupon, inflation) / METHOD_YEAR_ON_YEAR_MONTHLY_WITH_MARGIN.indexEstimationStart(coupon, inflation);
  }

  @Override
  public Double visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon,
      final InflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION_WITH_MARGIN.indexEstimationEnd(coupon, inflation) / METHOD_YEAR_ON_YEAR_INTERPOLATION_WITH_MARGIN.indexEstimationStart(coupon, inflation);
  }

}
