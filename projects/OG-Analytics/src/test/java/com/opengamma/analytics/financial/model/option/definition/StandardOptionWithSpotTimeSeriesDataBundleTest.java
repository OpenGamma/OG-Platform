/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StandardOptionWithSpotTimeSeriesDataBundleTest {
  private static final double R = 0.05;
  private static final double SIGMA = 0.15;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double B = 0.01;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final LocalDateDoubleTimeSeries TS = ImmutableLocalDateDoubleTimeSeries.of(new int[] {19700101, 19700102}, new double[] {3, 4});
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(R + 1));
  private static final double OTHER_B = B + 1;
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA + 1));
  private static final double OTHER_SPOT = SPOT + 1;
  private static final ZonedDateTime OTHER_DATE = DateUtils.getDateOffsetWithYearFraction(DATE, 1);
  private static final LocalDateDoubleTimeSeries OTHER_TS = ImmutableLocalDateDoubleTimeSeries.of(new int[] {19700101, 19700102}, new double[] {5, 6});
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new StandardOptionWithSpotTimeSeriesDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
    assertEquals(DATA.getSpotTimeSeries(), TS);
  }

  @Test
  public void testGetInterestRate() {
    for (int i = 0; i < 10; i++) {
      assertEquals(DATA.getInterestRate(Math.random()), R, 1e-15);
    }
  }

  @Test
  public void testGetVolatility() {
    for (int i = 0; i < 10; i++) {
      assertEquals(DATA.getVolatility(Math.random(), Math.random()), SIGMA, 1e-15);
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    final StandardOptionDataBundle other1 = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);
    final StandardOptionDataBundle other2 = new StandardOptionWithSpotTimeSeriesDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), TS);
    final StandardOptionDataBundle other3 = new StandardOptionWithSpotTimeSeriesDataBundle(DATA);
    assertEquals(DATA, other1);
    assertEquals(DATA.hashCode(), other1.hashCode());
    assertEquals(DATA, other2);
    assertEquals(DATA.hashCode(), other2.hashCode());
    assertEquals(DATA, other3);
    assertEquals(DATA.hashCode(), other3.hashCode());
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, TS)));
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, TS)));
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, TS)));
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, TS)));
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, TS)));
    assertFalse(DATA.equals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_TS)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, TS), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, TS), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, TS), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, TS), DATA.withSpot(OTHER_SPOT));
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, TS), DATA.withDate(OTHER_DATE));
    assertEquals(new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_TS), DATA.withSpotTimeSeries(OTHER_TS));
  }
}
