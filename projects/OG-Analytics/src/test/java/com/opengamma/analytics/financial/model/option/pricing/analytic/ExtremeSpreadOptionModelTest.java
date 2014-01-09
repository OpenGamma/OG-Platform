/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.ExtremeSpreadOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExtremeSpreadOptionModelTest {
  private static final double SPOT = 100;
  private static final double B = 0.1;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final ExtremeSpreadOptionModel MODEL = new ExtremeSpreadOptionModel();
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    DoubleTimeSeries<?> ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    StandardOptionWithSpotTimeSeriesDataBundle data = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(0.15)), SPOT, DATE, ts);
    ExtremeSpreadOptionDefinition option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25)), false);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 10.6618, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 110});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 8.4878, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 120});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 4.5235, EPS);
    option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.75)), true);
    data = data.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0.3)));
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 13.3404, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 90});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 14.8173, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 80});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 19.0537, EPS);
    option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.)), true);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 0, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 90});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 1.4769, EPS);
    ts = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 80});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 5.7133, EPS);
  }
}
