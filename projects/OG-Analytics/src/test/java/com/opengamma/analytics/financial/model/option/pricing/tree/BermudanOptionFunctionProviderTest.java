/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;

/**
 * 
 */
public class BermudanOptionFunctionProviderTest {
  private static final TreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();

  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 114., };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {0.015, 0.05, 0.1 };
  private static final double[] VOLS = new double[] {0.04, 0.15, 0.4 };

  private static final double[] DIVIDENDS = new double[] {0.02, 0.09 };

  /**
   * the exercise times agree with nodes. Then the option price is the same as the American vanilla option
   */
  @Test
  public void reduceToAmericanPriceTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };

    final double time = 5.;
    final double[] exerciseTimes = new double[] {0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5 };
    final int steps = 10;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            final double vol = 0.5;
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new BermudanOptionFunctionProvider(strike, time, steps, isCall, exerciseTimes);
              final OptionFunctionProvider1D functionAm = new AmericanVanillaOptionFunctionProvider(strike, time, steps, isCall);
              final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
              final double resAm = _model.getPrice(lattice, functionAm, SPOT, vol, interest, dividend);
              assertEquals(res, resAm, 1.e-14);
            }
          }
        }
      }
    }
  }

  /**
   * European =< Bermudan =< American should hold
   */
  @Test
  public void betweenEuropeanAndAmericanPriceTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double time = 5.;
    final double[] exerciseTimes = new double[] {0.1, 1.4, 1.5, 1.6, 2.5, 3.1, 10. / 3., 4.0, 5.0 };
    final int steps = 37;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider1D function = new BermudanOptionFunctionProvider(strike, time, steps, isCall, exerciseTimes);
                final OptionFunctionProvider1D functionEu = new EuropeanVanillaOptionFunctionProvider(strike, time, steps, isCall);
                final OptionFunctionProvider1D functionAm = new AmericanVanillaOptionFunctionProvider(strike, time, steps, isCall);
                final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                final double resAm = _model.getPrice(lattice, functionAm, SPOT, vol, interest, dividend);
                final double resEu = _model.getPrice(lattice, functionEu, SPOT, vol, interest, dividend);
                assertTrue(res <= resAm && res >= resEu);
              }
            }
          }
        }
      }
    }
  }

  /**
   * the exercise times agree with nodes. Then the option price is the same as the American vanilla option
   */
  @Test
  public void reduceToAmericanGreeksTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };

    final double time = 5.;
    final double[] exerciseTimes = new double[] {0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5 };
    final int steps = 10;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            final double vol = 0.5;
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new BermudanOptionFunctionProvider(strike, time, steps, isCall, exerciseTimes);
              final OptionFunctionProvider1D functionAm = new AmericanVanillaOptionFunctionProvider(strike, time, steps, isCall);
              final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
              final GreekResultCollection resAm = _model.getGreeks(lattice, functionAm, SPOT, vol, interest, dividend);
              assertEquals(res.get(Greek.FAIR_PRICE), resAm.get(Greek.FAIR_PRICE), 1.e-14);
              assertEquals(res.get(Greek.DELTA), resAm.get(Greek.DELTA), 1.e-14);
              assertEquals(res.get(Greek.GAMMA), resAm.get(Greek.GAMMA), 1.e-14);
              assertEquals(res.get(Greek.THETA), resAm.get(Greek.THETA), 1.e-14);
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void reduceToEuropeanGreeksTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] exerciseTimes = new double[] {4.2 };
    final int steps = 37;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider1D function = new BermudanOptionFunctionProvider(strike, TIME, steps, isCall, exerciseTimes);
                final OptionFunctionProvider1D functionEu = new EuropeanVanillaOptionFunctionProvider(strike, TIME, steps, isCall);
                final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                final GreekResultCollection resEu = _model.getGreeks(lattice, functionEu, SPOT, vol, interest, dividend);
                assertEquals(res.get(Greek.FAIR_PRICE), resEu.get(Greek.FAIR_PRICE), 1.e-14);
                assertEquals(res.get(Greek.DELTA), resEu.get(Greek.DELTA), 1.e-14);
                assertEquals(res.get(Greek.GAMMA), resEu.get(Greek.GAMMA), 1.e-14);
                assertEquals(res.get(Greek.THETA), resEu.get(Greek.THETA), 1.e-14);
              }
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void getExerciseTimesTest() {
    final int steps = 1253;
    final double dt = TIME / steps;
    final double eps = dt * 0.1;
    final int[] expectedSteps = new int[] {3, 349, 21, 1143 };
    final double[] exerciseTimes = new double[] {dt * expectedSteps[0] + eps, dt * expectedSteps[2] - eps, dt * expectedSteps[1] - 3. * eps, dt * expectedSteps[3] };
    final int nTimes = exerciseTimes.length;
    final BermudanOptionFunctionProvider function = new BermudanOptionFunctionProvider(100., TIME, steps, true, exerciseTimes);

    Arrays.sort(expectedSteps);
    Arrays.sort(exerciseTimes);
    assertEquals(function.getNumberOfExerciseTimes(), nTimes);
    for (int i = 0; i < nTimes; ++i) {
      assertEquals(function.getExerciseSteps()[i], expectedSteps[i]);
      assertEquals(function.getExerciseTimes()[i], exerciseTimes[i], 1.e-14);
    }
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooManyTimesTest() {
    final int steps = 5;
    final double[] exerciseTimes = new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0 };
    new BermudanOptionFunctionProvider(100., TIME, steps, true, exerciseTimes);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideStepsTest() {
    final int steps = 12;
    final double[] exerciseTimes = new double[] {0.5, 1.0, 1.5, 2.0, 2.00001, 3.0, 3.5, 4.0 };
    new BermudanOptionFunctionProvider(100., TIME, steps, true, exerciseTimes);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimesTest() {
    final int steps = 55;
    final double[] exerciseTimes = new double[] {0.5, 1.0, -1.5, 2.0, 2.5, 3.0, 3.5, 4.0 };
    new BermudanOptionFunctionProvider(100., TIME, steps, true, exerciseTimes);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeTimesTest() {
    final int steps = 95;
    final double[] exerciseTimes = new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 9.5, 4.0 };
    new BermudanOptionFunctionProvider(100., TIME, steps, true, exerciseTimes);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider1D ref = new BermudanOptionFunctionProvider(100., 5., 50, true, new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5 });
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref,
        new BermudanOptionFunctionProvider(100., 5., 50, true, new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5 }),
        new BermudanOptionFunctionProvider(100., 5., 50, true, new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.4, 4.5 }),
        new BermudanOptionFunctionProvider(100., 10., 50, true, new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9. }),
        new AmericanVanillaOptionFunctionProvider(100., 5., 50, true), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanSpreadOptionFunctionProvider(110., 1., 53, true)));
  }

  /**
   * Test below is for debugging
   */
  @Test(enabled = false)
  public void sampleDataPrintTest() {
    final double spot = 100.;
    final double strike = 100.;
    final double time = 5.;
    final double[] exerciseTimes = new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5 };
    final double[] vols = new double[] {0.05, 0.3, 0.8 };
    final double interest = 0.05;
    final double[] divs = new double[] {0.02, 0.10 };
    final int steps = 416;

    //    new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
    //    new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification()

    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();

    for (final double dividend : divs) {
      for (final double vol : vols) {
        final boolean[] tfSet = new boolean[] {true, false };
        for (final boolean isCall : tfSet) {
          final OptionFunctionProvider1D function = new BermudanOptionFunctionProvider(strike, time, steps, isCall, exerciseTimes);
          final OptionFunctionProvider1D functionAm = new AmericanVanillaOptionFunctionProvider(strike, time, steps, isCall);
          final double res = _model.getPrice(lattice, function, spot, vol, interest, dividend);
          final double resAm = _model.getPrice(lattice, functionAm, spot, vol, interest, dividend);
          System.out.println(res + "\t" + resAm);
        }
      }
    }
  }
}
