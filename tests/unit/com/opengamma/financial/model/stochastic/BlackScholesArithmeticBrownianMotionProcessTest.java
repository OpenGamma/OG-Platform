/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.stochastic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.math.random.RandomNumberGenerator;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BlackScholesArithmeticBrownianMotionProcessTest {
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);
  private static final StochasticProcess<OptionDefinition, StandardOptionDataBundle> PROCESS = new BlackScholesArithmeticBrownianMotionProcess<OptionDefinition, StandardOptionDataBundle>(
      GENERATOR);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
  private static final double R = 0.4;
  private static final double B = 0.2;
  private static final double S = 100;
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(R), B, new ConstantVolatilitySurface(0.), S, DATE);
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new BlackScholesArithmeticBrownianMotionProcess<OptionDefinition, StandardOptionDataBundle>(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    PROCESS.getPath(null, DATA, 100, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    PROCESS.getPath(CALL, null, 100, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientPaths() {
    PROCESS.getPath(CALL, DATA, 0, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientSteps() {
    PROCESS.getPath(CALL, DATA, 100, 0);
  }

  @Test
  public void testWithZeroVol() {
    final List<Double[]> paths = PROCESS.getPath(CALL, DATA, 100, 100);
    final Double[] zeroth = paths.get(0);
    final double s1 = S * Math.exp(R - B);
    assertEquals(zeroth[99], s1, EPS);
    Double[] array;
    for (int i = 1; i < paths.size(); i++) {
      array = paths.get(i);
      assertArrayEquals(array, zeroth);
      assertEquals(array[99], s1, EPS);
    }
  }
}
