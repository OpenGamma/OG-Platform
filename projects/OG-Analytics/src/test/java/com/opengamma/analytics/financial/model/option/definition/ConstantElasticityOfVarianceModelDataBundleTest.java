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
public class ConstantElasticityOfVarianceModelDataBundleTest {
  private static final double R = 0.05;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final YieldAndDiscountCurve OTHER_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.06));
  private static final double B = 0.01;
  private static final double OTHER_B = 0.04;
  private static final double SIGMA = 0.2;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final VolatilitySurface OTHER_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.4));
  private static final double SPOT = 100;
  private static final double OTHER_SPOT = 105;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime OTHER_DATE = DateUtils.getUTCDate(2011, 1, 1);
  private static final double ELASTICITY = 1;
  private static final double OTHER_ELASTICITY = 2;
  private static final ConstantElasticityOfVarianceModelDataBundle DATA = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, ELASTICITY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new ConstantElasticityOfVarianceModelDataBundle(null, ELASTICITY);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), B, 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getInterestRateCurve(), CURVE);
    assertEquals(DATA.getElasticity(), ELASTICITY, 0);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), SURFACE);
  }

  @Test
  public void testGetData() {
    assertEquals(DATA.getInterestRate(Math.random()), R, 0);
    assertEquals(DATA.getVolatility(Math.random(), Math.random()), SIGMA, 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    ConstantElasticityOfVarianceModelDataBundle other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, ELASTICITY);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new ConstantElasticityOfVarianceModelDataBundle(DATA, ELASTICITY);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new ConstantElasticityOfVarianceModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, ELASTICITY);
    assertFalse(DATA.equals(other));
    other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, ELASTICITY);
    assertFalse(DATA.equals(other));
    other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, ELASTICITY);
    assertFalse(DATA.equals(other));
    other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, ELASTICITY);
    assertFalse(DATA.equals(other));
    other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, ELASTICITY);
    assertFalse(DATA.equals(other));
    other = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_ELASTICITY);
    assertFalse(DATA.equals(other));
  }

  @Test
  public void testBuilders() {
    ConstantElasticityOfVarianceModelDataBundle other = DATA.withCostOfCarry(OTHER_B);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(CURVE, OTHER_B, SURFACE, SPOT, DATE, ELASTICITY));
    other = DATA.withDate(OTHER_DATE);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, OTHER_DATE, ELASTICITY));
    other = DATA.withElasticity(OTHER_ELASTICITY);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, OTHER_ELASTICITY));
    other = DATA.withInterestRateCurve(OTHER_CURVE);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(OTHER_CURVE, B, SURFACE, SPOT, DATE, ELASTICITY));
    other = DATA.withSpot(OTHER_SPOT);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, SURFACE, OTHER_SPOT, DATE, ELASTICITY));
    other = DATA.withVolatilitySurface(OTHER_SURFACE);
    assertEquals(other, new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, OTHER_SURFACE, SPOT, DATE, ELASTICITY));
  }
}
