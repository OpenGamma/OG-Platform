/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.stochastic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackScholesArithmeticBrownianMotionProcessTest {
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);
  private static final StochasticProcess<OptionDefinition, StandardOptionDataBundle> PROCESS = new BlackScholesArithmeticBrownianMotionProcess<>();
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
  private static final double R = 0.4;
  private static final double B = 0.1;
  private static final double S = 100;
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(R)), B, new VolatilitySurface(ConstantDoublesSurface.from(0.)), S, DATE);
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    PROCESS.getPathGeneratingFunction(null, DATA, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    PROCESS.getPathGeneratingFunction(CALL, null, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientSteps() {
    PROCESS.getPathGeneratingFunction(CALL, DATA, 0);
  }

  @Test
  public void testWithZeroVol() {
    final int steps = 100;
    final int dimension = 100;
    final List<double[]> randomNumbers = GENERATOR.getVectors(dimension, steps);
    final List<double[]> paths = PROCESS.getPaths(CALL, DATA, randomNumbers);
    final double[] zeroth = paths.get(0);
    final double s1 = S * Math.exp(B);
    assertEquals(zeroth[99], s1, EPS);
    double[] array;
    for (int i = 0; i < paths.size(); i++) {
      array = paths.get(i);
      assertArrayEquals(array, zeroth, EPS);
      assertEquals(array[99], s1, EPS);
    }
  }
}
