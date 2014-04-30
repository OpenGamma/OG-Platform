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
import com.opengamma.analytics.financial.model.option.definition.FadeInOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FadeInOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final double B = 0;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
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
    TS = ImmutableInstantDoubleTimeSeries.of(t, s);
    DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(DEFINITION).evaluate((StandardOptionWithSpotTimeSeriesDataBundle) null);
  }

  @Test
  public void testAgainstBSM() {
    final double eps = 1e-6;
    FadeInOptionDefinition definition = new FadeInOptionDefinition(SPOT, EXPIRY, false, 20, 180);
    final StandardOptionWithSpotTimeSeriesDataBundle data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0)));
    assertEquals(BSM.getPricingFunction(definition).evaluate(data), MODEL.getPricingFunction(definition).evaluate(data), eps);
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 95, 105);
    assertEquals(BSM.getPricingFunction(definition).evaluate(data), MODEL.getPricingFunction(definition).evaluate(data), eps);
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, false, 95, 105);
    assertEquals(BSM.getPricingFunction(definition).evaluate(data), MODEL.getPricingFunction(definition).evaluate(data), eps);
  }

  @Test
  public void test() {
    StandardOptionWithSpotTimeSeriesDataBundle data = DATA;
    FadeInOptionDefinition definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 85, 115);
    assertEquals(MODEL.getPricingFunction(definition).evaluate(data), 2.58, 1e-2);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0.4)));
    definition = new FadeInOptionDefinition(SPOT, EXPIRY, true, 95, 105);
    assertEquals(MODEL.getPricingFunction(definition).evaluate(data), 2.036, 1e-3);
  }
}
