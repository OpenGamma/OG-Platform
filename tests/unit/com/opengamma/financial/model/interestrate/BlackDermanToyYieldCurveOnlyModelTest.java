/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.BlackDermanToyDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.Triple;

/**
 * 
 * @author emcleod
 */
public class BlackDermanToyYieldCurveOnlyModelTest {

  @Test
  public void test() {
    final int steps = 9;
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final ZonedDateTime maturity = DateUtil.getDateOffsetWithYearFraction(date, 4);
    final BlackDermanToyDataBundle data = new BlackDermanToyDataBundle(new ConstantInterestRateDiscountCurve(0.05),
        new ConstantVolatilityCurve(0.1), date);
    final BlackDermanToyYieldOnlyInterestRateModel model = new BlackDermanToyYieldOnlyInterestRateModel(steps);
    final RecombiningBinomialTree<Triple<Double, Double, Double>> tree = model.getTrees(maturity).evaluate(data);
    for (int i = 0; i < steps; i++) {
      for (int j = 0; j <= i; j++) {
        System.out.println(i + " " + j + " " + tree.getNode(i, j));
      }
    }
  }
}
