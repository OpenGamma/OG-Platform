/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationConvexityAdjustmentProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the inflation convexity adjustment.
 */
@Test(groups = TestGroup.UNIT)
public class InflationMarketModelConvexityAdjustmentCouponTest {

  // Calibration of the volatility used in the convextity adjutmnent.
  // We use Zero coupon caps/floors to calibrate those volatilities.
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 10000;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final ZonedDateTime PAYMENT_DATE_YOY = ScheduleCalculator.getAdjustedDate(PRICING_DATE, Period.ofYears(1), BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_YOY_MINUS1 = ScheduleCalculator.getAdjustedDate(PRICING_DATE, Period.ofYears(0), BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(PRICING_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS1 = ScheduleCalculator.getAdjustedDate(PRICING_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);

  private final static double DELTA = 10e-8;

  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG, false);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR = YEAR_ON_YEAR_DEFINITION.toDerivative(PRICING_DATE);

  private static final InflationProviderDiscount INFLATION_PROVIDER = MARKET.getInflationProvider();
  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(INTERPOLATOR_LINEAR, INTERPOLATOR_LINEAR);
  private static final InterpolatedDoublesCurve BLACK_SURF = InterpolatedDoublesCurve.from(new double[] {1.0, 5.0, 10.0 }, new double[] {.2, .2, .2 }, INTERPOLATOR_LINEAR);
  private static final BlackFlatCapFloorParameters BLACK_PARAM = new BlackFlatCapFloorParameters(BLACK_SURF, EURIBOR6M);
  private static final double[] INFLATION_TIME = new double[] {YEAR_ON_YEAR.getNaturalPaymentStartTime(), YEAR_ON_YEAR.getNaturalPaymentEndTime() };
  private static final double[] ATM_VOLATILITY = new double[] {0.02, 0.02 };
  private static final InterpolatedDoublesSurface CORRELATION_SURFACE = InterpolatedDoublesSurface.from(
      new double[] {1.0, 5.0, 10.0, 30.0, 1.0, 5.0, 10.0, 30.0, 1.0, 5.0, 10.0, 30.0, 1.0, 5.0, 10.0, 30.0 },
      new double[] {1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 30.0, 30.0, 30.0, 30.0 },
      new double[] {1.0, 0.99, 0.98, 0.97, 0.99, 1.0, 0.99, 0.98, .98, 0.99, 1.0, 0.99, 0.97, 0.98, 0.98, 1.0 },
      INTERPOLATOR_LINEAR_2D);
  private static final Surface<Double, Double, Double> PRICE_INDEX_CORRELATION = CORRELATION_SURFACE;
  private static final Surface<Double, Double, Double> LIBOR_CORRELATION = CORRELATION_SURFACE;
  private static final Curve<Double, Double> PRICE_INDEX_RATE_CORRELATION = InterpolatedDoublesCurve.from(new double[] {1.0, 10.0, 30.0 }, new double[] {-0.15, -0.15, -0.15 }, INTERPOLATOR_LINEAR);

  private static final InflationConvexityAdjustmentParameters INFLATION_PARAM = new InflationConvexityAdjustmentParameters(INFLATION_TIME, ATM_VOLATILITY, LIBOR_CORRELATION, PRICE_INDEX_CORRELATION,
      PRICE_INDEX_RATE_CORRELATION, PRICE_INDEX_EUR);
  private static final InflationConvexityAdjustmentProviderDiscount PARAMETER_INTERFACE = new InflationConvexityAdjustmentProviderDiscount(INFLATION_PROVIDER, INFLATION_PARAM, BLACK_PARAM);

  private static final InflationMarketModelConvexityAdjustmentForCoupon CONVEXITY_ADJUSTMENT_FUNCTION = new InflationMarketModelConvexityAdjustmentForCoupon();
  private static final CouponInflationZeroCouponMonthlyConvexityAdjustmentMethod METHOD_ZC_MONTHLY_CONVEXITY_ADJUSTMENT = new CouponInflationZeroCouponMonthlyConvexityAdjustmentMethod();
  private static final CouponInflationZeroCouponMonthlyDiscountingMethod METHOD_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyDiscountingMethod();
  private static final CouponInflationYearOnYearMonthlyConvexityAdjustmentMethod METHOD_YOY_MONTHLY_CONVEXITY_ADJUSTMENT = new CouponInflationYearOnYearMonthlyConvexityAdjustmentMethod();
  private static final CouponInflationYearOnYearMonthlyDiscountingMethod METHOD_YOY_MONTHLY = new CouponInflationYearOnYearMonthlyDiscountingMethod();
  private static final CouponInflationZeroCouponInterpolationConvexityAdjustmentMethod METHOD_ZC_INTERPOLATION_CONVEXITY_ADJUSTMENT = new CouponInflationZeroCouponInterpolationConvexityAdjustmentMethod();
  private static final CouponInflationZeroCouponInterpolationDiscountingMethod METHOD_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationDiscountingMethod();
  private static final CouponInflationYearOnYearInterpolationConvexityAdjustmentMethod METHOD_YOY_INTERPOLATION_CONVEXITY_ADJUSTMENT = new CouponInflationYearOnYearInterpolationConvexityAdjustmentMethod();
  private static final CouponInflationYearOnYearInterpolationDiscountingMethod METHOD_YOY_INTERPOLATION = new CouponInflationYearOnYearInterpolationDiscountingMethod();

  final DoubleTimeSeries<ZonedDateTime> cpiTimeSerie = MulticurveProviderDiscountDataSets.usCpiFrom2009();

  /**
   * Tests the  value.
   */
  @Test
  public void valueForZeroCouponMonthly() {
    final CouponInflationZeroCouponMonthlyDefinition[] zeroCouponDefinitions = new CouponInflationZeroCouponMonthlyDefinition[20];
    final CouponInflationZeroCouponMonthly[] zeroCoupons = new CouponInflationZeroCouponMonthly[20];
    final double[] convexitysAdjustments = new double[20];
    final double[] pricesWithConvexity = new double[20];
    final double[] pricesWithoutConvexity = new double[20];
    for (int i = 0; i < 20; i++) {
      zeroCouponDefinitions[i] = CouponInflationZeroCouponMonthlyDefinition.from(PRICING_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, 3 + i, true);
      zeroCoupons[i] = (CouponInflationZeroCouponMonthly) zeroCouponDefinitions[i].toDerivative(PRICING_DATE, cpiTimeSerie);
      convexitysAdjustments[i] = CONVEXITY_ADJUSTMENT_FUNCTION.getZeroCouponConvexityAdjustment(zeroCoupons[i], PARAMETER_INTERFACE);
      pricesWithConvexity[i] = METHOD_ZC_MONTHLY_CONVEXITY_ADJUSTMENT.presentValue(zeroCoupons[i], PARAMETER_INTERFACE).getAmount(zeroCoupons[i].getCurrency());
      pricesWithoutConvexity[i] = METHOD_ZC_MONTHLY.presentValue(zeroCoupons[i], INFLATION_PROVIDER).getAmount(zeroCoupons[i].getCurrency());
      assertEquals("convexity adjustment for inflation zero coupon monthly", pricesWithConvexity[i], pricesWithoutConvexity[i] * convexitysAdjustments[i], DELTA);
    }
  }

  /**
   * Tests the  value.
   */
  @Test
  public void valueForZeroCouponMonthlyYearly() {
    final CouponInflationZeroCouponMonthlyDefinition[] zeroCouponDefinitions = new CouponInflationZeroCouponMonthlyDefinition[20];
    final CouponInflationZeroCouponMonthly[] zeroCoupons = new CouponInflationZeroCouponMonthly[20];
    final double[] convexitysAdjustments = new double[20];
    final double[] pricesWithConvexity = new double[20];
    final double[] pricesWithoutConvexity = new double[20];
    for (int i = 0; i < 20; i++) {
      zeroCouponDefinitions[i] = CouponInflationZeroCouponMonthlyDefinition.from(PRICING_DATE, PAYMENT_DATE.plusYears(i), NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, 8, true);
      zeroCoupons[i] = (CouponInflationZeroCouponMonthly) zeroCouponDefinitions[i].toDerivative(PRICING_DATE, cpiTimeSerie);
      convexitysAdjustments[i] = CONVEXITY_ADJUSTMENT_FUNCTION.getZeroCouponConvexityAdjustment(zeroCoupons[i], PARAMETER_INTERFACE);
      pricesWithConvexity[i] = METHOD_ZC_MONTHLY_CONVEXITY_ADJUSTMENT.presentValue(zeroCoupons[i], PARAMETER_INTERFACE).getAmount(zeroCoupons[i].getCurrency());
      pricesWithoutConvexity[i] = METHOD_ZC_MONTHLY.presentValue(zeroCoupons[i], INFLATION_PROVIDER).getAmount(zeroCoupons[i].getCurrency());
      assertEquals("convexity adjustment for inflation zero coupon monthly", pricesWithConvexity[i], pricesWithoutConvexity[i] * convexitysAdjustments[i], DELTA);
    }
  }

  /**
   * Tests the  value.
   */
  @Test(enabled = false, description = "FAILING")
  public void valueForZeroCouponInterpolation() {
    final CouponInflationZeroCouponInterpolationDefinition[] zeroCouponDefinitions = new CouponInflationZeroCouponInterpolationDefinition[20];
    final CouponInflationZeroCouponInterpolation[] zeroCoupons = new CouponInflationZeroCouponInterpolation[20];
    final double[] convexitysAdjustments = new double[20];
    final double[] pricesWithConvexity = new double[20];
    final double[] pricesWithoutConvexity = new double[20];
    for (int i = 0; i < 20; i++) {
      zeroCouponDefinitions[i] = CouponInflationZeroCouponInterpolationDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, 3 + i, true);
      zeroCoupons[i] = zeroCouponDefinitions[i].toDerivative(PRICING_DATE);
      convexitysAdjustments[i] = CONVEXITY_ADJUSTMENT_FUNCTION.getZeroCouponConvexityAdjustment(zeroCoupons[i], PARAMETER_INTERFACE);
      pricesWithConvexity[i] = METHOD_ZC_INTERPOLATION_CONVEXITY_ADJUSTMENT.presentValue(zeroCoupons[i], PARAMETER_INTERFACE).getAmount(zeroCoupons[i].getCurrency());
      pricesWithoutConvexity[i] = METHOD_ZC_INTERPOLATION.presentValue(zeroCoupons[i], INFLATION_PROVIDER).getAmount(zeroCoupons[i].getCurrency());
      assertEquals("convexity adjustment for inflation zero coupon interpolation", pricesWithConvexity[i], pricesWithoutConvexity[i] * convexitysAdjustments[i], DELTA);
    }
  }

  /**
   * Tests the  value.
   */
  @Test
  public void valueForYearOnYearInterpolation() {
    final CouponInflationYearOnYearInterpolationDefinition[] zeroCouponDefinitions = new CouponInflationYearOnYearInterpolationDefinition[20];
    final CouponInflationYearOnYearInterpolation[] zeroCoupons = new CouponInflationYearOnYearInterpolation[20];
    final double[] convexitysAdjustments = new double[20];
    final double[] pricesWithConvexity = new double[20];
    final double[] pricesWithoutConvexity = new double[20];
    for (int i = 0; i < 20; i++) {
      zeroCouponDefinitions[i] = CouponInflationYearOnYearInterpolationDefinition.from(PAYMENT_DATE_YOY_MINUS1.plusYears(i), PAYMENT_DATE_YOY.plusYears(i), NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG,
          MONTH_LAG,
          true);
      zeroCoupons[i] = zeroCouponDefinitions[i].toDerivative(PRICING_DATE);
      convexitysAdjustments[i] = CONVEXITY_ADJUSTMENT_FUNCTION.getYearOnYearConvexityAdjustment(zeroCoupons[i], PARAMETER_INTERFACE);
      pricesWithConvexity[i] = METHOD_YOY_INTERPOLATION_CONVEXITY_ADJUSTMENT.presentValue(zeroCoupons[i], PARAMETER_INTERFACE).getAmount(zeroCoupons[i].getCurrency());
      pricesWithoutConvexity[i] = METHOD_YOY_INTERPOLATION.presentValue(zeroCoupons[i], INFLATION_PROVIDER).getAmount(zeroCoupons[i].getCurrency());
      assertEquals("convexity adjustment for inflation year on year interpolation", pricesWithConvexity[i], pricesWithoutConvexity[i] * convexitysAdjustments[i], DELTA);
    }
  }

  /**
   * Tests the  value.
   */
  @Test
  public void valueForYearOnYearMonthly() {
    final CouponInflationYearOnYearMonthlyDefinition[] zeroCouponDefinitions = new CouponInflationYearOnYearMonthlyDefinition[20];
    final CouponInflationYearOnYearMonthly[] zeroCoupons = new CouponInflationYearOnYearMonthly[20];
    final double[] convexitysAdjustments = new double[20];
    final double[] pricesWithConvexity = new double[20];
    final double[] pricesWithoutConvexity = new double[20];
    for (int i = 0; i < 20; i++) {
      zeroCouponDefinitions[i] = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_YOY_MINUS1.plusYears(i), PAYMENT_DATE_YOY.plusYears(i), NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG,
          true);
      zeroCoupons[i] = zeroCouponDefinitions[i].toDerivative(PRICING_DATE);
      convexitysAdjustments[i] = CONVEXITY_ADJUSTMENT_FUNCTION.getYearOnYearConvexityAdjustment(zeroCoupons[i], PARAMETER_INTERFACE);
      pricesWithConvexity[i] = METHOD_YOY_MONTHLY_CONVEXITY_ADJUSTMENT.presentValue(zeroCoupons[i], PARAMETER_INTERFACE).getAmount(zeroCoupons[i].getCurrency());
      pricesWithoutConvexity[i] = METHOD_YOY_MONTHLY.presentValue(zeroCoupons[i], INFLATION_PROVIDER).getAmount(zeroCoupons[i].getCurrency());
      assertEquals("convexity adjustment for inflation year on year monthly", pricesWithConvexity[i], pricesWithoutConvexity[i] * convexitysAdjustments[i], DELTA);
    }
  }
}
