/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationConvexityAdjustmentProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the inflation convexity adjutment.
 */
public class InflationMarketModelConvexityAdjustmentForCoupontest {

  // Calibration of the volagtility used in the convextity adjutmnent.
  // We use Zero coupon caps/floors to calibrate those volatilities.
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS1 = ScheduleCalculator.getAdjustedDate(START_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);

  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, false);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR = YEAR_ON_YEAR_DEFINITION.toDerivative(PRICING_DATE);

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final InterpolatedDoublesCurve BLACK_SURF = InterpolatedDoublesCurve.from(new double[] {1.0, 2.0, 3.0 }, new double[] {1.0, 1.0, 1.0 }, INTERPOLATOR_LINEAR);
  private static final BlackFlatCapFloorParameters BLACK_PARAM = new BlackFlatCapFloorParameters(BLACK_SURF, EURIBOR6M);
  private static final double[] INFLATION_TIME = new double[] {1.0, 1.0 };
  private static final double[] ATM_VOLATILITY = new double[] {1.0, 1.0 };
  private static final InterpolatedDoublesSurface SURFACE = BlackDataSets.createBlackSurfaceExpiryStrike();
  private static final Surface<Double, Double, Double> PRICE_INDEX_CORRELATION = SURFACE;
  private static final Surface<Double, Double, Double> LIBOR_CORRELATION = SURFACE;

  private static final Curve<Double, Double> PRICE_INDEX_RATE_CORRELATION = InterpolatedDoublesCurve.from(new double[] {1.0, 2.0, 3.0 }, new double[] {1.0, 1.0, 1.0 }, INTERPOLATOR_LINEAR);

  private static final InflationConvexityAdjustmentParameters INFLATION_PARAM = new InflationConvexityAdjustmentParameters(INFLATION_TIME, ATM_VOLATILITY, PRICE_INDEX_CORRELATION, LIBOR_CORRELATION,
      PRICE_INDEX_RATE_CORRELATION, PRICE_INDEX_EUR);
  private static final InflationConvexityAdjustmentProviderDiscount PARAMETER_INTERFACE = new InflationConvexityAdjustmentProviderDiscount(MARKET.getInflationProvider(), INFLATION_PARAM, BLACK_PARAM);

  private static final InflationMarketModelConvexityAdjustmentForCoupon CONVEXITY_ADJUSTMENT_FUNCTION = new InflationMarketModelConvexityAdjustmentForCoupon();

  @Test
  /**
   * Tests the  value.
   */
  public void value() {
    double convexityAdjustment = CONVEXITY_ADJUSTMENT_FUNCTION.getYearOnYearConvexityAdjustment(YEAR_ON_YEAR, PARAMETER_INTERFACE);
    convexityAdjustment = Math.exp(convexityAdjustment);

    // TODO : finish this test
  }
}
