/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.ExtremeSpreadOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.ExtremeSpreadOptionModel;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * 
 */
public class ExtremeSpreadOptionModelTest {
  private static final double SPOT = 100;
  private static final double B = 0.1;
  private static final YieldAndDiscountCurve CURVE = new YieldCurve(ConstantDoublesCurve.from(0.1));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final ExtremeSpreadOptionModel MODEL = new ExtremeSpreadOptionModel();
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    DoubleTimeSeries<?> ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    StandardOptionWithSpotTimeSeriesDataBundle data = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(0.15)), SPOT, DATE, ts);
    ExtremeSpreadOptionDefinition option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25)), false);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 10.6618, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 110});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 8.4878, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 120});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 4.5235, EPS);
    option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.75)), true);
    data = data.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0.3)));
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 13.3404, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 90});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 14.8173, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 80});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 19.0537, EPS);
    option = new ExtremeSpreadOptionDefinition(EXPIRY, true, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.)), true);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 100});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 0, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 90});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 1.4769, EPS);
    ts = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 11, 1)}, new double[] {SPOT, 80});
    data = data.withSpotTimeSeries(ts);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 5.7133, EPS);
  }
}
