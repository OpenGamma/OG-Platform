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
public class BlackOptionDataBundleTest {
  private static final double R = 0.05;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.35));
  private static final double F = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final BlackOptionDataBundle DATA = new BlackOptionDataBundle(F, CURVE, SURFACE, DATE);

  @Test
  public void test() {
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getForward(), F, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    final double t = Math.random();
    assertEquals(DATA.getDiscountFactor(t), Math.exp(-R * t), 0);
    BlackOptionDataBundle other = new BlackOptionDataBundle(F, CURVE, SURFACE, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new BlackOptionDataBundle(DATA);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new BlackOptionDataBundle(F + 1, CURVE, SURFACE, DATE);
    assertFalse(other.equals(DATA));
    other = new BlackOptionDataBundle(F, YieldCurve.from(ConstantDoublesCurve.from(0.06)), SURFACE, DATE);
    assertFalse(other.equals(DATA));
    other = new BlackOptionDataBundle(F, CURVE, new VolatilitySurface(ConstantDoublesSurface.from(0.6)), DATE);
    assertFalse(other.equals(DATA));
    other = new BlackOptionDataBundle(F, CURVE, SURFACE, DATE.plusDays(1));
    assertFalse(other.equals(DATA));
  }

  @Test
  public void testBuilders() {
    final ZonedDateTime newDate = DATE.plusDays(1);
    assertEquals(DATA.withDate(newDate), new BlackOptionDataBundle(F, CURVE, SURFACE, newDate));
    final double newForward = F + 1;
    assertEquals(DATA.withForward(newForward), new BlackOptionDataBundle(newForward, CURVE, SURFACE, DATE));
    final YieldCurve newCurve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    assertEquals(DATA.withInterestRateCurve(newCurve), new BlackOptionDataBundle(F, newCurve, SURFACE, DATE));
    final VolatilitySurface newSurface = new VolatilitySurface(ConstantDoublesSurface.from(0.9));
    assertEquals(DATA.withVolatilitySurface(newSurface), new BlackOptionDataBundle(F, CURVE, newSurface, DATE));
  }

}
