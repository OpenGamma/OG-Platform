/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.CoxRossRubinsteinBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.LeisenReimerBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.RendlemanBartterBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class MultiplicativeBinomialOptionModelTest {
  private static final double EPS = 1e-2;
  private static final Double STRIKE = 95.;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.08), 0.08, new ConstantVolatilitySurface(0.3), 100.,
      DATE);
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> CRR = new CoxRossRubinsteinBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> LR = new LeisenReimerBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> RB = new RendlemanBartterBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> DUMMY = new BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle>() {
    @Override
    public double getDownFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
      return 1. / 1.1;
    }

    @Override
    public double getProbability(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
      final double t = option.getTimeToExpiry(data.getDate());
      final double dt = t / n;
      final double r = data.getInterestRate(t);
      final double u = getUpFactor(option, data, n);
      final double d = getDownFactor(option, data, n);
      return (Math.exp(r * dt) - d) / (u - d);
    }

    @Override
    public double getUpFactor(final OptionDefinition option, final StandardOptionDataBundle data, final double n) {
      return 1.1;
    }
  };
  private static final MultiplicativeBinomialOptionModel BINOMIAL_THREE_STEPS = new MultiplicativeBinomialOptionModel(3, DUMMY);

  @SuppressWarnings("unchecked")
  @Test
  public void testEuropeanCallTree() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.06), 0., new ConstantVolatilitySurface(0.), 100., date);
    final OptionDefinition option = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 1)), true);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<Pair<Double, Double>>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final Pair<Double, Double>[][] result = f.evaluate(data).getTree();
    final Pair<Double, Double>[][] expected = new Pair[4][4];
    expected[0][0] = new Pair<Double, Double>(100., 10.1457);
    expected[1][0] = new Pair<Double, Double>(90.91, 3.2545);
    expected[1][1] = new Pair<Double, Double>(110., 15.4471);
    expected[2][0] = new Pair<Double, Double>(82.64, 0.);
    expected[2][1] = new Pair<Double, Double>(100., 5.7048);
    expected[2][2] = new Pair<Double, Double>(121., 22.9801);
    expected[3][0] = new Pair<Double, Double>(75.13, 0.);
    expected[3][1] = new Pair<Double, Double>(90.91, 0.);
    expected[3][2] = new Pair<Double, Double>(110., 10.);
    expected[3][3] = new Pair<Double, Double>(133.1, 33.1);
    testTrees(expected, result, 4);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAmericanPutTree() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.06), 0., new ConstantVolatilitySurface(0.), 100., date);
    final OptionDefinition option = new AmericanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 1)), false);
    final Function1D<StandardOptionDataBundle, RecombiningBinomialTree<Pair<Double, Double>>> f = BINOMIAL_THREE_STEPS.getTreeGeneratingFunction(option);
    final Pair<Double, Double>[][] result = f.evaluate(data).getTree();
    final Pair<Double, Double>[][] expected = new Pair[4][4];
    expected[0][0] = new Pair<Double, Double>(100., 4.6546);
    expected[1][0] = new Pair<Double, Double>(90.91, 9.2356);
    expected[1][1] = new Pair<Double, Double>(110., 1.5261);
    expected[2][0] = new Pair<Double, Double>(82.64, 17.3554);
    expected[2][1] = new Pair<Double, Double>(100., 3.7247);
    expected[2][2] = new Pair<Double, Double>(121., 0.);
    expected[3][0] = new Pair<Double, Double>(75.13, 24.8685);
    expected[3][1] = new Pair<Double, Double>(90.91, 9.0909);
    expected[3][2] = new Pair<Double, Double>(110., 0.);
    expected[3][3] = new Pair<Double, Double>(133.1, 0.);
    testTrees(expected, result, 4);
  }

  @Test
  public void test() {
    final OptionDefinition call = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    TreeOptionModel<OptionDefinition, StandardOptionDataBundle> binomial = new MultiplicativeBinomialOptionModel(CRR);
    testAgainstBSM(call, binomial);
    testAgainstBSM(put, binomial);
    binomial = new MultiplicativeBinomialOptionModel(LR);
    testAgainstBSM(call, binomial);
    testAgainstBSM(put, binomial);
    binomial = new MultiplicativeBinomialOptionModel(1001, RB);
    testAgainstBSM(call, binomial);
    testAgainstBSM(put, binomial);
  }

  private void testTrees(final Pair<Double, Double>[][] expected, final Pair<Double, Double>[][] result, final int n) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (expected[i][j] == null) {
          assertTrue(result[i][j] == null);
        } else {
          assertEquals(expected[i][j].getFirst(), result[i][j].getFirst(), 1e-2);
          assertEquals(expected[i][j].getSecond(), result[i][j].getSecond(), 1e-4);
        }
      }
    }
  }

  private void testAgainstBSM(final OptionDefinition option, final TreeOptionModel<OptionDefinition, StandardOptionDataBundle> model) {
    final List<Greek> requiredGreeks = Arrays.asList(Greek.PRICE);
    final Double expected = (Double) BSM.getGreeks(option, DATA, requiredGreeks).get(Greek.PRICE).getResult();
    final Double result = (Double) model.getGreeks(option, DATA, requiredGreeks).get(Greek.PRICE).getResult();
    assertEquals(expected, result, EPS);
  }
}
