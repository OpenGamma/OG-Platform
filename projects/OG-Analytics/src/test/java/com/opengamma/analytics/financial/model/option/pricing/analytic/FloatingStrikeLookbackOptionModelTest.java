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
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.FloatingStrikeLookbackOptionDefinition;
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
public class FloatingStrikeLookbackOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final double B = 0.04;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double SPOT = 120;
  private static final DoubleTimeSeries<?> TS = ImmutableInstantDoubleTimeSeries.of(new long[] {1, 2, 3, 4, 5, 6, 7}, new double[] {100, 101, 106, 100, 109,
      101, 104});
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final FloatingStrikeLookbackOptionDefinition CALL = new FloatingStrikeLookbackOptionDefinition(EXPIRY, true);
  private static final FloatingStrikeLookbackOptionDefinition PUT = new FloatingStrikeLookbackOptionDefinition(EXPIRY, false);
  private static final AnalyticOptionModel<FloatingStrikeLookbackOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> MODEL = new FloatingStrikeLookbackOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CALL).evaluate((StandardOptionWithSpotTimeSeriesDataBundle) null);
  }

  @Test
  public void test() {
    double strike = 102;
    DoubleTimeSeries<?> shortTS = ImmutableInstantDoubleTimeSeries.of(new long[] {1}, new double[] {strike});
    StandardOptionWithSpotTimeSeriesDataBundle data = DATA.withSpotTimeSeries(shortTS).withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0)));
    OptionDefinition vanilla = new EuropeanVanillaOptionDefinition(strike, EXPIRY, true);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    data = data.withCostOfCarry(0);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    strike = 95;
    shortTS = ImmutableInstantDoubleTimeSeries.of(new long[] {1}, new double[] {strike});
    data = DATA.withSpotTimeSeries(shortTS).withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0)));
    vanilla = new EuropeanVanillaOptionDefinition(strike, EXPIRY, false);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    data = data.withCostOfCarry(0);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(DATA), 25.3533, 1e-4);
  }
}
