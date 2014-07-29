/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BinomialOptionModelTest {
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> DUMMY = new BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle>() {
    @Override
    public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
      return 1. / 1.1;
    }

    @Override
    public RecombiningBinomialTree<Double> getUpProbabilityTree(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
      final double t = option.getTimeToExpiry(data.getDate());
      final double dt = t / n;
      final double r = data.getInterestRate(t);
      final double u = getUpFactor(option, data, n, j);
      final double d = getDownFactor(option, data, n, j);
      final double p = (Math.exp(r * dt) - d) / (u - d);
      final Double[][] tree = new Double[n + 1][j];
      for (int i = 0; i <= n; i++) {
        for (int ii = 0; ii < j; ii++) {
          tree[i][ii] = p;
        }
      }
      return new RecombiningBinomialTree<>(tree);
    }

    @Override
    public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
      return 1.1;
    }
  };
  private static final BinomialOptionModel<StandardOptionDataBundle> BINOMIAL_THREE_STEPS = new BinomialOptionModel<>(DUMMY, 3);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    new BinomialOptionModel<>(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeN() {
    new BinomialOptionModel<>(DUMMY, -3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroN() {
    new BinomialOptionModel<>(DUMMY, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDepth() {
    new BinomialOptionModel<>(DUMMY, 3, -3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInconsistentDepth() {
    new BinomialOptionModel<>(DUMMY, 3, 10);
  }

  @Test
  public void testEuropeanCallTree() {
    final ZonedDateTime date = DateUtils.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.)), 100., date);
    final OptionDefinition option = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getDateOffsetWithYearFraction(date, 1)), true);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<DoublesPair>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final DoublesPair[][] result = f.evaluate(data).getNodes();
    final DoublesPair[][] expected = new DoublesPair[4][4];
    expected[0][0] = DoublesPair.of(100., 10.1457);
    expected[1][0] = DoublesPair.of(90.91, 3.2545);
    expected[1][1] = DoublesPair.of(110., 15.4471);
    expected[2][0] = DoublesPair.of(82.64, 0.);
    expected[2][1] = DoublesPair.of(100., 5.7048);
    expected[2][2] = DoublesPair.of(121., 22.9801);
    expected[3][0] = DoublesPair.of(75.13, 0.);
    expected[3][1] = DoublesPair.of(90.91, 0.);
    expected[3][2] = DoublesPair.of(110., 10.);
    expected[3][3] = DoublesPair.of(133.1, 33.1);
    assertTrees(expected, result, 4);
  }

  @Test
  public void testAmericanPutTree() {
    final ZonedDateTime date = DateUtils.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.)), 100., date);
    final OptionDefinition option = new AmericanVanillaOptionDefinition(100, new Expiry(DateUtils.getDateOffsetWithYearFraction(date, 1)), false);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<DoublesPair>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final DoublesPair[][] result = f.evaluate(data).getNodes();
    final DoublesPair[][] expected = new DoublesPair[4][4];
    expected[0][0] = DoublesPair.of(100., 4.6546);
    expected[1][0] = DoublesPair.of(90.91, 9.2356);
    expected[1][1] = DoublesPair.of(110., 1.5261);
    expected[2][0] = DoublesPair.of(82.64, 17.3554);
    expected[2][1] = DoublesPair.of(100., 3.7247);
    expected[2][2] = DoublesPair.of(121., 0.);
    expected[3][0] = DoublesPair.of(75.13, 24.8685);
    expected[3][1] = DoublesPair.of(90.91, 9.0909);
    expected[3][2] = DoublesPair.of(110., 0.);
    expected[3][3] = DoublesPair.of(133.1, 0.);
    assertTrees(expected, result, 4);
  }

  private void assertTrees(final DoublesPair[][] expected, final DoublesPair[][] result, final int n) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (expected[i][j] == null) {
          assertTrue(result[i][j] == null);
        } else {
          assertEquals(expected[i][j].first, result[i][j].first, 1e-2);
          assertEquals(expected[i][j].second, result[i][j].second, 1e-4);
        }
      }
    }
  }

}
