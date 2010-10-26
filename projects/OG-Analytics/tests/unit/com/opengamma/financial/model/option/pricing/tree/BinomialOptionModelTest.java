/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
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
      return new RecombiningBinomialTree<Double>(tree);
    }

    @Override
    public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final int n, final int j) {
      return 1.1;
    }
  };
  private static final BinomialOptionModel<StandardOptionDataBundle> BINOMIAL_THREE_STEPS = new BinomialOptionModel<StandardOptionDataBundle>(DUMMY, 3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    new BinomialOptionModel<StandardOptionDataBundle>(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeN() {
    new BinomialOptionModel<StandardOptionDataBundle>(DUMMY, -3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroN() {
    new BinomialOptionModel<StandardOptionDataBundle>(DUMMY, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDepth() {
    new BinomialOptionModel<StandardOptionDataBundle>(DUMMY, 3, -3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentDepth() {
    new BinomialOptionModel<StandardOptionDataBundle>(DUMMY, 3, 10);
  }

  @Test
  public void testEuropeanCallTree() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.)), 100., date);
    final OptionDefinition option = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 1)), true);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<DoublesPair>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final DoublesPair[][] result = f.evaluate(data).getTree();
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
    testTrees(expected, result, 4);
  }

  @Test
  public void testAmericanPutTree() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.)), 100., date);
    final OptionDefinition option = new AmericanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 1)), false);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<DoublesPair>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final DoublesPair[][] result = f.evaluate(data).getTree();
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
    testTrees(expected, result, 4);
  }

  private void testTrees(final DoublesPair[][] expected, final DoublesPair[][] result, final int n) {
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
