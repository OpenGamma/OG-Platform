/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.FadeInOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class FadeInOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(0.1);
  private static final double B = 0;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(0.1);
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final DoubleTimeSeries<?> TS;
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA;
  private static final AnalyticOptionModel<FadeInOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> MODEL = new FadeInOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final FadeInOptionDefinition DEFINITION = new FadeInOptionDefinition(SPOT, EXPIRY, true, 20, 180);

  static {
    final int n = 183;
    final long[] t = new long[n];
    final double[] s = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i;
      s[i] = Math.random() * SPOT;
    }
    TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, s);
    DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(DEFINITION).evaluate((StandardOptionWithSpotTimeSeriesDataBundle) null);
  }

  @Test
  public void testAgainstBSM() {
    final double eps = 1e-6;
    assertEquals(BSM.getPricingFunction(DEFINITION).evaluate(DATA), MODEL.getPricingFunction(DEFINITION).evaluate(DATA), eps);
    FadeInOptionDefinition definition = new FadeInOptionDefinition(SPOT, EXPIRY, false, 20, 180);
    assertEquals(BSM.getPricingFunction(definition).evaluate(DATA), MODEL.getPricingFunction(definition).evaluate(DATA), eps);
    final StandardOptionWithSpotTimeSeriesDataBundle data = DATA.withVolatilitySurface(new ConstantVolatilitySurface(0));
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 95, 105);
    assertEquals(BSM.getPricingFunction(definition).evaluate(data), MODEL.getPricingFunction(definition).evaluate(data), eps);
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, false, 95, 105);
    assertEquals(BSM.getPricingFunction(definition).evaluate(data), MODEL.getPricingFunction(definition).evaluate(data), eps);
  }

  @Test
  public void test() {
    StandardOptionWithSpotTimeSeriesDataBundle data = DATA;
    FadeInOptionDefinition definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 75, 125);
    assertEquals(MODEL.getPricingFunction(definition).evaluate(data), 2.6802, 1e-4);
    data = DATA.withVolatilitySurface(new ConstantVolatilitySurface(0.4));
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 50, 150);
    assertEquals(MODEL.getPricingFunction(definition).evaluate(data), 9.396, 1e-4);
  }
}
