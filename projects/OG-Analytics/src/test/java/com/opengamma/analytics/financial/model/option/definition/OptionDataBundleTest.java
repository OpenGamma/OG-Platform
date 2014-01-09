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
public class OptionDataBundleTest {
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.35));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final OptionDataBundle DATA = new OptionDataBundle(CURVE, SURFACE, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    new OptionDataBundle(new OptionDataBundle(CURVE, SURFACE, null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new OptionDataBundle(null);
  }

  @Test
  public void test() {
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    OptionDataBundle other = new OptionDataBundle(CURVE, SURFACE, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new OptionDataBundle(DATA);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new OptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), SURFACE, DATE);
    assertFalse(other.equals(DATA));
    other = new OptionDataBundle(CURVE, new VolatilitySurface(ConstantDoublesSurface.from(0.6)), DATE);
    assertFalse(other.equals(DATA));
    other = new OptionDataBundle(CURVE, SURFACE, DATE.plusDays(1));
    assertFalse(other.equals(DATA));
  }

  @Test
  public void testBuilders() {
    final ZonedDateTime newDate = DATE.plusDays(1);
    assertEquals(DATA.withDate(newDate), new OptionDataBundle(CURVE, SURFACE, newDate));
    final YieldCurve newCurve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    assertEquals(DATA.withInterestRateCurve(newCurve), new OptionDataBundle(newCurve, SURFACE, DATE));
    final VolatilitySurface newSurface = new VolatilitySurface(ConstantDoublesSurface.from(0.9));
    assertEquals(DATA.withVolatilitySurface(newSurface), new OptionDataBundle(CURVE, newSurface, DATE));
  }

}
