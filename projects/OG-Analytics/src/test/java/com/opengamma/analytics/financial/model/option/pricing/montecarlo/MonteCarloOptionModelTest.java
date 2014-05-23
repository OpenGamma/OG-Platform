/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.montecarlo;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.stochastic.BlackScholesArithmeticBrownianMotionProcess;
import com.opengamma.analytics.financial.model.stochastic.StochasticProcess;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MonteCarloOptionModelTest {
  private static final int N = 100;
  private static final int STEPS = 1000;
  private static final StochasticProcess<OptionDefinition, StandardOptionDataBundle> PROCESS = new BlackScholesArithmeticBrownianMotionProcess<>();
  private static final RandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);
  private static final DummyModel MODEL = new DummyModel(N, STEPS, PROCESS, GENERATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeN() {
    new DummyModel(-N, STEPS, PROCESS, GENERATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSteps() {
    new DummyModel(N, -STEPS, PROCESS, GENERATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProcess() {
    new DummyModel(N, STEPS, null, GENERATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator() {
    new DummyModel(N, STEPS, PROCESS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getGreeks(null, new StandardOptionDataBundle(null, 0, null, 100, DateUtils.getUTCDate(2010, 1, 1)), Sets.newHashSet(Greek.FAIR_PRICE));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getGreeks(new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getUTCDate(2010, 1, 1)), true), null, Sets.newHashSet(Greek.FAIR_PRICE));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGreeks() {
    MODEL.getGreeks(new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getUTCDate(2010, 1, 1)), true), new StandardOptionDataBundle(null, 0, null, 100, DateUtils.getUTCDate(2010, 1, 1)),
        null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyGreeks() {
    MODEL.getGreeks(new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getUTCDate(2010, 1, 1)), true), new StandardOptionDataBundle(null, 0, null, 100, DateUtils.getUTCDate(2010, 1, 1)),
        Collections.<Greek> emptySet());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongGreeks() {
    MODEL.getGreeks(new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getUTCDate(2010, 1, 1)), true), new StandardOptionDataBundle(null, 0, null, 100, DateUtils.getUTCDate(2010, 1, 1)),
        Sets.newHashSet(Greek.DELTA, Greek.GAMMA));
  }

  @Test
  public void testPriceOnly() {
    final GreekResultCollection result = MODEL.getGreeks(new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getUTCDate(2010, 1, 1)), true), new StandardOptionDataBundle(null, 0, null, 100,
        DateUtils.getUTCDate(2010, 1, 1)), Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA, Greek.GAMMA));
    assertEquals(result.size(), 1);
    assertEquals(result.keySet().iterator().next(), Greek.FAIR_PRICE);
    assertEquals(result.values().iterator().next(), 2., 0);
  }

  private static class DummyModel extends MonteCarloOptionModel<OptionDefinition, StandardOptionDataBundle> {

    public DummyModel(final int n, final int steps, final StochasticProcess<OptionDefinition, StandardOptionDataBundle> process, final RandomNumberGenerator generator) {
      super(n, steps, process, generator);
    }

    @Override
    public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
      return new Function1D<StandardOptionDataBundle, Double>() {

        @Override
        public Double evaluate(final StandardOptionDataBundle x) {
          return 2.;
        }

      };
    }

  }
}
