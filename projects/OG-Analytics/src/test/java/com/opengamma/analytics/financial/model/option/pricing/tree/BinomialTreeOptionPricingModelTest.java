/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

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

  @Test(enabled = false)
  public void aTest() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final double[] res = _model.getEuropeanGreeks(lattice, 120, 110, 1., 1., 1., 1001, true);
    final double price = _model.getEuropeanPrice(lattice, 120, 110, 1., 1., 1., 1001, true);
    System.out.println(new DoubleMatrix1D(res));
    System.out.println(price);
  }

  @Test
  public void latticeBStest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new LogEqualProbabiliesLatticeSpecification(), new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    //    long startTime = System.currentTimeMillis();
    //    int i = 0;
    //    while (i < 100) {
    //      ++i;
    //    System.out.println(_model.getEuropeanPrice(lattice, 120., 100., 1., 1., 1., 100));
    //    }
    //    long finishTime = System.currentTimeMillis();
    //    System.out.println("That took: " + (finishTime - startTime) + " ms");

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {101, 512, 1001 };
              for (final int nSteps : choicesSteps) {
                final double exact = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double res = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                final double ref = Math.max(exact, 1.) / Math.sqrt(nSteps);
                assertEquals(res, exact, ref);
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void LeisenReimerTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };
    //    long startTime = System.currentTimeMillis();
    //    int i = 0;
    //    while (i < 100) {
    //      ++i;
    //    System.out.println(_model.getEuropeanPrice(lattice, 120., 100., 1., 1., 1., 100));
    //    }
    //    long finishTime = System.currentTimeMillis();
    //    System.out.println("That took: " + (finishTime - startTime) + " ms");

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {111, 509, 1001 };
              for (final int nSteps : choicesSteps) {
                final double exact = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double res = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                final double ref = Math.max(exact, 1.) / nSteps / nSteps;
                assertEquals(res, exact, ref);
              }
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
  public void greektest() {
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
            final int[] choicesSteps = new int[] {101, 512, 1001 };
            for (final int nSteps : choicesSteps) {
              final double delta = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest, isCall);
              final double gamma = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest);
              final double theta = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest, isCall);
              final double[] res = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
              final double refDelta = Math.max(delta, 1.) / nSteps;
              final double refGamma = Math.max(gamma, 1.) / nSteps;
              final double refTheta = Math.max(theta, 1.) / nSteps;
              assertEquals(res[1], delta, refDelta);
              assertEquals(res[2], gamma, refGamma);
              assertEquals(res[3], theta, refTheta);
            }
          }
        }
      }
    }

  }
}
