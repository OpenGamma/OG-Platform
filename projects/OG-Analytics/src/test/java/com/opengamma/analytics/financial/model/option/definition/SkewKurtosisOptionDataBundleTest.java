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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SkewKurtosisOptionDataBundleTest {
  private static final double R = 0.03;
  private static final double SIGMA = 0.25;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double B = 0.03;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final double SKEW = 1.2;
  private static final double KURTOSIS = 4.5;
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(R + 1));
  private static final double OTHER_B = B + 1;
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA + 1));
  private static final double OTHER_SPOT = SPOT + 1;
  private static final ZonedDateTime OTHER_DATE = DateUtils.getDateOffsetWithYearFraction(DATE, 1);
  private static final double OTHER_SKEW = 0.1;
  private static final double OTHER_KURTOSIS = 3;
  private static final SkewKurtosisOptionDataBundle DATA = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBundle() {
    new SkewKurtosisOptionDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
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
    final SkewKurtosisOptionDataBundle data1 = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS);
    final SkewKurtosisOptionDataBundle data2 = new SkewKurtosisOptionDataBundle(new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE), SKEW, KURTOSIS);
    final SkewKurtosisOptionDataBundle data3 = new SkewKurtosisOptionDataBundle(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS));
    assertEquals(DATA, data1);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertEquals(DATA, data2);
    assertEquals(DATA.hashCode(), data2.hashCode());
    assertEquals(DATA, data3);
    assertEquals(DATA.hashCode(), data3.hashCode());
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_SKEW, KURTOSIS)));
    assertFalse(DATA.equals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, OTHER_KURTOSIS)));
  }

  @Test
  public void testBuilders() {
    assertEquals(new SkewKurtosisOptionDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS), DATA.withInterestRateCurve(OTHER_CURVE));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, SKEW, KURTOSIS), DATA.withCostOfCarry(OTHER_B));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, SKEW, KURTOSIS), DATA.withVolatilitySurface(OTHER_SURFACE));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, SKEW, KURTOSIS), DATA.withSpot(OTHER_SPOT));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, SKEW, KURTOSIS), DATA.withDate(OTHER_DATE));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_SKEW, KURTOSIS), DATA.withSkew(OTHER_SKEW));
    assertEquals(new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, OTHER_KURTOSIS), DATA.withKurtosis(OTHER_KURTOSIS));
  }
}
