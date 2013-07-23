/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;

/**
 * 
 */
public class BinomialTreeOptionPricingModelTest {

  final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {81., 97., 105., 105.1, 114., 138. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0., 0.001, 0.005, 0.01 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };

  @Test
  public void test() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    //    long startTime = System.currentTimeMillis();
    //    int i = 0;
    //    while (i < 100) {
    //      ++i;
    //    System.out.println(_model.getEuropeanPrice(lattice, 120., 100., 1., 1., 1., 100));
    //    }
    //    long finishTime = System.currentTimeMillis();
    //    System.out.println("That took: " + (finishTime - startTime) + " ms");

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int[] choicesSteps = new int[] {101, 512, 1001, 2204 };
            for (final int nSteps : choicesSteps) {
              final double exact = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest, isCall);
              final double res = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
              assertEquals(res, exact, Math.max(exact, 1.) / nSteps);

            }
          }
        }
      }
    }
  }
}
