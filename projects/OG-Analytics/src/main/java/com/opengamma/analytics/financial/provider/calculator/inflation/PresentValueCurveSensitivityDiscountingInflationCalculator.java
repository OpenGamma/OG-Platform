/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
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
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculator of the present value curve sensitivity as multiple currency interest rate curve sensitivity.
 */

public final class PresentValueCurveSensitivityDiscountingInflationCalculator extends InstrumentDerivativeVisitorAdapter<InflationProviderInterface, MultipleCurrencyInflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator INSTANCE = new PresentValueCurveSensitivityDiscountingInflationCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityDiscountingInflationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityDiscountingInflationCalculator() {
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
   * Pricing method for year on year coupon fixed compounding.
   */
  private static final CouponFixedCompoundingDiscountingMethod METHOD_CPN_FIXED_COMPOUNDING = CouponFixedCompoundingDiscountingMethod.getInstance();

  // -----     Inflation Coupon     ------

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final InflationProviderInterface inflation) {
    return METHOD_ZC_MONTHLY.presentValueCurveSensitivity(coupon, inflation);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final InflationProviderInterface inflation) {
    return METHOD_ZC_INTERPOLATION.presentValueCurveSensitivity(coupon, inflation);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final InflationProviderInterface inflation) {
    return METHOD_ZC_MONTHLY_GEARING.presentValueCurveSensitivity(coupon, inflation);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon,
      final InflationProviderInterface inflation) {
    return METHOD_ZC_INTERPOLATION_GEARING.presentValueCurveSensitivity(coupon, inflation);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final InflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_MONTHLY.presentValueCurveSensitivity(coupon, inflation);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    return METHOD_YEAR_ON_YEAR_INTERPOLATION.presentValueCurveSensitivity(coupon, inflation);
  }

  /*@Override
  public MultipleCurrencyInflationSensitivity visitCouponFixedCompounding(final CouponFixedCompounding payment, final InflationProviderInterface inflation) {
    return METHOD_CPN_FIXED_COMPOUNDING.presentValueCurveSensitivity(payment, inflation);
  }*/

  //-----     Annuity     ------

  @Override
  public MultipleCurrencyInflationSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(inflation, "inflation");
    MultipleCurrencyInflationSensitivity cs = annuity.getNthPayment(0).accept(this, inflation);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(annuity.getNthPayment(loopp).accept(this, inflation));
    }
    return cs;
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final InflationProviderInterface inflation) {
    return visitGenericAnnuity(annuity, inflation);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyInflationSensitivity visitSwap(final Swap<?, ?> swap, final InflationProviderInterface inflation) {
    final MultipleCurrencyInflationSensitivity sensitivity1 = swap.getFirstLeg().accept(this, inflation);
    final MultipleCurrencyInflationSensitivity sensitivity2 = swap.getSecondLeg().accept(this, inflation);
    return sensitivity1.plus(sensitivity2);
  }

  @Override
  public MultipleCurrencyInflationSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final InflationProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

}
