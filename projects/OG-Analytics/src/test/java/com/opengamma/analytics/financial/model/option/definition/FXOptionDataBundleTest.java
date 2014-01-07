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
public class FXOptionDataBundleTest {
  private static final YieldAndDiscountCurve DOMESTIC = YieldCurve.from(ConstantDoublesCurve.from(0.03));
  private static final YieldAndDiscountCurve FOREIGN = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final VolatilitySurface SIGMA = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final double SPOT = 1.5;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final FXOptionDataBundle DATA = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE);

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), -0.02, 1e-15);
    assertEquals(DATA.getForeignInterestRate(Math.random()), 0.05, 0);
    assertEquals(DATA.getForeignInterestRateCurve(), FOREIGN);
  }

  @Test
  public void testEqualsAndHashCode() {
    FXOptionDataBundle other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new FXOptionDataBundle(FOREIGN, FOREIGN, SIGMA, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, DOMESTIC, SIGMA, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT + 1, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE.plusDays(2));
    assertFalse(other.equals(DATA));
  }
}
