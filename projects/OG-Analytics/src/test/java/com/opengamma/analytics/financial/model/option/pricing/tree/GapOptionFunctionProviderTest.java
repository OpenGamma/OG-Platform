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
public class GapOptionFunctionProviderTest {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 105.1, 114. };
  private static final double[] PAYOFF_STRIKES = new double[] {101., 112. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.017, 0.05 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.014 };

  /**
   * 
   */
  @Test
  public void priceLatticeTrinomialTest() {
    /*
     * Due to slow convergence of other lattice specifications, only TianLatticeSpecification is used here
     */
    final LatticeSpecification lattice = new TianLatticeSpecification();

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int nSteps = 1543;
            for (final double dividend : DIVIDENDS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
                final double exactDiv = price(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double resDiv = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                final double refDiv = Math.max(Math.abs(exactDiv), 1.) * 5.e-2;
                assertEquals(resDiv, exactDiv, refDiv);
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
    final LatticeSpecification lattice = new TianLatticeSpecification();
    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int nSteps = 1543;
            for (final double dividend : DIVIDENDS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
                final GreekResultCollection resDiv = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                final double priceDiv = price(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) * 5.e-2;
                assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                final double deltaDiv = delta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 0.1) * 1.e-1;
                assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                final double gammaDiv = gamma(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend);
                final double refGammaDiv = Math.max(Math.abs(gammaDiv), 0.1) * 1.e-1;
                assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                final double thetaDiv = theta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) * 1.e-1;
                assertEquals(resDiv.get(Greek.THETA), thetaDiv, refThetaDiv);
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
    /*
     * Due to slow convergence of other lattice specifications, only LeisenReimerLatticeSpecification is used here
     */
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
    //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 831;
              for (final double dividend : DIVIDENDS) {
                for (final double payoffStrike : PAYOFF_STRIKES) {
                  final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
                  final double exactDiv = price(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                  final double resDiv = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  final double refDiv = Math.max(Math.abs(exactDiv), 1.) * 1.e-6;
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
    /*
     * Due to slow convergence of other lattice specifications, only LeisenReimerLatticeSpecification is used here
     */
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
    //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {5., 10., 8. };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final int nSteps = 1631;
                final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactProp = price(resSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double appCash = price(modSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double resProp = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);
                final double refProp = Math.max(Math.abs(exactProp), 1.) * 1.e-2;
                assertEquals(resProp, exactProp, refProp);
                final double resCash = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                final double refCash = Math.max(Math.abs(appCash), 1.) * 1.e-1;
                assertEquals(resCash, appCash, refCash);

                final LatticeSpecification latticeTri = new TianLatticeSpecification();
                final int nStepsTri = 531;
                final OptionFunctionProvider1D functionTri = new GapOptionFunctionProvider(strike, TIME, nStepsTri, isCall, payoffStrike);
                final double resPropTrinomial = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, propDividend);
                final double resCashTrinomial = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, cashDividend);
                assertEquals(resPropTrinomial, exactProp, Math.max(exactProp, 1.) * 1.e-1);
                assertEquals(resCashTrinomial, appCash, Math.max(appCash, 1.) * 1.e-1);
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
    /*
     * Due to slow convergence of other lattice specifications, only LeisenReimerLatticeSpecification is used here
     */
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
    //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final int nSteps = 831;
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
                  final GreekResultCollection resDiv = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  final double priceDiv = price(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                  final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) * 1.e-6;
                  assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                  final double deltaDiv = delta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                  final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) * 1.e-3;
                  assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                  final double gammaDiv = gamma(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend);
                  final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) * 1.e-3;
                  assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                  final double thetaDiv = theta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                  final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) * 1.e-2;
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
    /*
     * Due to slow convergence of other lattice specifications, only LeisenReimerLatticeSpecification is used here
     */
    //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
    //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.03, 0.02 };
    final double[] cashDividends = new double[] {1., 4., 1. };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final int nSteps = 1631;
                final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactPriceProp = price(resSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double exactDeltaProp = delta(resSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double exactGammaProp = gamma(resSpot, strike, payoffStrike, TIME, vol, interest, interest);
                final double exactThetaProp = theta(resSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);

                final double appPriceCash = price(modSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double appDeltaCash = delta(modSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);
                final double appGammaCash = gamma(modSpot, strike, payoffStrike, TIME, vol, interest, interest);
                final double appThetaCash = theta(modSpot, strike, payoffStrike, TIME, vol, interest, interest, isCall);

                final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, TIME, nSteps, isCall, payoffStrike);
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

                final LatticeSpecification latticeTri = new TianLatticeSpecification();
                final int nStepsTri = 731;
                final OptionFunctionProvider1D functionTri = new GapOptionFunctionProvider(strike, TIME, nStepsTri, isCall, payoffStrike);
                final GreekResultCollection resPropTrinomial = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, propDividend);
                final GreekResultCollection resCashTrinomial = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, cashDividend);

                assertEquals(resPropTrinomial.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-1);
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

  /**
   * non-constant volatility and interest rate
   */
  @Test
  public void timeVaryingVolTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 501;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 311;
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
          for (final double payoffStrike : PAYOFF_STRIKES) {

            final OptionFunctionProvider1D function = new GapOptionFunctionProvider(strike, time, steps, isCall, payoffStrike);
            final double resPrice = _model.getPrice(function, SPOT, vol, rate, dividend);
            final GreekResultCollection resGreeks = _model.getGreeks(function, SPOT, vol, rate, dividend);

            final double resPriceConst = _model.getPrice(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
            assertEquals(resPrice, resPriceConst, Math.max(Math.abs(resPriceConst), 1.) * 1.e-1);
            assertEquals(resGreeks.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 1.) * 0.1);
            assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 1.) * 0.1);
            assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 1.) * 0.1);
            assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 1.));

            final OptionFunctionProvider1D functionTri = new GapOptionFunctionProvider(strike, time, stepsTri, isCall, payoffStrike);
            final double resPriceTrinomial = _modelTrinomial.getPrice(functionTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resPriceTrinomial, resPriceConst, Math.max(Math.abs(resPriceConst), 1.));
            final GreekResultCollection resGreeksTrinomial = _modelTrinomial.getGreeks(functionTri, SPOT, volTri, rateTri, dividendTri);
            assertEquals(resGreeksTrinomial.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 1.));
            assertEquals(resGreeksTrinomial.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 1.));
            assertEquals(resGreeksTrinomial.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 1.));
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
  public void getStrikePayoffTest() {
    final GapOptionFunctionProvider function = new GapOptionFunctionProvider(103., 1., 1003, true, 105.5);
    assertEquals(function.getStrikePayoff(), 105.5);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  void negativePayoffTest() {
    new GapOptionFunctionProvider(103., 1., 1003, true, -105.5);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider1D ref = new GapOptionFunctionProvider(103., 1., 1003, true, 105.5);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new GapOptionFunctionProvider(103., 1., 1003, true, 105.5),
        new GapOptionFunctionProvider(103., 1., 1003, true, 106), new EuropeanVanillaOptionFunctionProvider(103., 1., 1003, true), null };
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

  private double price(final double spot, final double strike, final double payoffStrike, final double time, final double vol, final double interest, final double cost, final boolean isCall) {
    final double d1 = (Math.log(spot / strike) + (cost + 0.5 * vol * vol) * time) / vol / Math.sqrt(time);
    final double d2 = d1 - vol * Math.sqrt(time);
    final double sign = isCall ? 1. : -1.;
    return sign * (spot * Math.exp((cost - interest) * time) * NORMAL.getCDF(sign * d1) - payoffStrike * Math.exp((-interest) * time) * NORMAL.getCDF(sign * d2));
  }

  private double delta(final double spot, final double strike, final double payoffStrike, final double time, final double vol, final double interest, final double cost, final boolean isCall) {
    final double d1 = (Math.log(spot / strike) + (cost + 0.5 * vol * vol) * time) / vol / Math.sqrt(time);
    final double sign = isCall ? 1. : -1.;
    return Math.exp((cost - interest) * time) * (sign * NORMAL.getCDF(sign * d1) + NORMAL.getPDF(d1) * (1. - payoffStrike / strike) / vol / Math.sqrt(time));
  }

  private double gamma(final double spot, final double strike, final double payoffStrike, final double time, final double vol, final double interest, final double cost) {
    final double d1 = (Math.log(spot / strike) + (cost + 0.5 * vol * vol) * time) / vol / Math.sqrt(time);
    return Math.exp((cost - interest) * time) * NORMAL.getPDF(d1) * (1. - (1. - payoffStrike / strike) * d1 / vol / Math.sqrt(time)) / spot / vol / Math.sqrt(time);
  }

  private double theta(final double spot, final double strike, final double payoffStrike, final double time, final double vol, final double interest, final double cost, final boolean isCall) {
    final double d1 = (Math.log(spot / strike) + (cost + 0.5 * vol * vol) * time) / vol / Math.sqrt(time);
    final double d2 = d1 - vol * Math.sqrt(time);
    final double sign = isCall ? 1. : -1.;

    final double firstTerm = sign *
        (spot * (cost - interest) * Math.exp((cost - interest) * time) * NORMAL.getCDF(sign * d1) + interest * payoffStrike * Math.exp(-interest * time) * NORMAL.getCDF(sign * d2));
    final double secondTerm = spot * Math.exp((cost - interest) * time) * NORMAL.getPDF(d1) *
        (0.5 * (1. - payoffStrike / strike) * (-Math.log(spot / strike) / vol / Math.pow(time, 1.5) + cost / vol / Math.sqrt(time)) + 0.25 * (1. + payoffStrike / strike) * vol / Math.sqrt(time));
    return -firstTerm - secondTerm;
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
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              for (final double payoffStrike : PAYOFF_STRIKES) {
                final double delta = delta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double gamma = gamma(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend);
                final double theta = theta(SPOT, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double upSpot = price(SPOT + eps, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double downSpot = price(SPOT - eps, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double upSpotDelta = delta(SPOT + eps, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double downSpotDelta = delta(SPOT - eps, strike, payoffStrike, TIME, vol, interest, interest - dividend, isCall);
                final double upTime = price(SPOT, strike, payoffStrike, TIME + eps, vol, interest, interest - dividend, isCall);
                final double downTime = price(SPOT, strike, payoffStrike, TIME - eps, vol, interest, interest - dividend, isCall);
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
