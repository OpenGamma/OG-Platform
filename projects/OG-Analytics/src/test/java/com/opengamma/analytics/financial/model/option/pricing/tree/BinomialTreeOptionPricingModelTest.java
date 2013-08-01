/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
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
  //not to be changed, look into this later
  public void cTest() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final double priceP = _model.getAmericanPrice(lattice, 120, 110, 1., 1., 1., 10001, false);
    System.out.println(priceP);

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double bsPrice = bs.price(120, 110, 1., 1., 1., 1., false);
    final BaroneAdesiWhaleyModel baw = new BaroneAdesiWhaleyModel();
    final double bawPrice = baw.price(120, 110, 1., 1., 1., 1., false);
    System.out.println(bsPrice);
    System.out.println(bawPrice);

    final double exact = BlackScholesFormulaRepository.price(120, 110, 1., 1., 1., 1., false);
    System.out.println(exact);
  }

  @Test(enabled = false)
  public void aTest() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final double[] res = _model.getEuropeanGreeks(lattice, 120, 110, 1., 1., 1., 1001, true);
    final double price = _model.getEuropeanPrice(lattice, 120, 110, 1., 1., 1., 1001, true);
    System.out.println(new DoubleMatrix1D(res));
    System.out.println(price);
  }

  @Test(enabled = false)
  public void bTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new LogEqualProbabiliesLatticeSpecification(), new TrigeorgisLatticeSpecification(), new TianLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(),
        new LeisenReimerLatticeSpecification() };
    final int nLat = lattices.length;
    for (int i = 0; i < nLat; ++i) {
      final LatticeSpecification lattice = lattices[i];
      final double[] res = _model.getEuropeanGreeks(lattice, 120, 110, 1., 1., 1., 1001, true);
      //    final double price = _model.getEuropeanPrice(lattice, 120, 110, 1., 1., 1., 1001, true);
      System.out.println(new DoubleMatrix1D(res));
      //    System.out.println(price);
    }
  }

  @Test
  public void EuropeanPriceLatticeTest() {
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
              final int[] choicesSteps = new int[] {81, 312, 901 };
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
  public void EuropeanPriceLeisenReimerTest() {
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
              final int[] choicesSteps = new int[] {81, 309, 901 };
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

  @Test
  public void EuropeanGreekLatticesTest() {
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
              final int[] choicesSteps = new int[] {81, 312, 901 };
              for (final int nSteps : choicesSteps) {
                final double delta = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double gamma = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest);
                final double theta = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double[] res = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                final double refDelta = Math.max(delta, 1.) / Math.sqrt(nSteps);
                final double refGamma = Math.max(gamma, 1.) / Math.sqrt(nSteps);
                final double refTheta = Math.max(theta, 1.) / Math.sqrt(nSteps);
                assertEquals(res[1], delta, refDelta);
                assertEquals(res[2], gamma, refGamma);
                assertEquals(res[3], theta, refTheta * 10.);
              }
            }
          }
        }
      }
    }

  }

  @Test
  public void EuropeanGreekLeisenReimerTest() {
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();

    final boolean[] tfSet = new boolean[] {true, false };

    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int[] choicesSteps = new int[] {81, 309, 901 };
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
              assertEquals(res[3], theta, refTheta * 10.);
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
  public void americanPutLatticesTest() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final int[] steps = new int[] {81, 312, 901 };
    final int nSteps = steps.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;

    final double[][][][] expected = new double[][][][] {{ { {20.580173252234907, 21.942223907574643, 48.194668768125879 }, {6.389319154050021, 10.478389319791065, 42.302627194873281 }, {
        2.503527748767755, 6.709214883123458, 39.887484048675731 }, {2.469969456283047, 6.669699699715626, 39.857294759348264 }, {
        0.622670446678808, 3.827071169431741, 37.170448009203476 }, {0.003538689265214, 0.678491317002307, 31.428511140208283 } },
    { {24.016023492425845, 24.917192859584063, 49.547934789112901 }, {
        9.291672658012988, 12.858508238468072, 43.652930582658598 }, {
        4.303700896193302, 8.595982998651463, 41.223343675041107 }, {
        4.255750277572445, 8.550076323888920, 41.192973838695892 }, {
        1.335931558814119, 5.159786445142252, 38.490058403971446 }, {
        0.013933985474957, 1.046608881400952, 32.681515177037554 } }, { {24.353549447375656, 25.216134093974564, 49.683095155463654 }, {
        9.605690928861387, 13.108852400009043, 43.788176685809177 }, {
        4.518279067279778, 8.799779740427999, 41.357306256535004 }, {
        4.468896929914100, 8.753252276882673, 41.326920376169078 }, {
        1.432247713203117, 5.308268150919305, 38.622577023601551 }, {
        0.015850299571867, 1.091018963372994, 32.807739223251829 } }, { {25.691485099795806, 26.412522273295405, 50.223358173712349 }, {
        10.895812057979743, 14.130673142786229, 44.329473342328157 }, {
        5.437944450481051, 9.641586108783972, 41.893762250613783 }, {
        5.382982920652308, 9.592624813536752, 41.863315861967351 }, {
        1.870265924749265, 5.930353657248435, 39.153587272435104 }, {
        0.026157934555348, 1.284224487912077, 33.314245399117027 } }, { {27.335610400148070, 27.906665048634686, 50.897722282769422 }, {
        12.570896978368316, 15.450697403792280, 45.006676824891649 }, {
        6.719230143006900, 10.751936442683183, 42.565576117231096 }, {
        6.657807442360971, 10.700066297395963, 42.535062358385353 }, {
        2.545554804869064, 6.771580211175841, 39.819337821112995 }, {
        0.047391011104531, 1.563444313838434, 33.950894834164750 } } } };

    for (int i = 0; i < 1; ++i) {
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            final double priceP = _model.getAmericanPrice(lattice, SPOT, STRIKES[k], TIME, VOLS[l], INTERESTS[j], steps[i], false);
            assertEquals(priceP, expected[i][j][k][l], expected[i][j][k][l] * 1.e-10);
          }
        }
      }
    }

  }

  //  @Test
  //  public void LeisenReimerAmericanTest() {
  //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };
  //    //    long startTime = System.currentTimeMillis();
  //    //    int i = 0;
  //    //    while (i < 100) {
  //    //      ++i;
  //    //    System.out.println(_model.getEuropeanPrice(lattice, 120., 100., 1., 1., 1., 100));
  //    //    }
  //    //    long finishTime = System.currentTimeMillis();
  //    //    System.out.println("That took: " + (finishTime - startTime) + " ms");
  //
  //    for (final LatticeSpecification lattice : lattices) {
  //        for (final double strike : STRIKES) {
  //          for (final double interest : INTERESTS) {
  //            for (final double vol : VOLS) {
  //              final int[] choicesSteps = new int[] {81, 309, 901 };
  //              for (final int nSteps : choicesSteps) {
  //                final double res = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, false);
  //                final double ref = Math.max(expected[i][j][k], 1.) / nSteps / nSteps;
  //                assertEquals(res, exact, ref);
  //              }
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }

}
