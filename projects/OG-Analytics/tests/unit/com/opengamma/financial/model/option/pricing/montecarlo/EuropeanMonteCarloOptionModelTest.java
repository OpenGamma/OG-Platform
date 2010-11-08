/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import static org.junit.Assert.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.stochastic.BlackScholesGeometricBrownianMotionProcess;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.math.random.RandomNumberGenerator;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanMonteCarloOptionModelTest {
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.06)), 0.02, new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
      100., DATE);
  private static final OptionDefinition CALL1 = new EuropeanVanillaOptionDefinition(110, EXPIRY, true);
  private static final OptionDefinition PUT1 = new EuropeanVanillaOptionDefinition(110, EXPIRY, false);
  private static final OptionDefinition CALL2 = new EuropeanVanillaOptionDefinition(90, EXPIRY, true);
  private static final OptionDefinition PUT2 = new EuropeanVanillaOptionDefinition(90, EXPIRY, false);
  private static final int N = 10000;
  private static final double EPS = 0.05;
  private static final EuropeanMonteCarloOptionModel MODEL = new EuropeanMonteCarloOptionModel(N, 1, new BlackScholesGeometricBrownianMotionProcess<OptionDefinition, StandardOptionDataBundle>(),
      GENERATOR);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CALL1).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    double x1 = BSM.getPricingFunction(CALL1).evaluate(DATA);
    double x2 = MODEL.getPricingFunction(CALL1).evaluate(DATA);
    assertTrue(Math.abs(x1 - x2) / x1 < EPS);
    x1 = BSM.getPricingFunction(CALL2).evaluate(DATA);
    x2 = MODEL.getPricingFunction(CALL2).evaluate(DATA);
    assertTrue(Math.abs(x1 - x2) / x1 < EPS);
    x1 = BSM.getPricingFunction(PUT1).evaluate(DATA);
    x2 = MODEL.getPricingFunction(PUT1).evaluate(DATA);
    assertTrue(Math.abs(x1 - x2) / x1 < EPS);
    x1 = BSM.getPricingFunction(PUT2).evaluate(DATA);
    x2 = MODEL.getPricingFunction(PUT2).evaluate(DATA);
    assertTrue(Math.abs(x1 - x2) / x1 < EPS);
  }
}
