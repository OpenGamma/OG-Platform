/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.montecarlo;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.financial.model.stochastic.BlackScholesGeometricBrownianMotionProcess;
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
public class EuropeanMonteCarloOptionModelTest {
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0.02, new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
      100., DATE);
  private static final OptionDefinition CALL1 = new EuropeanVanillaOptionDefinition(110, EXPIRY, true);
  private static final OptionDefinition PUT1 = new EuropeanVanillaOptionDefinition(110, EXPIRY, false);
  private static final OptionDefinition CALL2 = new EuropeanVanillaOptionDefinition(90, EXPIRY, true);
  private static final OptionDefinition PUT2 = new EuropeanVanillaOptionDefinition(90, EXPIRY, false);
  private static final int N = 10000;
  private static final double EPS = 0.05;
  private static final EuropeanMonteCarloOptionModel MODEL = new EuropeanMonteCarloOptionModel(N, 1, new BlackScholesGeometricBrownianMotionProcess<>(),
      GENERATOR);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
