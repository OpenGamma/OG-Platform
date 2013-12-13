/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CappedPowerOptionFunctionProviderTest {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 10.;
  private static final double POWER = 2.;
  private static final double[] STRIKES = new double[] {97., 105., 105.1, 114. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.017, 0.05 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.014 };

  /**
   * 
   */
  @Test
  public void priceLatticeTrinomialTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 381;
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                  final double exactDiv = price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double resDiv = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  final double refDiv = Math.max(exactDiv, 1.) * 1.e-2;
                  assertEquals(resDiv, exactDiv, refDiv);
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
  public void greekTrinomialTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 391;
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                  final GreekResultCollection resDiv = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  final double priceDiv = price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) * 1.e-2;
                  assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                  final double deltaDiv = delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) * 1.e-2;
                  assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                  final double gammaDiv = gamma(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) * 1.e-1;
                  assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                  final double thetaDiv = theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) * 1.e-1;
                  assertEquals(resDiv.get(Greek.THETA), thetaDiv, refThetaDiv);
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
  public void priceLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };

    /**
     * Since d1, d2 in Black-Scholes formula are not relevant in the case of power option, Leisen-Reimer is poor approximation 
     */
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 369;
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                  final double exactDiv = price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double resDiv = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  final double refDiv = Math.max(Math.abs(exactDiv), 1.) * 1.e-2;
                  assertEquals(resDiv, exactDiv, refDiv);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * The dividend is cash or proportional to asset price
   */
  @Test
  public void priceDiscreteDividendTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {0.5, 1., 0.8 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 521;
                final int nStepsTri = 71;
                final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactProp = price(resSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double appCash = price(modSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double resProp = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);
                final double refProp = Math.max(Math.abs(exactProp), 1.) * 1.e-2;
                assertEquals(resProp, exactProp, refProp);
                final double resCash = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                final double refCash = Math.max(Math.abs(appCash), 1.) * 1.e-1;
                assertEquals(resCash, appCash, refCash);

                if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                    lattice instanceof TianLatticeSpecification) {
                  final OptionFunctionProvider1D functionTri = new CappedPowerOptionFunctionProvider(strike, TIME, nStepsTri, isCall, POWER, cap);
                  final double resPropTrinomial = _modelTrinomial.getPrice(lattice, functionTri, SPOT, vol, interest, propDividend);
                  final double resCashTrinomial = _modelTrinomial.getPrice(lattice, functionTri, SPOT, vol, interest, cashDividend);
                  assertEquals(resPropTrinomial, exactProp, Math.max(exactProp, 1.) * 1.e-1);
                  assertEquals(resCashTrinomial, appCash, Math.max(appCash, 1.) * 1.e-1);
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
  public void greekTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 429;
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                  final GreekResultCollection resDiv = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  final double priceDiv = price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) * 1.e-2;
                  assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                  final double deltaDiv = delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) * 1.e-2;
                  assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                  final double gammaDiv = gamma(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) * 1.e-1;
                  assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                  final double thetaDiv = theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                  final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) * 1.e-1;
                  assertEquals(resDiv.get(Greek.THETA), thetaDiv, refThetaDiv);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * The dividend is cash or proportional to asset price
   */
  @Test
  public void greeksDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new JarrowRuddLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    //    new CoxRossRubinsteinLatticeSpecification(),new TrigeorgisLatticeSpecification(), //Omitted due to slow convergence
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.02, 0.02 };
    final double[] cashDividends = new double[] {0.1, 0.4, 0.1 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
          for (final double cap : caps) {
            for (final double interest : INTERESTS) {
              for (final double vol : VOLS) {
                final int nSteps = 781;
                final int nStepsTri = 481;
                //              final int nSteps = 8637;
                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactPriceProp = price(resSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double exactDeltaProp = delta(resSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double exactGammaProp = gamma(resSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double exactThetaProp = theta(resSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);

                final double appPriceCash = price(modSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double appDeltaCash = delta(modSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double appGammaCash = gamma(modSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);
                final double appThetaCash = theta(modSpot, strike, TIME, vol, interest, interest, isCall, POWER, cap);

                final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, TIME, nSteps, isCall, POWER, cap);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
                final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);

                assertEquals(resProp.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-2);
                assertEquals(resProp.get(Greek.DELTA), exactDeltaProp, Math.max(1., Math.abs(exactDeltaProp)) * 1.e-1);
                assertEquals(resProp.get(Greek.GAMMA), exactGammaProp, Math.max(1., Math.abs(exactGammaProp)) * 1.e-1);
                assertEquals(resProp.get(Greek.THETA), exactThetaProp, Math.max(1., Math.abs(exactThetaProp)) * 1.e-1);

                assertEquals(resCash.get(Greek.FAIR_PRICE), appPriceCash, Math.max(1., Math.abs(appPriceCash)) * 1.e-1);
                assertEquals(resCash.get(Greek.DELTA), appDeltaCash, Math.max(1., Math.abs(appDeltaCash)) * 1.e-1);
                assertEquals(resCash.get(Greek.GAMMA), appGammaCash, Math.max(1., Math.abs(appGammaCash)) * 1.e-1);
                assertEquals(resCash.get(Greek.THETA), appThetaCash, Math.max(1., Math.abs(appThetaCash)));//theta is poorly approximated

                if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                    lattice instanceof TianLatticeSpecification) {
                  final OptionFunctionProvider1D functionTri = new CappedPowerOptionFunctionProvider(strike, TIME, nStepsTri, isCall, POWER, cap);
                  final GreekResultCollection resPropTrinomial = _modelTrinomial.getGreeks(lattice, functionTri, SPOT, vol, interest, propDividend);
                  final GreekResultCollection resCashTrinomial = _modelTrinomial.getGreeks(lattice, functionTri, SPOT, vol, interest, cashDividend);

                  assertEquals(resPropTrinomial.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-2);
                  assertEquals(resPropTrinomial.get(Greek.DELTA), exactDeltaProp, Math.max(1., Math.abs(exactDeltaProp)) * 1.e-1);
                  assertEquals(resPropTrinomial.get(Greek.GAMMA), exactGammaProp, Math.max(1., Math.abs(exactGammaProp)) * 1.e-1);
                  assertEquals(resPropTrinomial.get(Greek.THETA), exactThetaProp, Math.max(1., Math.abs(exactThetaProp)) * 1.e-1);

                  assertEquals(resCashTrinomial.get(Greek.FAIR_PRICE), appPriceCash, Math.max(1., Math.abs(appPriceCash)) * 1.e-1);
                  assertEquals(resCashTrinomial.get(Greek.DELTA), appDeltaCash, Math.max(1., Math.abs(appDeltaCash)) * 1.e-1);
                  assertEquals(resCashTrinomial.get(Greek.GAMMA), appGammaCash, Math.max(1., Math.abs(appGammaCash)) * 1.e-1);
                  assertEquals(resCashTrinomial.get(Greek.THETA), appThetaCash, Math.max(1., Math.abs(appThetaCash)));//theta is poorly approximated
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
    final int steps = 301;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 261;
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
        final double[] caps = new double[] {strike * 0.2, strike * 0.4 };
        for (final double cap : caps) {
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

            final OptionFunctionProvider1D function = new CappedPowerOptionFunctionProvider(strike, time, steps, isCall, POWER, cap);
            final double resPrice = _model.getPrice(function, SPOT, vol, rate, dividend);
            final GreekResultCollection resGreeks = _model.getGreeks(function, SPOT, vol, rate, dividend);

            final double resPriceConst = _model.getPrice(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
            assertEquals(resPrice, resPriceConst, Math.max(Math.abs(resPriceConst), 1.) * 1.e-2);
            assertEquals(resGreeks.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 1.) * 1.e-2);
            assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 1.) * 1.e-2);
            assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 1.) * 1.e-1);
            assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 1.));

            final OptionFunctionProvider1D functionTri = new CappedPowerOptionFunctionProvider(strike, time, stepsTri, isCall, POWER, cap);
            final double resPriceTrinomial = _modelTrinomial.getPrice(functionTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resPriceTrinomial, resPriceConst, Math.max(Math.abs(resPriceConst), 1.) * 1.e-1);
            final GreekResultCollection resGreeksTrinomial = _modelTrinomial.getGreeks(functionTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resGreeksTrinomial.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 1.) * 0.1);
            assertEquals(resGreeksTrinomial.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 1.) * 0.1);
            assertEquals(resGreeksTrinomial.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 1.) * 0.1);
            assertEquals(resGreeksTrinomial.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 1.));
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void getPowerTest() {
    final CappedPowerOptionFunctionProvider function = new CappedPowerOptionFunctionProvider(103., TIME, 1003, true, 4., 30.);
    assertEquals(function.getPower(), 4.);
  }

  /**
   * 
   */
  @Test
  public void getCapTest() {
    final CappedPowerOptionFunctionProvider function = new CappedPowerOptionFunctionProvider(103., TIME, 1003, true, 4., 30.);
    assertEquals(function.getCap(), 30.);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativePowerTest() {
    new CappedPowerOptionFunctionProvider(103., TIME, 1003, true, -12., 30.);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeCapTest() {
    new CappedPowerOptionFunctionProvider(103., TIME, 1003, true, 12., -30.);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooLargeCaprTest() {
    new CappedPowerOptionFunctionProvider(103., TIME, 1003, false, 3., 130.);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider1D ref = new CappedPowerOptionFunctionProvider(103., 1., 1003, true, 12., 120.);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new CappedPowerOptionFunctionProvider(103., 1., 1003, true, 12., 120.),
        new CappedPowerOptionFunctionProvider(103., 1., 1003, true, 12., 121.), new CappedPowerOptionFunctionProvider(103., 1., 1003, true, 11., 120.),
        new CappedPowerOptionFunctionProvider(103., 10., 1003, true, 12., 120.), new EuropeanVanillaOptionFunctionProvider(103., 1., 1003, true), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
  }

  private double price(final double spot, final double strike, final double time, final double vol, final double interest, final double cost, final boolean isCall, final double power, final double cap) {
    final double sign = isCall ? 1. : -1.;
    final double e1 = (Math.log(spot / Math.pow(strike, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e2 = e1 - power * vol * Math.sqrt(time);
    final double e3 = (Math.log(spot / Math.pow(strike + sign * cap, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e4 = e3 - power * vol * Math.sqrt(time);
    return sign *
        (Math.pow(spot, power) * Math.exp(((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost)) * time) * (NORMAL.getCDF(sign * e1) - NORMAL.getCDF(sign * e3)) - Math
            .exp((-interest) * time) * (strike * NORMAL.getCDF(sign * e2) - (strike + sign * cap) * NORMAL.getCDF(sign * e4)));
  }

  private double delta(final double spot, final double strike, final double time, final double vol, final double interest, final double cost, final boolean isCall, final double power, final double cap) {
    final double sign = isCall ? 1. : -1.;
    final double e1 = (Math.log(spot / Math.pow(strike, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e2 = e1 - power * vol * Math.sqrt(time);
    final double e3 = (Math.log(spot / Math.pow(strike + sign * cap, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e4 = e3 - power * vol * Math.sqrt(time);
    final double de = 1. / spot / vol / Math.sqrt(time);
    final double first = sign * power * Math.pow(spot, power - 1) * Math.exp(((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost)) * time) *
        (NORMAL.getCDF(sign * e1) - NORMAL.getCDF(sign * e3));
    final double second = de * Math.pow(spot, power) * Math.exp(((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost)) * time) * (NORMAL.getPDF(e1) - NORMAL.getPDF(e3)) -
        de * Math.exp((-interest) * time) * (strike * NORMAL.getPDF(e2) - (strike + sign * cap) * NORMAL.getPDF(e4));
    return first + second;
  }

  private double gamma(final double spot, final double strike, final double time, final double vol, final double interest, final double cost, final boolean isCall, final double power, final double cap) {
    final double sign = isCall ? 1. : -1.;
    final double e1 = (Math.log(spot / Math.pow(strike, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e2 = e1 - power * vol * Math.sqrt(time);
    final double e3 = (Math.log(spot / Math.pow(strike + sign * cap, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e4 = e3 - power * vol * Math.sqrt(time);
    final double de = 1. / spot / vol / Math.sqrt(time);
    final double dde = -1. / spot / spot / vol / Math.sqrt(time);
    final double factor1 = Math.exp(((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost)) * time);

    final double first = sign * power * (power - 1) * Math.pow(spot, power - 2) * factor1 * (NORMAL.getCDF(sign * e1) - NORMAL.getCDF(sign * e3)) +
        2. * power * Math.pow(spot, power - 1) * factor1 * de * (NORMAL.getPDF(e1) - NORMAL.getPDF(e3));
    final double second = de * de * Math.pow(spot, power) * factor1 * (NORMAL.getPDF(e1) * (-e1) - NORMAL.getPDF(e3) * (-e3)) -
        de * de * Math.exp((-interest) * time) * (strike * NORMAL.getPDF(e2) * (-e2) - (strike + sign * cap) * NORMAL.getPDF(e4) * (-e4));
    final double third = dde * Math.pow(spot, power) * factor1 * (NORMAL.getPDF(e1) - NORMAL.getPDF(e3)) -
        dde * Math.exp((-interest) * time) * (strike * NORMAL.getPDF(e2) - (strike + sign * cap) * NORMAL.getPDF(e4));
    return first + second + third;
  }

  private double theta(final double spot, final double strike, final double time, final double vol, final double interest, final double cost, final boolean isCall, final double power, final double cap) {
    final double sign = isCall ? 1. : -1.;
    final double e1 = (Math.log(spot / Math.pow(strike, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e2 = e1 - power * vol * Math.sqrt(time);
    final double e3 = (Math.log(spot / Math.pow(strike + sign * cap, 1. / power)) + (cost + (power - 0.5) * vol * vol) * time) / vol / Math.sqrt(time);
    final double e4 = e3 - power * vol * Math.sqrt(time);
    final double de1 = -0.5 * Math.log(spot / Math.pow(strike, 1. / power)) / vol / Math.pow(time, 1.5) + 0.5 * (cost + (power - 0.5) * vol * vol) / Math.sqrt(time) / vol;
    final double de2 = de1 - 0.5 * power * vol / Math.sqrt(time);
    final double de3 = -0.5 * Math.log(spot / Math.pow(strike + sign * cap, 1. / power)) / vol / Math.pow(time, 1.5) + 0.5 * (cost + (power - 0.5) * vol * vol) / Math.sqrt(time) / vol;
    final double de4 = de3 - 0.5 * power * vol / Math.sqrt(time);
    final double cst = ((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost));

    final double first = sign * (cst * Math.pow(spot, power) * Math.exp(cst * time) * (NORMAL.getCDF(sign * e1) - NORMAL.getCDF(sign * e3)) + interest * Math.exp((-interest) * time) *
        (strike * NORMAL.getCDF(sign * e2) - (strike + sign * cap) * NORMAL.getCDF(sign * e4)));
    final double second = Math.pow(spot, power) * Math.exp(((power - 1.) * (interest + 0.5 * power * vol * vol) - power * (interest - cost)) * time) *
        (NORMAL.getPDF(e1) * de1 - NORMAL.getPDF(e3) * de3) - Math.exp((-interest) * time) * (strike * NORMAL.getPDF(e2) * de2 - (strike + sign * cap) * NORMAL.getPDF(e4) * de4);
    return -first - second;
  }

  /**
   * test for analytic formula
   */
  @Test(enabled = false)
  public void functionTest() {
    final boolean[] tfSet = new boolean[] {true, false };
    final double eps = 1.e-6;
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        final double[] caps = new double[] {strike * .2, strike * .3 };
        for (final double cap : caps) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final double delta = delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double gamma = gamma(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double theta = theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double upSpot = price(SPOT + eps, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double downSpot = price(SPOT - eps, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double upSpotDelta = delta(SPOT + eps, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double downSpotDelta = delta(SPOT - eps, strike, TIME, vol, interest, interest - dividend, isCall, POWER, cap);
                final double upTime = price(SPOT, strike, TIME + eps, vol, interest, interest - dividend, isCall, POWER, cap);
                final double downTime = price(SPOT, strike, TIME - eps, vol, interest, interest - dividend, isCall, POWER, cap);
                assertEquals(delta, 0.5 * (upSpot - downSpot) / eps, eps);
                assertEquals(gamma, 0.5 * (upSpotDelta - downSpotDelta) / eps, eps);
                assertEquals(theta, -0.5 * (upTime - downTime) / eps, eps);
              }
            }
          }
        }
      }
    }
  }
}
