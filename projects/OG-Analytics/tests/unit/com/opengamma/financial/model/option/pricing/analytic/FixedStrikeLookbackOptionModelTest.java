/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.FixedStrikeLookbackOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class FixedStrikeLookbackOptionModelTest {
  private static final double S = 100;
  private static final DoubleTimeSeries<?> STATIC = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2, 3, 4, 5, 6, 7}, new double[] {S, S, S, S, S, S, S});
  private static final double B = 0.1;
  private static final YieldAndDiscountCurve CURVE = new YieldCurve(ConstantDoublesCurve.from(0.1));
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final FixedStrikeLookbackOptionModel MODEL = new FixedStrikeLookbackOptionModel();
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), S, DATE,
      STATIC);
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new FixedStrikeLookbackOptionDefinition(100, EXPIRY, true)).evaluate((StandardOptionWithSpotTimeSeriesDataBundle) null);
  }

  @Test
  public void test() {
    FixedStrikeLookbackOptionDefinition option = new FixedStrikeLookbackOptionDefinition(95, EXPIRY, false);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 4.4448, EPS);
    option = new FixedStrikeLookbackOptionDefinition(100, EXPIRY, false);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 8.3177, EPS);
    option = new FixedStrikeLookbackOptionDefinition(105, EXPIRY, false);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 13.0739, EPS);
    option = new FixedStrikeLookbackOptionDefinition(95, EXPIRY, true);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 18.9263, EPS);
    option = new FixedStrikeLookbackOptionDefinition(100, EXPIRY, true);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 14.1702, EPS);
    option = new FixedStrikeLookbackOptionDefinition(105, EXPIRY, true);
    assertEquals(MODEL.getPricingFunction(option).evaluate(DATA), 9.8905, EPS);
  }
}
