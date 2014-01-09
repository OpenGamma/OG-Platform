/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * American type of single barrier option
 */
@Test(groups = TestGroup.UNIT)
public class AmericanSingleBarrierOptionFunctionProviderTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 114. };
  private static final double TIME = 4.2;
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] INTERESTS = new double[] {0.01, 0.05 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.02 };

  /**
   * 
   */
  @Test
  public void putCallSymmetryTest() {
    /*
     * Two sample lattices are checked 
     */
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final int steps = 251;
    final double[] barrierSet = new double[] {92, 116 };
    AmericanSingleBarrierOptionFunctionProvider.BarrierTypes dao = AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut");
    AmericanSingleBarrierOptionFunctionProvider.BarrierTypes uao = AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("UpAndOut");
    for (final double strike : STRIKES) {
      for (final double barrier : barrierSet) {
        final OptionFunctionProvider1D functionCall = new AmericanSingleBarrierOptionFunctionProvider(strike, TIME, steps, true, barrier, dao);
        final OptionFunctionProvider1D functionPut = new AmericanSingleBarrierOptionFunctionProvider(SPOT * SPOT / strike, TIME, steps, false, SPOT * SPOT / barrier, uao);
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              for (final LatticeSpecification lattice : lattices) {
                final GreekResultCollection greekCall = _model.getGreeks(lattice, functionCall, SPOT, vol, interest, dividend);
                final GreekResultCollection greekPut = _model.getGreeks(lattice, functionPut, SPOT, vol, dividend, interest);
                final double priceCall = greekCall.get(Greek.FAIR_PRICE);
                final double thetaCall = greekCall.get(Greek.THETA);
                final double pricePut = greekPut.get(Greek.FAIR_PRICE);
                final double thetaPut = greekPut.get(Greek.THETA);
                final double refPrice = strike * pricePut / SPOT;
                final double refTheta = strike * thetaPut / SPOT;
                assertEquals(priceCall, refPrice, Math.max(refPrice, 1.) * 1.e-5);
                assertEquals(thetaCall, refTheta, Math.max(Math.abs(refTheta), 1.) * 1.e-2);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Check the case when analytic approximation is available
   */
  @Test
  public void priceTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] vols = new double[] {0.1, 0.15 };
    final double time = 0.1;
    final int nSteps = 511;

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  for (final double dividend : DIVIDENDS) {
                    final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, time, nSteps, isCall, barrier,
                        AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    if (type == "DownAndOut") {
                      if (strike > barrier) {
                        double exact = isCall ? getA(SPOT, strike, time, vol, interest, dividend, 1.) - getC(SPOT, strike, time, vol, interest, dividend, barrier, 1., 1.) : getA(
                            SPOT, strike, time, vol, interest, dividend, -1.) - getB(SPOT, strike, time, vol, interest, dividend, barrier, -1.) +
                            getC(SPOT, strike, time, vol, interest, dividend, barrier, -1., 1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, -1., 1.);
                        exact = exact < 0. ? 0. : exact;
                        final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                        assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                            lattice instanceof TianLatticeSpecification) {
                          final double resTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                          assertEquals(resTrinomial, exact, Math.max(exact, 1.) * 1.e-1);
                        }
                      } else {
                        double exact = isCall ? getB(SPOT, strike, time, vol, interest, dividend, barrier, 1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, 1., 1.) : 0.;
                        exact = exact < 0. ? 0. : exact;
                        final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                        assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                            lattice instanceof TianLatticeSpecification) {
                          final double resTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                          assertEquals(resTrinomial, exact, Math.max(exact, 1.) * 1.e-2);
                        }
                      }
                    } else {
                      if (strike < barrier) {
                        double exact = !isCall ? getA(SPOT, strike, time, vol, interest, dividend, -1.) - getC(SPOT, strike, time, vol, interest, dividend, barrier, -1., -1.) : getA(
                            SPOT, strike, time, vol, interest, dividend, 1.) - getB(SPOT, strike, time, vol, interest, dividend, barrier, 1.) +
                            getC(SPOT, strike, time, vol, interest, dividend, barrier, 1., -1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, 1., -1.);
                        exact = exact < 0. ? 0. : exact;
                        final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                        assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                            lattice instanceof TianLatticeSpecification) {
                          final double resTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                          assertEquals(resTrinomial, exact, Math.max(exact, 1.) * 1.e-1);
                        }
                      } else {
                        double exact = !isCall ? getB(SPOT, strike, time, vol, interest, dividend, barrier, -1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, -1., -1.) : 0.;
                        exact = exact < 0. ? 0. : exact;
                        final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                        assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                            lattice instanceof TianLatticeSpecification) {
                          final double resTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                          assertEquals(resTrinomial, exact, Math.max(exact, 1.) * 1.e-2);
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
    }
  }

  /**
   * 
   */
  @Test
  public void discreteDividendsPriceTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final double eps = 1.e-1;

    final double[] propDividends = new double[] {0.002, 0.004, 0.001 };
    final double[] cashDividends = new double[] {.2, .3, .2 };
    final double time = 0.05;
    final double[] dividendTimes = new double[] {time / 6., time / 3., time / 2. };

    final double[] vols = new double[] {0.1, 0.15 };

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  final int nSteps = 311;
                  final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, time, nSteps, isCall, barrier,
                      AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                  final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                  final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                      Math.exp(-interest * dividendTimes[2]);
                  final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                  final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                  final double resMod = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                  final double resRes = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);

                  double resModTrinomial = 0.;
                  double resResTrinomial = 0.;
                  /**
                   * In at-the-money case trinomial tree produces poor approximation, excluded
                   */
                  final boolean canBeTrinomial = (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification ||
                      lattice instanceof TrigeorgisLatticeSpecification || lattice instanceof TianLatticeSpecification) && strike != STRIKES[1];
                  if (canBeTrinomial) {
                    resModTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                    resResTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, propDividend);

                    final GreekResultCollection greekMod = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);
                    final GreekResultCollection greekRes = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
                    final GreekResultCollection greekModTrinomial = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);
                    final GreekResultCollection greekResTrinomial = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, propDividend);

                    assertEquals(resModTrinomial, resMod, Math.max(resMod, 1.) * eps * eps);
                    assertEquals(resResTrinomial, resRes, Math.max(resRes, 1.) * eps * eps);
                    assertEquals(greekModTrinomial.get(Greek.FAIR_PRICE), greekMod.get(Greek.FAIR_PRICE), Math.max(Math.abs(greekMod.get(Greek.FAIR_PRICE)), 1.) * eps * eps);
                    assertEquals(greekModTrinomial.get(Greek.DELTA), greekMod.get(Greek.DELTA), Math.max(Math.abs(greekMod.get(Greek.DELTA)), 1.) * eps * eps);
                    assertEquals(greekModTrinomial.get(Greek.GAMMA), greekMod.get(Greek.GAMMA), Math.max(Math.abs(greekMod.get(Greek.GAMMA)), 1.) * eps * eps);
                    assertEquals(greekModTrinomial.get(Greek.THETA), greekMod.get(Greek.THETA), Math.max(Math.abs(greekMod.get(Greek.THETA)), 1.) * eps * eps);
                    assertEquals(greekResTrinomial.get(Greek.FAIR_PRICE), greekRes.get(Greek.FAIR_PRICE), Math.max(Math.abs(greekRes.get(Greek.FAIR_PRICE)), 1.) * eps * eps);
                    assertEquals(greekResTrinomial.get(Greek.DELTA), greekRes.get(Greek.DELTA), Math.max(Math.abs(greekRes.get(Greek.DELTA)), 1.) * eps * eps);
                    assertEquals(greekResTrinomial.get(Greek.GAMMA), greekRes.get(Greek.GAMMA), Math.max(Math.abs(greekRes.get(Greek.GAMMA)), 1.) * eps * eps);
                    assertEquals(greekResTrinomial.get(Greek.THETA), greekRes.get(Greek.THETA), Math.max(Math.abs(greekRes.get(Greek.THETA)), 1.) * eps * eps);
                  }

                  if (type == "DownAndOut") {
                    if (strike > barrier) {
                      double exactMod = isCall ? getA(modSpot, strike, time, vol, interest, 0., 1.) - getC(modSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : getA(
                          modSpot, strike, time, vol, interest, 0., -1.) - getB(modSpot, strike, time, vol, interest, 0., barrier, -1.) +
                          getC(modSpot, strike, time, vol, interest, 0., barrier, -1., 1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, -1., 1.);
                      exactMod = barrier >= SPOT ? 0. : exactMod;
                      assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                      double exactRes = isCall ? getA(resSpot, strike, time, vol, interest, 0., 1.) - getC(resSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : getA(
                          resSpot, strike, time, vol, interest, 0., -1.) - getB(resSpot, strike, time, vol, interest, 0., barrier, -1.) +
                          getC(resSpot, strike, time, vol, interest, 0., barrier, -1., 1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, -1., 1.);
                      exactRes = barrier >= SPOT ? 0. : exactRes;
                      assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                    } else {
                      double exactMod = isCall ? getB(modSpot, strike, time, vol, interest, 0., barrier, 1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : 0.;
                      exactMod = barrier >= SPOT ? 0. : exactMod;
                      assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                      double exactRes = isCall ? getB(resSpot, strike, time, vol, interest, 0., barrier, 1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : 0.;
                      exactRes = barrier >= SPOT ? 0. : exactRes;
                      assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                    }
                  } else {
                    if (strike < barrier) {
                      double exactMod = !isCall ? getA(modSpot, strike, time, vol, interest, 0., -1.) - getC(modSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : getA(
                          modSpot, strike, time, vol, interest, 0., 1.) - getB(modSpot, strike, time, vol, interest, 0., barrier, 1.) +
                          getC(modSpot, strike, time, vol, interest, 0., barrier, 1., -1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, 1., -1.);
                      exactMod = barrier <= SPOT ? 0. : exactMod;
                      assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                      double exactRes = !isCall ? getA(resSpot, strike, time, vol, interest, 0., -1.) - getC(resSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : getA(
                          resSpot, strike, time, vol, interest, 0., 1.) - getB(resSpot, strike, time, vol, interest, 0., barrier, 1.) +
                          getC(resSpot, strike, time, vol, interest, 0., barrier, 1., -1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, 1., -1.);
                      exactRes = exactRes < 0. ? 0. : exactRes;
                      exactRes = barrier <= SPOT ? 0. : exactRes;
                      assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                    } else {
                      double exactMod = !isCall ? getB(modSpot, strike, time, vol, interest, 0., barrier, -1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : 0.;
                      exactMod = barrier <= SPOT ? 0. : exactMod;
                      assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                      double exactRes = !isCall ? getB(resSpot, strike, time, vol, interest, 0., barrier, -1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : 0.;
                      exactRes = exactRes < 0. ? 0. : exactRes;
                      exactRes = barrier <= SPOT ? 0. : exactRes;
                      assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
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

  /**
   * Greeks test for trinomial is also included
   */
  @Test
  public void timeVaryingVolTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 797;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 727;
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

          final double[] barrierSet = new double[] {SPOT * 0.9, SPOT * 1.15 };
          for (final double barrier : barrierSet) {
            final OptionFunctionProvider1D functionBarrierDown = new AmericanSingleBarrierOptionFunctionProvider(strike, time, steps, isCall, barrier,
                AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUp = new AmericanSingleBarrierOptionFunctionProvider(strike, time, steps, isCall, barrier,
                AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.UpAndOut);

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

            final OptionFunctionProvider1D functionBarrierDownTri = new AmericanSingleBarrierOptionFunctionProvider(strike, time, stepsTri, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUpTri = new AmericanSingleBarrierOptionFunctionProvider(strike, time, stepsTri, isCall, barrier,
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

            final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
                new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
            for (final LatticeSpecification lattice : lattices) {
              final GreekResultCollection resGreeksBarrierDownTriRe = _modelTrinomial.getGreeks(lattice, functionBarrierDown, SPOT, volRef, rateRef, dividend[0]);
              assertEquals(resGreeksBarrierDownTriRe.get(Greek.FAIR_PRICE), resGreeksConstBarrierDown.get(Greek.FAIR_PRICE),
                  Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierDownTriRe.get(Greek.DELTA), resGreeksConstBarrierDown.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.DELTA)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierDownTriRe.get(Greek.GAMMA), resGreeksConstBarrierDown.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.GAMMA)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierDownTriRe.get(Greek.THETA), resGreeksConstBarrierDown.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.THETA)), 0.1));
              final GreekResultCollection resGreeksBarrierUpTriRe = _modelTrinomial.getGreeks(lattice, functionBarrierUp, SPOT, volRef, rateRef, dividend[0]);
              assertEquals(resGreeksBarrierUpTriRe.get(Greek.FAIR_PRICE), resGreeksConstBarrierUp.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierUpTriRe.get(Greek.DELTA), resGreeksConstBarrierUp.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.DELTA)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierUpTriRe.get(Greek.GAMMA), resGreeksConstBarrierUp.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.GAMMA)), 0.1) * 0.1);
              assertEquals(resGreeksBarrierUpTriRe.get(Greek.THETA), resGreeksConstBarrierUp.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.THETA)), 0.1));
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
  public void hashCodeEqualsTest() {
    final AmericanSingleBarrierOptionFunctionProvider.BarrierTypes type = AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut");
    final OptionFunctionProvider1D ref = new AmericanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., type);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new AmericanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., type),
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
