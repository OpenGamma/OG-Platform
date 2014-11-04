/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the estimated net amount of an inflation (linear) instruments for a given InflationProvider.
 */

public final class NetAmountInflationCalculator 
  extends InstrumentDerivativeVisitorDelegate<ParameterInflationProviderInterface, MultipleCurrencyAmount> {

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
    super(new InflationProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
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
  public MultipleCurrencyAmount visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final ParameterInflationProviderInterface market) {
    return METHOD_ZC_MONTHLY.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final ParameterInflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final ParameterInflationProviderInterface market) {
    return METHOD_ZC_MONTHLY_GEARING.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final ParameterInflationProviderInterface market) {
    return METHOD_ZC_INTERPOLATION_GEARING.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final ParameterInflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_MONTHLY.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final ParameterInflationProviderInterface market) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION.netAmount(coupon, market.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final ParameterInflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_MONTHLY_WITH_MARGIN.netAmount(coupon, inflation.getInflationProvider());
  }

  @Override
  public MultipleCurrencyAmount visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon,
      final ParameterInflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION_WITH_MARGIN.netAmount(coupon, inflation.getInflationProvider());
  }
}
