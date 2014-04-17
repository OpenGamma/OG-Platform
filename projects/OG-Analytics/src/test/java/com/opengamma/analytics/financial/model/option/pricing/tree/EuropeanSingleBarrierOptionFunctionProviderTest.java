/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanSingleBarrierOptionFunctionProviderTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 114. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {0.015, 0.05 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.02 };

  /**
   * 
   */
  @Test
  public void priceTrinomialTest() {
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final double[] vols = new double[] {0.02 };

    final int nSteps = 3121;
    final int nStepsAdm = 165;
    final double[] barrierSet = new double[] {90., 112. };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  double exact = price(SPOT, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double res = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  assertEquals(res, exact, Math.max(exact, 1.) * 1.e-2);

                  final OptionFunctionProvider1D functionAdm = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nStepsAdm, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  final LatticeSpecification latticeAdm = new AdaptiveLatticeSpecification(functionAdm);
                  final double resAdm = _modelTrinomial.getPrice(latticeAdm, functionAdm, SPOT, vol, interest, dividend);
                  assertEquals(resAdm, exact, Math.max(exact, 1.) * 1.e-2);
                }
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
  public void greeksTrinomialTest() {
    final double eps = 1.e-6;
    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
    final double[] vols = new double[] {0.02 };

    final int nSteps = 3121;
    final double[] barrierSet = new double[] {90., 112. };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  final double price = price(SPOT, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpotUp = price(SPOT + eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpotDown = price(SPOT - eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpot2Up = price(SPOT + 2. * eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpot2Down = price(SPOT - 2. * eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceTimeUp = price(SPOT, strike, TIME + eps, vol, interest, dividend, isCall, barrier, type);
                  final double priceTimeDown = price(SPOT, strike, TIME - eps, vol, interest, dividend, isCall, barrier, type);
                  final double delta = 0.5 * (priceSpotUp - priceSpotDown) / eps;
                  final double deltaUp = 0.5 * (priceSpot2Up - price) / eps;
                  final double deltaDown = 0.5 * (price - priceSpot2Down) / eps;
                  final double gamma = 0.5 * (deltaUp - deltaDown) / eps;
                  final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;
                  final GreekResultCollection res = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);
                }
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
  public void priceTest() {
    /*
     * Due to slow convergence, only one lattice is used in this test
     */
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();

    /*
     *  As expected, large vol and spot \sim barrier leads to poor accuracy since the effect of discreteness becomes large. 
     */
    final double[] vols = new double[] {0.02 };

    final int nSteps = 3121;
    final double[] barrierSet = new double[] {90., 112. };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  double exact = price(SPOT, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  assertEquals(res, exact, Math.max(exact, 1.) * 1.e-2);
                }
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
  public void greeksTest() {
    final double eps = 1.e-6;
    /*
     * Due to slow convergence, only one lattice is used in this test
     */
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();

    /*
     *  As expected, large vol and spot \sim barrier leads to poor accuracy since the effect of discreteness becomes large. 
     */
    final double[] vols = new double[] {0.02 };

    final int nSteps = 3121;

    final double[] barrierSet = new double[] {90, 112 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  final double price = price(SPOT, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpotUp = price(SPOT + eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpotDown = price(SPOT - eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpot2Up = price(SPOT + 2. * eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceSpot2Down = price(SPOT - 2. * eps, strike, TIME, vol, interest, dividend, isCall, barrier, type);
                  final double priceTimeUp = price(SPOT, strike, TIME + eps, vol, interest, dividend, isCall, barrier, type);
                  final double priceTimeDown = price(SPOT, strike, TIME - eps, vol, interest, dividend, isCall, barrier, type);
                  final double delta = 0.5 * (priceSpotUp - priceSpotDown) / eps;
                  final double deltaUp = 0.5 * (priceSpot2Up - price) / eps;
                  final double deltaDown = 0.5 * (price - priceSpot2Down) / eps;
                  final double gamma = 0.5 * (deltaUp - deltaDown) / eps;
                  final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;
                  final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);
                }
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
  public void discreteDividendPriceTest() {
    /*
     * Due to slow convergence, only one lattice is used in this test
     */
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();

    final double[] propDividends = new double[] {0.002, 0.001, 0.002 };
    final double[] cashDividends = new double[] {.2, 1.1, .5 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final double[] vols = new double[] {0.02 };
    final double[] barrierSet = new double[] {90, 112 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };

    final int nSteps = 3121;

    final LatticeSpecification latticeTri = new TianLatticeSpecification();
    final int nStepsTri = 121;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                    EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));

                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);

                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double resMod = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                final double resRes = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);

                double exactMod = price(modSpot, strike, TIME, vol, interest, 0., isCall, barrier, type);
                assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * 1.e-2);
                double exactRes = price(resSpot, strike, TIME, vol, interest, 0., isCall, barrier, type);
                assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * 1.e-2);

                final OptionFunctionProvider1D functionTri = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nStepsTri, isCall, barrier,
                    EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                final double resTriCash = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, cashDividend);
                final double resTriProp = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, propDividend);
                assertEquals(resTriCash, exactMod, Math.max(exactMod, 1.) * 1.e-1);
                assertEquals(resTriProp, exactRes, Math.max(exactRes, 1.) * 1.e-1);
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
  public void discreteDividendGreeksTest() {
    final double eps = 1.e-6;
    /*
     * Due to slow convergence, only one lattice is used in this test
     */
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    final int nSteps = 3121;

    final double[] propDividends = new double[] {0.002, 0.001, 0.002 };
    final double[] cashDividends = new double[] {.2, 1.1, .5 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final double[] vols = new double[] {0.02 };
    final double[] barrierSet = new double[] {90, 112 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };

    final LatticeSpecification latticeTri = new TianLatticeSpecification();
    final int nStepsTri = 2121;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                    EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                final OptionFunctionProvider1D functionTri = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nStepsTri, isCall, barrier,
                    EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));

                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);

                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                {
                  final double price = price(modSpot, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpotUp = price(modSpot + eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpotDown = price(modSpot - eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpot2Up = price(modSpot + 2. * eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpot2Down = price(modSpot - 2. * eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceTimeUp = price(modSpot, strike, TIME + eps, vol, interest, 0., isCall, barrier, type);
                  final double priceTimeDown = price(modSpot, strike, TIME - eps, vol, interest, 0., isCall, barrier, type);
                  final double delta = 0.5 * (priceSpotUp - priceSpotDown) / eps;
                  final double deltaUp = 0.5 * (priceSpot2Up - price) / eps;
                  final double deltaDown = 0.5 * (price - priceSpot2Down) / eps;
                  final double gamma = 0.5 * (deltaUp - deltaDown) / eps;
                  final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;
                  final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);
                  assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-1);
                  assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);

                  final GreekResultCollection resTri = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, cashDividend);
                  assertEquals(resTri.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-2);
                  assertEquals(resTri.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-1);
                  assertEquals(resTri.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(resTri.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);
                }
                {
                  final double price = price(resSpot, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpotUp = price(resSpot + eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpotDown = price(resSpot - eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpot2Up = price(resSpot + 2. * eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceSpot2Down = price(resSpot - 2. * eps, strike, TIME, vol, interest, 0., isCall, barrier, type);
                  final double priceTimeUp = price(resSpot, strike, TIME + eps, vol, interest, 0., isCall, barrier, type);
                  final double priceTimeDown = price(resSpot, strike, TIME - eps, vol, interest, 0., isCall, barrier, type);
                  final double delta = 0.5 * (priceSpotUp - priceSpotDown) / eps;
                  final double deltaUp = 0.5 * (priceSpot2Up - price) / eps;
                  final double deltaDown = 0.5 * (price - priceSpot2Down) / eps;
                  final double gamma = 0.5 * (deltaUp - deltaDown) / eps;
                  final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;
                  final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
                  assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-2);
                  assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);

                  final GreekResultCollection resTri = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, propDividend);
                  assertEquals(resTri.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-1);
                  assertEquals(resTri.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-1);
                  assertEquals(resTri.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-1);
                  assertEquals(resTri.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * non-constant volatility and interest rate
   */
  @Test
  public void timeVaryingVolTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 801;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 691;
    final double[] volTri = new double[stepsTri];
    final double[] rateTri = new double[stepsTri];
    final double[] dividendTri = new double[stepsTri];
    final double constA = 0.01;
    final double constB = 0.001;
    final double constC = 0.1;
    final double constD = 0.05;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double time : time_set) {
          for (int i = 0; i < steps; ++i) {
            rate[i] = constA + constB * i * time / steps;
            vol[i] = constC + constD * Math.sin(i * time / steps);
            dividend[i] = 0.005;
          }
          for (int i = 0; i < stepsTri; ++i) {
            rateTri[i] = constA + constB * i * time / steps;
            volTri[i] = constC + constD * Math.sin(i * time / steps);
            dividendTri[i] = 0.005;
          }
          final double rateRef = constA + 0.5 * constB * time;
          final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));

          final double[] barrierSet = new double[] {SPOT * 0.9, SPOT * 1.1 };
          for (final double barrier : barrierSet) {
            final OptionFunctionProvider1D functionBarrierDown = new EuropeanSingleBarrierOptionFunctionProvider(strike, time, steps, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUp = new EuropeanSingleBarrierOptionFunctionProvider(strike, time, steps, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.UpAndOut);

            final double resPriceBarrierDown = _model.getPrice(functionBarrierDown, SPOT, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierDown = _model.getGreeks(functionBarrierDown, SPOT, vol, rate, dividend);
            final double resPriceConstBarrierDown = _model.getPrice(lattice1, functionBarrierDown, SPOT, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierDown = _model.getGreeks(lattice1, functionBarrierDown, SPOT, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierDown, resPriceConstBarrierDown, Math.max(Math.abs(resPriceConstBarrierDown), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierDown.get(Greek.FAIR_PRICE), resGreeksConstBarrierDown.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.DELTA), resGreeksConstBarrierDown.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.GAMMA), resGreeksConstBarrierDown.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.THETA), resGreeksConstBarrierDown.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.THETA)), 0.1));

            final double resPriceBarrierUp = _model.getPrice(functionBarrierUp, SPOT, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierUp = _model.getGreeks(functionBarrierUp, SPOT, vol, rate, dividend);
            final double resPriceConstBarrierUp = _model.getPrice(lattice1, functionBarrierUp, SPOT, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierUp = _model.getGreeks(lattice1, functionBarrierUp, SPOT, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierUp, resPriceConstBarrierUp, Math.max(Math.abs(resPriceConstBarrierUp), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierUp.get(Greek.FAIR_PRICE), resGreeksConstBarrierUp.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.DELTA), resGreeksConstBarrierUp.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.GAMMA), resGreeksConstBarrierUp.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.THETA), resGreeksConstBarrierUp.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.THETA)), 0.1));

            final OptionFunctionProvider1D functionBarrierDownTri = new EuropeanSingleBarrierOptionFunctionProvider(strike, time, stepsTri, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUpTri = new EuropeanSingleBarrierOptionFunctionProvider(strike, time, stepsTri, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.UpAndOut);

            final double resPriceBarrierDownTri = _modelTrinomial.getPrice(functionBarrierDownTri, SPOT, volTri, rateTri, dividendTri);
            final GreekResultCollection resGreeksBarrierDownTri = _modelTrinomial.getGreeks(functionBarrierDownTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resPriceBarrierDownTri, resPriceConstBarrierDown, Math.max(Math.abs(resPriceConstBarrierDown), 1.) * 1.e-1);
            assertEquals(resGreeksBarrierDownTri.get(Greek.FAIR_PRICE), resGreeksConstBarrierDown.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.FAIR_PRICE)), 1.) * 0.1);
            assertEquals(resGreeksBarrierDownTri.get(Greek.DELTA), resGreeksConstBarrierDown.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.DELTA)), 1.) * 0.1);
            assertEquals(resGreeksBarrierDownTri.get(Greek.GAMMA), resGreeksConstBarrierDown.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.GAMMA)), 1.) * 0.1);
            assertEquals(resGreeksBarrierDownTri.get(Greek.THETA), resGreeksConstBarrierDown.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.THETA)), 1.));

            final double resPriceBarrierUpTri = _modelTrinomial.getPrice(functionBarrierUpTri, SPOT, volTri, rateTri, dividendTri);
            final GreekResultCollection resGreeksBarrierUpTri = _modelTrinomial.getGreeks(functionBarrierUpTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resPriceBarrierUpTri, resPriceConstBarrierUp, Math.max(Math.abs(resPriceConstBarrierUp), 1.) * 1.e-1);
            assertEquals(resGreeksBarrierUpTri.get(Greek.FAIR_PRICE), resGreeksConstBarrierUp.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.FAIR_PRICE)), 1.) * 0.1);
            assertEquals(resGreeksBarrierUpTri.get(Greek.DELTA), resGreeksConstBarrierUp.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.DELTA)), 1.) * 0.1);
            assertEquals(resGreeksBarrierUpTri.get(Greek.GAMMA), resGreeksConstBarrierUp.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.GAMMA)), 1.) * 0.1);
            assertEquals(resGreeksBarrierUpTri.get(Greek.THETA), resGreeksConstBarrierUp.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.THETA)), 1.));
          }

        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void getBarrierTest() {
    final EuropeanSingleBarrierOptionFunctionProvider function = new EuropeanSingleBarrierOptionFunctionProvider(STRIKES[2], 1., 101, true, 90.,
        EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut"));
    assertEquals(function.getBarrier(), 90.);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeBarrierTest() {
    new EuropeanSingleBarrierOptionFunctionProvider(STRIKES[2], 1., 101, true, -2., EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut"));
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = NotImplementedException.class)
  public void downInBarrierTest() {
    new EuropeanSingleBarrierOptionFunctionProvider(STRIKES[2], 1., 101, true, 90., EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndIn"));
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = NotImplementedException.class)
  public void upInBarrierTest() {
    new EuropeanSingleBarrierOptionFunctionProvider(STRIKES[2], 1., 101, true, 90., EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("UpAndIn"));
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes type = EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut");
    final OptionFunctionProvider1D ref = new EuropeanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., type);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new EuropeanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., type),
        new EuropeanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 91., type),
        new EuropeanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("UpAndOut")),
        new EuropeanVanillaOptionFunctionProvider(100., 1., 53, true), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true)));
  }

  private double price(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final boolean isCall, final double barrier,
      final String type) {
    double exact = 0.;
    if (type == "DownAndOut") {
      if (strike > barrier) {
        exact = isCall ? getA(spot, strike, time, vol, interest, dividend, 1.) - getC(spot, strike, time, vol, interest, dividend, barrier, 1., 1.) : getA(
            spot, strike, time, vol, interest, dividend, -1.) - getB(spot, strike, time, vol, interest, dividend, barrier, -1.) +
            getC(spot, strike, time, vol, interest, dividend, barrier, -1., 1.) - getD(spot, strike, time, vol, interest, dividend, barrier, -1., 1.);
        exact = exact < 0. ? 0. : exact;
        exact = spot <= barrier ? 0. : exact;
      } else {
        exact = isCall ? getB(spot, strike, time, vol, interest, dividend, barrier, 1.) - getD(spot, strike, time, vol, interest, dividend, barrier, 1., 1.) : 0.;
        exact = exact < 0. ? 0. : exact;
        exact = spot <= barrier ? 0. : exact;
      }
    } else {
      if (strike < barrier) {
        exact = !isCall ? getA(spot, strike, time, vol, interest, dividend, -1.) - getC(spot, strike, time, vol, interest, dividend, barrier, -1., -1.) : getA(
            spot, strike, time, vol, interest, dividend, 1.) - getB(spot, strike, time, vol, interest, dividend, barrier, 1.) +
            getC(spot, strike, time, vol, interest, dividend, barrier, 1., -1.) - getD(spot, strike, time, vol, interest, dividend, barrier, 1., -1.);
        exact = exact < 0. ? 0. : exact;
        exact = spot >= barrier ? 0. : exact;
      } else {
        exact = !isCall ? getB(spot, strike, time, vol, interest, dividend, barrier, -1.) - getD(spot, strike, time, vol, interest, dividend, barrier, -1., -1.) : 0.;
        exact = exact < 0. ? 0. : exact;
        exact = spot >= barrier ? 0. : exact;
      }
    }
    return exact;
  }

  /*
   * Tests below are for debugging
   */

  /**
   * Showing slow convergence for non-small vol
   */
  @Test(enabled = false)
  public void price1Test() {

    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    final double vol = 0.2;

    final int nSteps = 298121;
    final double barrier = 85;
    String type = "DownAndOut";
    final boolean isCall = true;
    final double strike = 110.;
    final double interest = 0.08;
    final double dividend = 0.02;
    final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
        EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
    double exact = price(SPOT, strike, TIME, vol, interest, dividend, isCall, barrier, type);
    final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
    assertEquals(res, exact, Math.max(exact, 1.) * 1.e-3);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void printTest() {
    final double barrier = 90.0;
    final double strike = 105.1;
    final double vol = 0.09;
    final double interest = -0.01;
    final double dividend = 0.0;
    final boolean isCall = false;

    for (int i = 0; i < 500; ++i) {
      final int nSteps = 2001 + 6 * i;
      final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
          EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut"));
      //      final LatticeSpecification lattice = new TrigeorgisLatticeSpecification();
      //      final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();
      final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
      double exact = isCall ? getA(SPOT, strike, TIME, vol, interest, dividend, 1.) - getC(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., 1.) : getA(
          SPOT, strike, TIME, vol, interest, dividend, -1.) -
          getB(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) +
          getC(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.) -
          getD(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.);
      exact = exact < 0. ? 0. : exact;
      exact = SPOT <= barrier ? 0. : exact;
      final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
      System.out.println(nSteps + "\t" + (res - exact));
    }
  }

  /**
   * 
   */
  @Test(enabled = false)
  public void priceLeisenReimerTest() {
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    /*
     *  As expected, large vol and spot \sim barrier leads to poor accuracy since the effect of discreteness becomes large. 
     */
    final double[] vols = new double[] {0.02, 0.09 };
    final double eps = 1.e-2;
    final int nSteps = 1189;

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final boolean isCall : tfSet) {
          for (final double strike : STRIKES) {
            for (final double interest : INTERESTS) {
              for (final double vol : vols) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                      EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  if (type == "DownAndOut") {
                    if (strike > barrier) {
                      double exact = isCall ? getA(SPOT, strike, TIME, vol, interest, dividend, 1.) - getC(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., 1.) : getA(
                          SPOT, strike, TIME, vol, interest, dividend, -1.) -
                          getB(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) +
                          getC(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.) -
                          getD(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.);
                      exact = exact < 0. ? 0. : exact;
                      exact = SPOT <= barrier ? 0. : exact;
                      final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                      System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + dividend + "\t" + isCall);
                      assertEquals(res, exact, Math.max(exact, 1.) * eps);
                    } else {
                      double exact = isCall ? getB(SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) - getD(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., 1.) : 0.;
                      exact = exact < 0. ? 0. : exact;
                      exact = SPOT <= barrier ? 0. : exact;
                      final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                      //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                      assertEquals(res, exact, Math.max(exact, 1.) * eps);
                    }
                  } else {
                    if (strike < barrier) {
                      double exact = !isCall ? getA(SPOT, strike, TIME, vol, interest, dividend, -1.) - getC(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., -1.) : getA(
                          SPOT, strike, TIME, vol, interest, dividend, 1.) -
                          getB(SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) +
                          getC(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., -1.) -
                          getD(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., -1.);
                      exact = exact < 0. ? 0. : exact;
                      exact = SPOT >= barrier ? 0. : exact;
                      final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                      //                        System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                      assertEquals(res, exact, Math.max(exact, 1.) * eps);
                    } else {
                      double exact = !isCall ? getB(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) - getD(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., -1.) : 0.;
                      exact = exact < 0. ? 0. : exact;
                      exact = SPOT >= barrier ? 0. : exact;
                      final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                      //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                      assertEquals(res, exact, Math.max(exact, 1.) * eps);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private double getA(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final double phi) {
    final boolean isCall = (phi == 1.);
    return BlackScholesFormulaRepository.price(spot, strike, time, vol, interest, interest - dividend, isCall);
  }

  private double getB(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final double barrier, final double phi) {
    final double sigmaRootT = vol * Math.sqrt(time);
    final double x2 = (Math.log(spot / barrier) + interest * time - dividend * time) / sigmaRootT + 0.5 * sigmaRootT;
    final double x2M = x2 - sigmaRootT;
    return phi * (spot * Math.exp(-dividend * time) * NORMAL.getCDF(phi * x2) - strike * Math.exp(-interest * time) * NORMAL.getCDF(phi * x2M));
  }

  private double getC(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final double barrier, final double phi,
      final double eta) {
    final boolean isCall = (eta == 1.);
    final double mu = (interest - dividend) / vol / vol - 0.5;
    return phi * eta * BlackScholesFormulaRepository.price(barrier * barrier / spot, strike, time, vol, interest, interest - dividend, isCall) * Math.pow(barrier / spot, 2. * mu);
  }

  private double getD(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final double barrier, final double phi,
      final double eta) {
    final double sigmaRootT = vol * Math.sqrt(time);
    final double y2 = (Math.log(barrier / spot) + interest * time - dividend * time) / sigmaRootT + 0.5 * sigmaRootT;
    final double y2M = y2 - sigmaRootT;
    final double mu = (interest - dividend) / vol / vol - 0.5;
    return phi *
        (spot * Math.exp(-dividend * time) * Math.pow(barrier / spot, 2. * mu + 2.) * NORMAL.getCDF(eta * y2) - strike * Math.exp(-interest * time) * Math.pow(barrier / spot, 2. * mu) *
            NORMAL.getCDF(eta * y2M));
  }
}
