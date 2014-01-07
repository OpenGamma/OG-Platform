/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackDermanToyYieldOnlyInterestRateModelTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadNodes() {
    new BlackDermanToyYieldOnlyInterestRateModel(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    new BlackDermanToyYieldOnlyInterestRateModel(3).getTrees(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new BlackDermanToyYieldOnlyInterestRateModel(5).getTrees(DateUtils.getUTCDate(2010, 8, 1)).evaluate((StandardDiscountBondModelDataBundle) null);
  }

  @Test
  public void test() {
    final int steps = 3;
    final ZonedDateTime date = DateUtils.getUTCDate(2009, 1, 1);
    final ZonedDateTime maturity = DateUtils.getDateOffsetWithYearFraction(date, 3);
    final StandardDiscountBondModelDataBundle data = new StandardDiscountBondModelDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), new VolatilityCurve(ConstantDoublesCurve.from(0.1)),
        date);
    final BlackDermanToyYieldOnlyInterestRateModel model = new BlackDermanToyYieldOnlyInterestRateModel(steps);
    final RecombiningBinomialTree<Triple<Double, Double, Double>> tree = model.getTrees(maturity).evaluate(data);
    final Triple<Double, Double, Double>[][] result = tree.getNodes();
    @SuppressWarnings("unchecked")
    final Triple<Double, Double, Double>[][] expected = new Triple[4][4];
    expected[0][0] = new Triple<>(0.05, 0.9524, 1.0);
    expected[1][0] = new Triple<>(0.045, 0.9569, 0.4762);
    expected[1][1] = new Triple<>(0.055, 0.9479, 0.4762);
    expected[2][0] = new Triple<>(0.0406, 0.9610, 0.2278);
    expected[2][1] = new Triple<>(0.0496, 0.9528, 0.4535);
    expected[2][2] = new Triple<>(0.0605, 0.9430, 0.2257);
    expected[3][0] = new Triple<>(0.0366, 0.9647, 0.1095);
    expected[3][1] = new Triple<>(0.0447, 0.9572, 0.3255);
    expected[3][2] = new Triple<>(0.0546, 0.9482, 0.3224);
    expected[3][3] = new Triple<>(0.0667, 0.9375, 0.1064);
    assertEquals(result.length, expected.length);
    assertEquals(result[0].length, expected[0].length);
    for (int i = 0; i < expected.length; i++) {
      for (int j = 0; j < expected[0].length; j++) {
        if (expected[i][j] == null) {
          final Triple<Double, Double, Double> triple = result[i][j];
          assertEquals(triple.getFirst(), 0, 1e-16);
          assertEquals(triple.getSecond(), 0, 1e-16);
          assertEquals(triple.getThird(), 0, 1e-16);
        } else {
          final Triple<Double, Double, Double> triple1 = result[i][j];
          final Triple<Double, Double, Double> triple2 = expected[i][j];
          assertEquals(triple1.getFirst(), triple2.getFirst(), 1e-4);
          assertEquals(triple1.getSecond(), triple2.getSecond(), 1e-4);
          assertEquals(triple1.getThird(), triple2.getThird(), 1e-4);
        }
      }
    }
  }
}
