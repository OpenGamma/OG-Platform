/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.CoxRossRubinsteinBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.tree.BinomialOptionModel;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ImpliedTreeTest {
  private static final double SPOT = 100;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(0.05);
  private static final double B = 0.05;
  private static final VolatilitySurface GLOBAL_SIGMA = new ConstantVolatilitySurface(0.15);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(R, B, GLOBAL_SIGMA, SPOT, DATE);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(SPOT, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 5)), true);
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> DEFINITION = new CoxRossRubinsteinBinomialOptionModelDefinition();
  private static final BinomialOptionModel<StandardOptionDataBundle> MODEL = new BinomialOptionModel<StandardOptionDataBundle>(DEFINITION, 5);

  @Test
  public void test() {
    final RecombiningBinomialTree<DoublesPair> result = MODEL.getTreeGeneratingFunction(OPTION).evaluate(DATA);
    @SuppressWarnings("unused")
    final DoublesPair[][] p = result.getTree();
    final RecombiningBinomialTree<Double> prob = DEFINITION.getUpProbabilityTree(OPTION, DATA, 5, RecombiningBinomialTree.NODES.evaluate(5));
    @SuppressWarnings("unused")
    final Double[][] x = prob.getTree();
  }
}
