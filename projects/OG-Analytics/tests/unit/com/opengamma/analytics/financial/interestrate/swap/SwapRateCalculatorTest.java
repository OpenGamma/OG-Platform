/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SwapRateCalculatorTest {
  private static final double DF_1 = 0.95;
  private static final ParRateCalculator CALCULATOR = ParRateCalculator.getInstance();
  private static final YieldAndDiscountCurve FUNDING_CURVE = new DiscountCurve(ConstantDoublesCurve.from(DF_1));
  private static final YieldAndDiscountCurve FORWARD_CURVE;
  private static final String FUNDING_CURVE_NAME = "Bill";
  private static final String FORWARD_CURVE_NAME = "Ben";
  private static final FixedFloatSwap SWAP;
  private static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  static {
    final double[] t1 = new double[] {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5};
    final int n = t1.length;
    final double[] t2 = new double[] {0.5, 1, 1.5, 2, 2.5, 3};
    final double[] forward = new double[n];
    for (int i = 0; i < n; i++) {
      forward[i] = Math.pow(DF_1, t1[i]);
    }
    FORWARD_CURVE = new DiscountCurve(InterpolatedDoublesCurve.from(t1, forward, new LinearInterpolator1D()));
    SWAP = new FixedFloatSwap(CUR, t2, t2, INDEX, 0.0, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    CALCULATOR.visit(SWAP, bundle);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.visit(SWAP, bundle);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.visit((FixedFloatSwap) null, bundle);
  }

  @Test
  public void test() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.visit(SWAP, bundle);
  }
}
