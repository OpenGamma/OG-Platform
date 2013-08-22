/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

public class BinomialTreeOptionPricingModelTest {

  private static final double EPS = 1.e-7;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {81., 97., 105., 105.1, 114., 138. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0., 0.001, 0.005, 0.01 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };

  private static final double[] DIVIDENDS = new double[] {0., 0.005, 0.02 };

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
    //    final double[] res = _model.getEuropeanGreeks(lattice, 120, 110, 1., 1., 1., 1001, true);
    final double price = _model.getEuropeanPrice(lattice, 120, 110, 1., 1., 1., 2000, true);
    //    System.out.println(new DoubleMatrix1D(res));
    //    System.out.println(price);
  }

  @Test(enabled = false)
  public void bTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new TianLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final int nLat = lattices.length;
    for (int i = 0; i < nLat; ++i) {
      final LatticeSpecification lattice = lattices[i];
      final double[] res = _model.getEuropeanGreeks(lattice, 120, 110, 1., 1., 1., 1001, true);
      //    final double price = _model.getEuropeanPrice(lattice, 120, 110, 1., 1., 1., 1001, true);
      System.out.println(new DoubleMatrix1D(res));
      //    System.out.println(price);
    }
  }

  @Test(enabled = false)
  public void largeDivTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new TianLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(),
        new LeisenReimerLatticeSpecification() };
    final double strike = SPOT - 1.;
    final double vol = 0.1;
    final double interest = 0.01;
    final double dividend = 0.15;
    final boolean isCall = true;
    final int nSteps = 2001;

    for (final LatticeSpecification lattice : lattices) {
      final double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
      final double resDiv = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);

      System.out.println(exactDiv + "\t" + resDiv);
    }
  }

  /**
   * 
   */
  @Test
  public void EuropeanPriceLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

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
              final int[] choicesSteps = new int[] {31, 115, 301 };
              for (final int nSteps : choicesSteps) {
                final double exact = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double res = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                final double ref = (lattice instanceof LeisenReimerLatticeSpecification) ? Math.max(exact, 1.) / nSteps / nSteps : Math.max(exact, 1.) / Math.sqrt(nSteps);
                assertEquals(res, exact, ref);
                for (final double dividend : DIVIDENDS) {
                  final double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double resDiv = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  final double refDiv = (lattice instanceof LeisenReimerLatticeSpecification) ? Math.max(exactDiv, 1.) / nSteps / nSteps : Math.max(exactDiv, 1.) / Math.sqrt(nSteps);
                  //                  System.out.println(SPOT + "\t" + strike + "\t" + TIME + "\t" + vol + "\t" + interest + "\t" + dividend + "\t" + nSteps + "\t" + isCall);
                  assertEquals(resDiv, exactDiv, refDiv);
                }
              }
            }
          }
        }
      }
    }
  }

  //  @Test
  //  public void EuropeanPriceLatticeNewMethodTest() {
  //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
  //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
  //
  //    final boolean[] tfSet = new boolean[] {true, false };
  //    for (final LatticeSpecification lattice : lattices) {
  //      for (final boolean isCall : tfSet) {
  //        for (final double strike : STRIKES) {
  //          for (final double interest : INTERESTS) {
  //            for (final double vol : VOLS) {
  //              for (final double dividend : DIVIDENDS) {
  //                final ZonedDateTime date = DateUtils.getUTCDate(2010, 7, 1);
  //                final int secondsInAYear = (int) (365.25 * 24 * 60 * 60);
  //                final EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(strike, new Expiry(date.plusSeconds((int) (secondsInAYear * TIME))), isCall);
  //                final YieldAndDiscountCurve yCurve = YieldCurve.from(ConstantDoublesCurve.from(interest));
  //                final VolatilitySurface vSurface = new VolatilitySurface(ConstantDoublesSurface.from(vol));
  //                final StandardOptionDataBundle data = new StandardOptionDataBundle(yCurve, interest - dividend, vSurface, SPOT, date);
  //                final int[] choicesSteps = new int[] {11, 105 };
  //                for (final int nSteps : choicesSteps) {
  //                  final double resNew = _model.getPrice(lattice, definition, data, nSteps);
  //                  final double resOld = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
  //                  //                  System.out.println(resNew + "\t" + resOld);
  //                  assertEquals(resNew, resOld, 1.e-14);
  //                }
  //              }
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }

  /**
   * European option price with discrete dividend
   * The dividend is cash or proportional to asset price
   */
  @Test
  public void EuropeanPriceDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {5., 10., 8. };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {31, 115, 301 };
              for (final int nSteps : choicesSteps) {
                final double df = Math.exp(-interest * TIME);
                final double resSpot = SPOT * Math.exp(interest * TIME) * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactProp = df * BlackFormulaRepository.price(resSpot, strike, TIME, vol, isCall);
                final double appCash = BlackScholesFormulaRepository.price(modSpot, strike, TIME, vol, interest, interest, isCall);
                final double resProp = _model.getEuropeanPriceProportionalDividends(lattice, SPOT, strike, TIME, vol, interest, dividendTimes, propDividends, nSteps, isCall);
                final double refProp = Math.max(exactProp, 1.) / Math.sqrt(nSteps);
                assertEquals(resProp, exactProp, refProp);
                final double resCash = _model.getEuropeanPriceCashDividends(lattice, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, nSteps, isCall);
                final double refCash = Math.max(appCash, 1.) / Math.sqrt(nSteps);
                assertEquals(resCash, appCash, refCash);
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void EuropeanGreeksDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {5., 10., 8. };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 401;
              final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
              final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                  Math.exp(-interest * dividendTimes[2]);
              final double exactPriceProp = BlackScholesFormulaRepository.price(resSpot, strike, TIME, vol, interest, interest, isCall);
              final double exactDeltaProp = BlackScholesFormulaRepository.delta(resSpot, strike, TIME, vol, interest, interest, isCall);
              final double exactGammaProp = BlackScholesFormulaRepository.gamma(resSpot, strike, TIME, vol, interest, interest);
              final double exactThetaProp = BlackScholesFormulaRepository.theta(resSpot, strike, TIME, vol, interest, interest, isCall);

              final double appPriceCash = BlackScholesFormulaRepository.price(modSpot, strike, TIME, vol, interest, interest, isCall);
              final double appDeltaCash = BlackScholesFormulaRepository.delta(modSpot, strike, TIME, vol, interest, interest, isCall);
              final double appGammaCash = BlackScholesFormulaRepository.gamma(modSpot, strike, TIME, vol, interest, interest);
              final double appThetaCash = BlackScholesFormulaRepository.theta(modSpot, strike, TIME, vol, interest, interest, isCall);

              final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, nSteps, isCall);
              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, propDividend);
              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, cashDividend);

              assertEquals(resProp.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-2);
              assertEquals(resProp.get(Greek.DELTA), exactDeltaProp, Math.max(1., Math.abs(exactDeltaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.GAMMA), exactGammaProp, Math.max(1., Math.abs(exactGammaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.THETA), exactThetaProp, Math.max(1., Math.abs(exactThetaProp)) * 1.e-1);

              assertEquals(resCash.get(Greek.FAIR_PRICE), appPriceCash, Math.max(1., Math.abs(appPriceCash)) * 1.e-2);
              assertEquals(resCash.get(Greek.DELTA), appDeltaCash, Math.max(1., Math.abs(appDeltaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.GAMMA), appGammaCash, Math.max(1., Math.abs(appGammaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.THETA), appThetaCash, Math.max(1., Math.abs(appThetaCash)));
            }
          }
        }
      }
    }
  }

  /**
   * Compare tree with time-varying volatility to BS formula with root-mean-square volatility
   */
  //  @Test
  //  public void timeVaryingVolEuropeanTest() {
  //    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
  //    final double[] time_set = new double[] {0.5, 1., 2. };
  //    final int steps = 301;
  //
  //    final double[] vol = new double[steps];
  //    final double[] rate = new double[steps];
  //    final double constA = 0.01;
  //    final double constB = 0.001;
  //    final double constC = 0.1;
  //    final double constD = 0.05;
  //
  //    final boolean[] tfSet = new boolean[] {true, false };
  //    for (final boolean isCall : tfSet) {
  //      for (final double strike : STRIKES) {
  //        for (final double time : time_set) {
  //          for (int i = 0; i < steps; ++i) {
  //            rate[i] = constA + constB * i * time / steps;
  //            vol[i] = constC + constD * Math.sin(i * time / steps);
  //          }
  //          final double rateRef = constA + 0.5 * constB * time;
  //          final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));
  //
  //          final double res1 = _model.getEuropeanPrice(lattice1, SPOT, strike, time, vol, rate, steps, isCall);
  //          final double res3 = BlackScholesFormulaRepository.price(SPOT, strike, time, volRef, rateRef, rateRef, isCall);
  //          //          System.out.println(res1);
  //          //          System.out.println(res3);
  //          assertEquals(res1, res3, Math.max(res3, 1.) * Math.pow(0.1, 1. / time));
  //        }
  //      }
  //    }
  //  }

  @Test
  public void barrierEuropeanTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    /*
     *  As expected, large vol and spot \sim barrier leads to poor accuracy since the effect of discreteness becomes large. 
     */
    final double[] vols = new double[] {0.02, 0.09 };

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
                    final int[] choicesSteps = new int[] {511 };
                    for (final int nSteps : choicesSteps) {
                      final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                          EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      if (type == "DownAndOut") {
                        if (strike > barrier) {
                          double exact = isCall ? getA(SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) - getC(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., 1.) : getA(
                              SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) -
                              getB(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) +
                              getC(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.) -
                              getD(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., 1.);
                          exact = exact < 0. ? 0. : exact;
                          exact = SPOT <= barrier ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                          //                        System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        } else {
                          double exact = isCall ? getB(SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) - getD(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., 1.) : 0.;
                          exact = exact < 0. ? 0. : exact;
                          exact = SPOT <= barrier ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                          //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        }
                      } else {
                        if (strike < barrier) {
                          double exact = !isCall ? getA(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) - getC(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., -1.) : getA(
                              SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) -
                              getB(SPOT, strike, TIME, vol, interest, dividend, barrier, 1.) +
                              getC(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., -1.) -
                              getD(SPOT, strike, TIME, vol, interest, dividend, barrier, 1., -1.);
                          exact = exact < 0. ? 0. : exact;
                          exact = SPOT >= barrier ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                          //                        System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        } else {
                          double exact = !isCall ? getB(SPOT, strike, TIME, vol, interest, dividend, barrier, -1.) - getD(SPOT, strike, TIME, vol, interest, dividend, barrier, -1., -1.) : 0.;
                          exact = exact < 0. ? 0. : exact;
                          exact = SPOT >= barrier ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                          //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
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

  private double getA(final double spot, final double strike, final double time, final double vol, final double interest, final double dividend, final double barrier, final double phi) {
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

  @Test
  public void EuropeanBarrierPriceDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double eps = 1.e-1;

    final double[] propDividends = new double[] {0.002, 0.001, 0.002 };
    final double[] cashDividends = new double[] {.2, 1.1, .5 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final double[] vols = new double[] {0.02, 0.09 };
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
                  final int[] choicesSteps = new int[] {1181 };
                  for (final int nSteps : choicesSteps) {
                    final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                        EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));

                    final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                    final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                        Math.exp(-interest * dividendTimes[2]);

                    final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                    final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                    final double resMod = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, cashDividend);
                    final double resRes = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, propDividend);

                    if (type == "DownAndOut") {
                      if (strike > barrier) {
                        double exactMod = isCall ? getA(modSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getC(modSpot, strike, TIME, vol, interest, 0., barrier, 1., 1.) : getA(
                            modSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getB(modSpot, strike, TIME, vol, interest, 0., barrier, -1.) +
                            getC(modSpot, strike, TIME, vol, interest, 0., barrier, -1., 1.) - getD(modSpot, strike, TIME, vol, interest, 0., barrier, -1., 1.);
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        //                        if (exactMod >= 0.) {
                        exactMod = SPOT <= barrier ? 0. : exactMod;
                        //                        System.out.println(SPOT + "\t" + strike + "\t" + barrier + "\t" + TIME + "\t" + vol + "\t" + interest + "\t" + isCall);
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);
                        //                        }

                        double exactRes = isCall ? getA(resSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getC(resSpot, strike, TIME, vol, interest, 0., barrier, 1., 1.) : getA(
                            resSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getB(resSpot, strike, TIME, vol, interest, 0., barrier, -1.) +
                            getC(resSpot, strike, TIME, vol, interest, 0., barrier, -1., 1.) - getD(resSpot, strike, TIME, vol, interest, 0., barrier, -1., 1.);
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = SPOT <= barrier ? 0. : exactRes;
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * 1. * eps);
                      } else {
                        double exactMod = isCall ? getB(modSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getD(modSpot, strike, TIME, vol, interest, 0., barrier, 1., 1.) : 0.;
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = SPOT <= barrier ? 0. : exactMod;
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = isCall ? getB(resSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getD(resSpot, strike, TIME, vol, interest, 0., barrier, 1., 1.) : 0.;
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = SPOT <= barrier ? 0. : exactRes;
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                      }
                    } else {
                      if (strike < barrier) {
                        double exactMod = !isCall ? getA(modSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getC(modSpot, strike, TIME, vol, interest, 0., barrier, -1., -1.) : getA(
                            modSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getB(modSpot, strike, TIME, vol, interest, 0., barrier, 1.) +
                            getC(modSpot, strike, TIME, vol, interest, 0., barrier, 1., -1.) - getD(modSpot, strike, TIME, vol, interest, 0., barrier, 1., -1.);
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = SPOT >= barrier ? 0. : exactMod;
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = !isCall ? getA(resSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getC(resSpot, strike, TIME, vol, interest, 0., barrier, -1., -1.) : getA(
                            resSpot, strike, TIME, vol, interest, 0., barrier, 1.) - getB(resSpot, strike, TIME, vol, interest, 0., barrier, 1.) +
                            getC(resSpot, strike, TIME, vol, interest, 0., barrier, 1., -1.) - getD(resSpot, strike, TIME, vol, interest, 0., barrier, 1., -1.);
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = SPOT >= barrier ? 0. : exactRes;
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                      } else {
                        double exactMod = !isCall ? getB(modSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getD(modSpot, strike, TIME, vol, interest, 0., barrier, -1., -1.) : 0.;
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = SPOT >= barrier ? 0. : exactMod;
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = !isCall ? getB(resSpot, strike, TIME, vol, interest, 0., barrier, -1.) - getD(resSpot, strike, TIME, vol, interest, 0., barrier, -1., -1.) : 0.;
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = SPOT >= barrier ? 0. : exactRes;
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
  }

  //  @Test
  //  public void EuropeanGreeksDiscreteDividendLatticeTest() {
  //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
  //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
  //
  //    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
  //    final double[] cashDividends = new double[] {5., 10., 8. };
  //    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };
  //
  //    final boolean[] tfSet = new boolean[] {true, false };
  //    for (final LatticeSpecification lattice : lattices) {
  //      for (final boolean isCall : tfSet) {
  //        for (final double strike : STRIKES) {
  //          for (final double interest : INTERESTS) {
  //            for (final double vol : VOLS) {
  //              final int nSteps = 401;
  //              final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
  //              final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
  //                  Math.exp(-interest * dividendTimes[2]);
  //              final double exactPriceProp = BlackScholesFormulaRepository.price(resSpot, strike, TIME, vol, interest, interest, isCall);
  //              final double exactDeltaProp = BlackScholesFormulaRepository.delta(resSpot, strike, TIME, vol, interest, interest, isCall);
  //              final double exactGammaProp = BlackScholesFormulaRepository.gamma(resSpot, strike, TIME, vol, interest, interest);
  //              final double exactThetaProp = BlackScholesFormulaRepository.theta(resSpot, strike, TIME, vol, interest, interest, isCall);
  //
  //              final double appPriceCash = BlackScholesFormulaRepository.price(modSpot, strike, TIME, vol, interest, interest, isCall);
  //              final double appDeltaCash = BlackScholesFormulaRepository.delta(modSpot, strike, TIME, vol, interest, interest, isCall);
  //              final double appGammaCash = BlackScholesFormulaRepository.gamma(modSpot, strike, TIME, vol, interest, interest);
  //              final double appThetaCash = BlackScholesFormulaRepository.theta(modSpot, strike, TIME, vol, interest, interest, isCall);
  //
  //              final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
  //                  AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
  //
  //              final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, nSteps, isCall);
  //              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
  //              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
  //              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, propDividend);
  //              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, cashDividend);
  //
  //              assertEquals(resProp.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-2);
  //              assertEquals(resProp.get(Greek.DELTA), exactDeltaProp, Math.max(1., Math.abs(exactDeltaProp)) * 1.e-1);
  //              assertEquals(resProp.get(Greek.GAMMA), exactGammaProp, Math.max(1., Math.abs(exactGammaProp)) * 1.e-1);
  //              assertEquals(resProp.get(Greek.THETA), exactThetaProp, Math.max(1., Math.abs(exactThetaProp)) * 1.e-1);
  //
  //              assertEquals(resCash.get(Greek.FAIR_PRICE), appPriceCash, Math.max(1., Math.abs(appPriceCash)) * 1.e-2);
  //              assertEquals(resCash.get(Greek.DELTA), appDeltaCash, Math.max(1., Math.abs(appDeltaCash)) * 1.e-1);
  //              assertEquals(resCash.get(Greek.GAMMA), appGammaCash, Math.max(1., Math.abs(appGammaCash)) * 1.e-1);
  //              assertEquals(resCash.get(Greek.THETA), appThetaCash, Math.max(1., Math.abs(appThetaCash)));
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }

  /*
   * Check the case when analytic approximation is available
   */
  @Test
  public void barrierAmericanTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] vols = new double[] {0.02, 0.09 };
    final double time = 0.1;

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
                    final int[] choicesSteps = new int[] {511 };
                    for (final int nSteps : choicesSteps) {
                      final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                          AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      if (type == "DownAndOut") {
                        if (strike > barrier) {
                          double exact = isCall ? getA(SPOT, strike, time, vol, interest, dividend, barrier, 1.) - getC(SPOT, strike, time, vol, interest, dividend, barrier, 1., 1.) : getA(
                              SPOT, strike, time, vol, interest, dividend, barrier, -1.) -
                              getB(SPOT, strike, time, vol, interest, dividend, barrier, -1.) +
                              getC(SPOT, strike, time, vol, interest, dividend, barrier, -1., 1.) -
                              getD(SPOT, strike, time, vol, interest, dividend, barrier, -1., 1.);
                          exact = exact < 0. ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, time, vol, interest, dividend);
                          //                        System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        } else {
                          double exact = isCall ? getB(SPOT, strike, time, vol, interest, dividend, barrier, 1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, 1., 1.) : 0.;
                          exact = exact < 0. ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, time, vol, interest, dividend);
                          //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        }
                      } else {
                        if (strike < barrier) {
                          double exact = !isCall ? getA(SPOT, strike, time, vol, interest, dividend, barrier, -1.) - getC(SPOT, strike, time, vol, interest, dividend, barrier, -1., -1.) : getA(
                              SPOT, strike, time, vol, interest, dividend, barrier, 1.) -
                              getB(SPOT, strike, time, vol, interest, dividend, barrier, 1.) +
                              getC(SPOT, strike, time, vol, interest, dividend, barrier, 1., -1.) -
                              getD(SPOT, strike, time, vol, interest, dividend, barrier, 1., -1.);
                          exact = exact < 0. ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, time, vol, interest, dividend);
                          //                        System.out.println(barrier + "\t" + strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
                        } else {
                          double exact = !isCall ? getB(SPOT, strike, time, vol, interest, dividend, barrier, -1.) - getD(SPOT, strike, time, vol, interest, dividend, barrier, -1., -1.) : 0.;
                          exact = exact < 0. ? 0. : exact;
                          final double res = _model.getPrice(lattice, function, SPOT, time, vol, interest, dividend);
                          //                        System.out.println(strike + "\t" + vol + "\t" + interest + "\t" + exact + "\t" + res);
                          assertEquals(res, exact, Math.max(exact, 1.) * 1.e-1);
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
     * sample data
     */
    final double spot = 100.;
    final double strike = 100.;
    final double time1 = 1.;
    final double sig = 0.2;
    final double rate = 0.06;
    final double barrier = 95.;
    final int steps = 3;

    final BinomialTreeOptionPricingModel modelB = new BinomialTreeOptionPricingModel(barrier, "DownAndOut");
    for (final LatticeSpecification lattice : lattices) {
      final double res = modelB.getAmericanPrice(lattice, spot, strike, time1, sig, rate, steps, true);
      assertEquals(res, 9.9958, 9958 * 1.e-1);
    }
  }

  @Test
  public void barrierAmericanDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final double eps = 1.e-1;

    final double[] propDividends = new double[] {0.002, 0.001, 0.002 };
    final double[] cashDividends = new double[] {.2, 1.1, .5 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final double[] vols = new double[] {0.02, 0.09 };
    final double time = 0.1;

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
                  final int[] choicesSteps = new int[] {311 };
                  for (final int nSteps : choicesSteps) {
                    final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                        AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                    final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                        Math.exp(-interest * dividendTimes[2]);
                    final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                    final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);

                    if (type == "DownAndOut") {
                      if (strike > barrier) {
                        double exactMod = isCall ? getA(modSpot, strike, time, vol, interest, 0., barrier, 1.) - getC(modSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : getA(
                            modSpot, strike, time, vol, interest, 0., barrier, -1.) - getB(modSpot, strike, time, vol, interest, 0., barrier, -1.) +
                            getC(modSpot, strike, time, vol, interest, 0., barrier, -1., 1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, -1., 1.);
                        //                                                exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = barrier >= SPOT ? 0. : exactMod;
                        final double resMod = _model.getPrice(lattice, function, SPOT, time, vol, interest, cashDividend);
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = isCall ? getA(resSpot, strike, time, vol, interest, 0., barrier, 1.) - getC(resSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : getA(
                            resSpot, strike, time, vol, interest, 0., barrier, -1.) - getB(resSpot, strike, time, vol, interest, 0., barrier, -1.) +
                            getC(resSpot, strike, time, vol, interest, 0., barrier, -1., 1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, -1., 1.);
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = barrier >= SPOT ? 0. : exactRes;
                        final double resRes = _model.getPrice(lattice, function, SPOT, time, vol, interest, propDividend);
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                      } else {
                        double exactMod = isCall ? getB(modSpot, strike, time, vol, interest, 0., barrier, 1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : 0.;
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = barrier >= SPOT ? 0. : exactMod;
                        final double resMod = _model.getPrice(lattice, function, SPOT, time, vol, interest, cashDividend);
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = isCall ? getB(resSpot, strike, time, vol, interest, 0., barrier, 1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, 1., 1.) : 0.;
                        //                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = barrier >= SPOT ? 0. : exactRes;
                        final double resRes = _model.getPrice(lattice, function, SPOT, time, vol, interest, propDividend);
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                      }
                    } else {
                      if (strike < barrier) {
                        double exactMod = !isCall ? getA(modSpot, strike, time, vol, interest, 0., barrier, -1.) - getC(modSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : getA(
                            modSpot, strike, time, vol, interest, 0., barrier, 1.) - getB(modSpot, strike, time, vol, interest, 0., barrier, 1.) +
                            getC(modSpot, strike, time, vol, interest, 0., barrier, 1., -1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, 1., -1.);
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = barrier <= SPOT ? 0. : exactMod;
                        final double resMod = _model.getPrice(lattice, function, SPOT, time, vol, interest, cashDividend);
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = !isCall ? getA(resSpot, strike, time, vol, interest, 0., barrier, -1.) - getC(resSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : getA(
                            resSpot, strike, time, vol, interest, 0., barrier, 1.) - getB(resSpot, strike, time, vol, interest, 0., barrier, 1.) +
                            getC(resSpot, strike, time, vol, interest, 0., barrier, 1., -1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, 1., -1.);
                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = barrier <= SPOT ? 0. : exactRes;
                        final double resRes = _model.getPrice(lattice, function, SPOT, time, vol, interest, propDividend);
                        assertEquals(resRes, exactRes, Math.max(exactRes, 1.) * eps);
                      } else {
                        double exactMod = !isCall ? getB(modSpot, strike, time, vol, interest, 0., barrier, -1.) - getD(modSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : 0.;
                        //                        exactMod = exactMod < 0. ? 0. : exactMod;
                        exactMod = barrier <= SPOT ? 0. : exactMod;
                        final double resMod = _model.getPrice(lattice, function, SPOT, time, vol, interest, cashDividend);
                        assertEquals(resMod, exactMod, Math.max(exactMod, 1.) * eps);

                        double exactRes = !isCall ? getB(resSpot, strike, time, vol, interest, 0., barrier, -1.) - getD(resSpot, strike, time, vol, interest, 0., barrier, -1., -1.) : 0.;
                        exactRes = exactRes < 0. ? 0. : exactRes;
                        exactRes = barrier <= SPOT ? 0. : exactRes;
                        final double res = _model.getPrice(lattice, function, SPOT, time, vol, interest, propDividend);
                        assertEquals(res, exactRes, Math.max(exactRes, 1.) * eps);
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

  @Test
  public void spreadEuropeanPriceTest() {
    final double[] spotSet2 = new double[] {SPOT * 0.9, SPOT * 1.1 };
    final double[] sigmaSet2 = new double[] {0.05, 0.12 };
    final double[] rhoSet = new double[] {0.1, 0.6, 0.9 };

    final double strike = 0.;
    final double div2 = 0.03;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          final int[] choicesSteps = new int[] {29, 113 };
          for (final int nSteps : choicesSteps) {
            for (final double spot2 : spotSet2) {
              for (final double sigma2 : sigmaSet2) {
                for (final double rho : rhoSet) {
                  for (final double dividend : DIVIDENDS) {
                    if (interest > 0. && dividend > 0.) {// this must be a sufficient condition for all probabilities > 0.
                      double exactDiv = BlackScholesFormulaRepository.price(SPOT, spot2, TIME, Math.sqrt(vol * vol + sigma2 * sigma2 - 2. * rho * vol * sigma2), div2, div2 - dividend, isCall);
                      final double resDiv = _model.getEuropeanSpreadPrice(SPOT, spot2, strike, TIME, vol, sigma2, rho, interest, dividend, div2, nSteps, isCall);
                      final double refDiv = Math.max(exactDiv, 1.) / Math.sqrt(nSteps);
                      assertEquals(resDiv, exactDiv, refDiv);
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

  @Test
  public void spreadAmericanPriceTest() {
    final double resDiv = _model.getAmericanSpreadPrice(100, 100., 1., 1., 0.2, 0.3, 0.50, 0.06, 0.03, 0.04, 3, true);
    assertEquals(resDiv, 1.e-3, 10.04479);
  }

  @Test
  public void EuropeanGreekLatticesTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    //    
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
              final int[] choicesSteps = new int[] {31, 112, 301 };
              for (final int nSteps : choicesSteps) {
                final double delta = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double gamma = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest);
                final double theta = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest, isCall);
                final double[] res = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                final double refDelta = Math.max(Math.abs(delta), 1.) / Math.sqrt(nSteps);
                final double refGamma = Math.max(Math.abs(gamma), 1.) / Math.sqrt(nSteps);
                final double refTheta = Math.max(Math.abs(theta), 1.) / Math.sqrt(nSteps);
                assertEquals(res[1], delta, refDelta);
                assertEquals(res[2], gamma, refGamma);
                assertEquals(res[3], theta, refTheta * 10.);
                for (final double dividend : DIVIDENDS) {
                  final double[] resDiv = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  final double deltaDiv = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) / Math.sqrt(nSteps);
                  assertEquals(resDiv[1], deltaDiv, refDeltaDiv);
                  final double gammaDiv = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                  final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) / Math.sqrt(nSteps);
                  assertEquals(resDiv[2], gammaDiv, refGammaDiv);
                  final double thetaDiv = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) / Math.sqrt(nSteps);
                  //                  System.out.println(SPOT + "\t" + strike + "\t" + TIME + "\t" + vol + "\t" + interest + "\t" + dividend + "\t" + nSteps + "\t" + isCall);
                  assertEquals(resDiv[3], thetaDiv, refThetaDiv);
                }
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
    //    final double timeUp = TIME * (1. + EPS);
    //    final double timeDw = TIME * (1. - EPS);
    //    final double spotUp = SPOT * (1. + EPS);
    //    final double spotDw = SPOT * (1. - EPS);
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int[] choicesSteps = new int[] {31, 109, 301 };
            for (final int nSteps : choicesSteps) {
              //              final double priceSpotUp = _model.getEuropeanPrice(lattice, spotUp, strike, TIME, vol, interest, nSteps, isCall);
              //              final double priceSpotDw = _model.getEuropeanPrice(lattice, spotDw, strike, TIME, vol, interest, nSteps, isCall);
              //              final double priceTimeUp = _model.getEuropeanPrice(lattice, SPOT, strike, timeUp, vol, interest, nSteps, isCall);
              //              final double priceTimeDw = _model.getEuropeanPrice(lattice, SPOT, strike, timeDw, vol, interest, nSteps, isCall);
              //              final double deltaFinite = 0.5 * (priceSpotUp - priceSpotDw) / SPOT / EPS;
              //              final double thetaFinite = 0.5 * (priceTimeUp - priceTimeDw) / TIME / EPS;
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
              //              assertEquals(res[1], deltaFinite, refDelta * 10.);
              //              assertEquals(res[2], gamma, refGamma);
              //              assertEquals(res[3], -thetaFinite, refTheta);
              for (final double dividend : DIVIDENDS) {
                final double[] resDiv = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                final double deltaDiv = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) / nSteps;
                assertEquals(resDiv[1], deltaDiv, refDeltaDiv);
                final double gammaDiv = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) / nSteps;
                assertEquals(resDiv[2], gammaDiv, refGammaDiv);
                final double thetaDiv = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) / nSteps;
                //                System.out.println(SPOT + "\t" + strike + "\t" + TIME + "\t" + vol + "\t" + interest + "\t" + dividend + "\t" + nSteps + "\t" + isCall);
                assertEquals(resDiv[3], thetaDiv, refThetaDiv * 10.);
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void americanPutLatticesTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int steps = 301;

    /*
     * Reference values computed by Leisen & Reimer with n=1517
     */
    final double[][][] expected = new double[][][] { { {0.058556821282147, 1.423987043411806, 27.594175137559205 }, {2.567397920364287, 6.631469777788842, 38.470266225289116 }, {
        6.997116228834605, 11.187942858140278, 44.283278841420419 }, {7.067960593857507, 11.252770980174546, 44.357339168435061 }, {14.515506902403871, 17.702451797170493, 51.075917865430618 }, {
        38.923092465651131, 39.592796431785167, 70.271437840386440 } }, { {0.017094649958404, 0.920709822120827, 25.474240053335222 }, {1.301458360751146, 4.849447693540402, 35.658978986087433 }, {
        4.290461746344483, 8.569678257804563, 41.117180225195625 }, {4.342689885004511, 8.623851224674286, 41.186776086129939 }, {10.340522143008346, 14.144011128134144, 47.505616692417014 }, {
        33.014540019698508, 34.041591510167400, 65.606689099801713 } }, { {0.015039762225174, 0.881641734429296, 25.289896569285318 }, {1.214752608311750, 4.706363895479459, 35.418274680478810 }, {
        4.094588917958109, 8.361058070122976, 40.848245125395458 }, {4.145461254087033, 8.414427418702433, 40.917488765417581 }, {10.064152039859630, 13.869714139477221, 47.205092138702085 }, {
        32.999999999999986, 33.715871341375923, 65.223659867497304 } }, { {0.008949793225201, 0.742484278065933, 24.596867328889161 }, {0.926795362260926, 4.195273759411491, 34.520923345086018 }, {
        3.452142183825225, 7.628512315996570, 39.850075948866980 }, {3.499089828086897, 7.679299041128621, 39.918068529919609 }, {9.351414622160069, 12.948354591146975, 46.095399019293197 }, {
        33.000000000000014, 33.065144904304397, 63.829711734824144 } }, { {0.004575370004527, 0.598492302803965, 23.787299679796622 }, {0.659819531124806, 3.648809197956719, 33.479804561431592 }, {
        2.841041595429597, 6.853189962076901, 38.696363157646154 }, {2.884758217377101, 6.901476841705181, 38.762965967889727 }, {9.007929443878707, 12.024232122541374, 44.818592298574579 }, {
        32.999999999999915, 32.999999999999872, 62.247311829954604 } } };

    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            final double priceP = _model.getAmericanPrice(lattice, SPOT, STRIKES[k], TIME, VOLS[l], INTERESTS[j], steps, false);
            if (lattice instanceof LeisenReimerLatticeSpecification) {
              assertEquals(priceP, expected[j][k][l], Math.max(expected[j][k][l], 1.) / steps);
            } else {
              /*
               * As the price by LogEqualProbabiliesLatticeSpecification tends to converge to the "true" value from above, 
               * this lattice specification must be tested separately. 
               */
              //              if (lattice instanceof LogEqualProbabiliesLatticeSpecification) {
              //                final double ref = expected[j][k][l] * (steps + 1.) / (steps - 1.);
              //                assertEquals(priceP, ref, Math.max(ref, 1.) / Math.sqrt(steps));
              //              } else {
              assertEquals(priceP, expected[j][k][l], Math.max(expected[j][k][l], 1.) / Math.sqrt(steps));
              //              }
            }
          }
        }
      }
    }
  }

  /*
   * Just consistency is checked
   */
  @Test
  public void americanPriceCashMultipleDividendsTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] cashDividendsMat = new double[][] { {0.5, 0.5, 0.5 }, {0.1, 0.3, 0.2 } };
    final int divDim = cashDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (int j = 0; j < divDim; ++j) {
                final double[] dividendTimes = dividendTimesMat[j];
                final double[] cashDividends = cashDividendsMat[j];

                double priceDiv = _model.getAmericanPriceCashDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, steps, isCall);
                //                System.out.println(priceDiv);
                double modSpot = SPOT;
                final int divTimes = cashDividends.length;
                for (int i = 0; i < divTimes; ++i) {
                  modSpot -= cashDividends[i] * Math.exp(-interest * dividendTimes[i]);
                }
                double priceMod = _model.getAmericanPrice(model, modSpot, strike, TIME, vol, interest, steps, isCall);
                //                System.out.println(priceMod);
                double price = _model.getAmericanPrice(model, SPOT, strike, TIME, vol, interest, steps, isCall);
                //                System.out.println(price);

                final double ref = Math.abs(priceDiv - priceMod) > Math.abs(priceDiv - price) ? price : priceMod;
                if (interest > 0.) {
                  //                  System.out.println(SPOT + "\t" + strike + "\t" + TIME + "\t" + vol + "\t" + interest + ";\t" + isCall);
                  assertEquals(priceDiv, ref, Math.max(ref, 1.) * 1.e-1);
                }
                //                System.out.println("\n");
              }
            }
          }
        }
      }
    }
  }

  /*
   * Just consistency is checked
   */
  @Test
  public void americanPricePropMultipleDividendsTest() {

    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] ProportionalDividendsMat = new double[][] { {.001, .001, .001 }, {.0015, .002, .001 } };
    final int divDim = ProportionalDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (int j = 0; j < divDim; ++j) {
                final double[] dividendTimes = dividendTimesMat[j];
                final double[] ProportionalDividends = ProportionalDividendsMat[j];

                double priceDiv = _model.getAmericanPriceProportionalDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, ProportionalDividends, steps, isCall);
                //                System.out.println(priceDiv);
                double modSpot = SPOT;
                final int divTimes = ProportionalDividends.length;
                for (int i = 0; i < divTimes; ++i) {
                  modSpot -= ProportionalDividends[i] * Math.exp(-interest * dividendTimes[i]);
                }
                double priceMod = _model.getAmericanPrice(model, modSpot, strike, TIME, vol, interest, steps, isCall);
                //                System.out.println(priceMod);
                double price = _model.getAmericanPrice(model, SPOT, strike, TIME, vol, interest, steps, isCall);
                //                System.out.println(price);
                final double ref = Math.abs(priceDiv - priceMod) > Math.abs(priceDiv - price) ? price : priceMod;
                if (interest > 0.) {
                  //                  System.out.println(SPOT + "\t" + strike + "\t" + TIME + "\t" + vol + "\t" + interest + ";\t" + isCall);
                  assertEquals(priceDiv, ref, Math.max(ref, 1.) * 1.e-1);
                }
                //                System.out.println("\n");
              }
            }
          }
        }
      }
    }
  }

  /*
   * Just consistency is checked
   */
  @Test
  public void AmericanGreeksDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.0015, 0.002, 0.001 };
    final double[] cashDividends = new double[] {.1, .3, .2 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 301;
              final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
              final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                  Math.exp(-interest * dividendTimes[2]);
              final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, nSteps, isCall);

              final GreekResultCollection resRes = _model.getGreeks(lattice, function, resSpot, TIME, vol, interest, 0.);
              final GreekResultCollection resMod = _model.getGreeks(lattice, function, modSpot, TIME, vol, interest, 0.);
              final GreekResultCollection resBare = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, 0.);

              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, propDividend);
              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, cashDividend);

              final double refPriceProp = Math.abs(resProp.get(Greek.FAIR_PRICE) - resRes.get(Greek.FAIR_PRICE)) > Math.abs(resProp.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
              final double refDeltaProp = Math.abs(resProp.get(Greek.DELTA) - resRes.get(Greek.DELTA)) > Math.abs(resProp.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
              final double refGammaProp = Math.abs(resProp.get(Greek.GAMMA) - resRes.get(Greek.GAMMA)) > Math.abs(resProp.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
              final double refThetaProp = Math.abs(resProp.get(Greek.THETA) - resRes.get(Greek.THETA)) > Math.abs(resProp.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
                  .get(Greek.THETA) : resRes.get(Greek.THETA);
              assertEquals(resProp.get(Greek.FAIR_PRICE), refPriceProp, Math.max(1., Math.abs(refPriceProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.DELTA), refDeltaProp, Math.max(1., Math.abs(refDeltaProp)));
              assertEquals(resProp.get(Greek.GAMMA), refGammaProp, Math.max(1., Math.abs(refGammaProp)));
              assertEquals(resProp.get(Greek.THETA), refThetaProp, Math.max(1., Math.abs(refThetaProp)));

              final double refPriceCash = Math.abs(resCash.get(Greek.FAIR_PRICE) - resMod.get(Greek.FAIR_PRICE)) > Math.abs(resCash.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
              final double refDeltaCash = Math.abs(resCash.get(Greek.DELTA) - resMod.get(Greek.DELTA)) > Math.abs(resCash.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
              final double refGammaCash = Math.abs(resCash.get(Greek.GAMMA) - resMod.get(Greek.GAMMA)) > Math.abs(resCash.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
              final double refThetaCash = Math.abs(resCash.get(Greek.THETA) - resMod.get(Greek.THETA)) > Math.abs(resCash.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
                  .get(Greek.THETA) : resRes.get(Greek.THETA);
              assertEquals(resProp.get(Greek.FAIR_PRICE), refPriceCash, Math.max(1., Math.abs(refPriceCash)) * 1.e-1);
              assertEquals(resProp.get(Greek.DELTA), refDeltaCash, Math.max(1., Math.abs(refDeltaCash)));
              assertEquals(resProp.get(Greek.GAMMA), refGammaCash, Math.max(1., Math.abs(refGammaCash)));
              assertEquals(resProp.get(Greek.THETA), refThetaCash, Math.max(1., Math.abs(refThetaCash)));

            }
          }
        }
      }
    }
  }

  //  /*
  //   * Just consistency is checked
  //   */
  //  @Test
  //  public void AmericanBarrierDiscreteDividendLatticeTest() {
  //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
  //        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
  //
  //    final double[] propDividends = new double[] {0.0015, 0.002, 0.001 };
  //    final double[] cashDividends = new double[] {.1, .3, .2 };
  //    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };
  //
  //    final boolean[] tfSet = new boolean[] {true, false };
  //    for (final LatticeSpecification lattice : lattices) {
  //      for (final boolean isCall : tfSet) {
  //        for (final double strike : STRIKES) {
  //          for (final double interest : INTERESTS) {
  //            for (final double vol : VOLS) {
  //              final int nSteps = 301;
  //              final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
  //              final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
  //                  Math.exp(-interest * dividendTimes[2]);
  //              final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, nSteps, isCall);
  //
  //              final GreekResultCollection resRes = _model.getGreeks(lattice, function, resSpot, TIME, vol, interest, 0.);
  //              final GreekResultCollection resMod = _model.getGreeks(lattice, function, modSpot, TIME, vol, interest, 0.);
  //              final GreekResultCollection resBare = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, 0.);
  //
  //              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
  //              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
  //              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, propDividend);
  //              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, cashDividend);
  //
  //              final double refPriceProp = Math.abs(resProp.get(Greek.FAIR_PRICE) - resRes.get(Greek.FAIR_PRICE)) > Math.abs(resProp.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
  //                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
  //              final double refDeltaProp = Math.abs(resProp.get(Greek.DELTA) - resRes.get(Greek.DELTA)) > Math.abs(resProp.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
  //                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
  //              final double refGammaProp = Math.abs(resProp.get(Greek.GAMMA) - resRes.get(Greek.GAMMA)) > Math.abs(resProp.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
  //                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
  //              final double refThetaProp = Math.abs(resProp.get(Greek.THETA) - resRes.get(Greek.THETA)) > Math.abs(resProp.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
  //                  .get(Greek.THETA) : resRes.get(Greek.THETA);
  //              assertEquals(resProp.get(Greek.FAIR_PRICE), refPriceProp, Math.max(1., Math.abs(refPriceProp)) * 1.e-1);
  //              assertEquals(resProp.get(Greek.DELTA), refDeltaProp, Math.max(1., Math.abs(refDeltaProp)));
  //              assertEquals(resProp.get(Greek.GAMMA), refGammaProp, Math.max(1., Math.abs(refGammaProp)));
  //              assertEquals(resProp.get(Greek.THETA), refThetaProp, Math.max(1., Math.abs(refThetaProp)));
  //
  //              final double refPriceCash = Math.abs(resCash.get(Greek.FAIR_PRICE) - resMod.get(Greek.FAIR_PRICE)) > Math.abs(resCash.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
  //                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
  //              final double refDeltaCash = Math.abs(resCash.get(Greek.DELTA) - resMod.get(Greek.DELTA)) > Math.abs(resCash.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
  //                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
  //              final double refGammaCash = Math.abs(resCash.get(Greek.GAMMA) - resMod.get(Greek.GAMMA)) > Math.abs(resCash.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
  //                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
  //              final double refThetaCash = Math.abs(resCash.get(Greek.THETA) - resMod.get(Greek.THETA)) > Math.abs(resCash.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
  //                  .get(Greek.THETA) : resRes.get(Greek.THETA);
  //              assertEquals(resProp.get(Greek.FAIR_PRICE), refPriceCash, Math.max(1., Math.abs(refPriceCash)) * 1.e-1);
  //              assertEquals(resProp.get(Greek.DELTA), refDeltaCash, Math.max(1., Math.abs(refDeltaCash)));
  //              assertEquals(resProp.get(Greek.GAMMA), refGammaCash, Math.max(1., Math.abs(refGammaCash)));
  //              assertEquals(resProp.get(Greek.THETA), refThetaCash, Math.max(1., Math.abs(refThetaCash)));
  //
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }

  //  @Test
  //  public void timeVaryingVolAmericanTest() {
  //    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
  //    final LatticeSpecification lattice2 = new TimeVaryingLatticeSpecification();
  //    final double[] time_set = new double[] {0.5, 1., 2. };
  //    final int steps = 301;
  //
  //    final double[] vol = new double[steps];
  //    final double[] rate = new double[steps];
  //    final double constA = 0.01;
  //    final double constB = 0.001;
  //    final double constC = 0.1;
  //    final double constD = 0.05;
  //
  //    for (final double strike : STRIKES) {
  //      for (final double time : time_set) {
  //        for (int i = 0; i < steps; ++i) {
  //          rate[i] = constA + constB * i * time / steps;
  //          vol[i] = constC + constD * Math.sin(i * time / steps);
  //        }
  //        final double rateRef = constA + 0.5 * constB * time;
  //        final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));
  //
  //        final boolean[] tfSet = new boolean[] {true, false };
  //        for (final boolean isCall : tfSet) {
  //          final double res1 = _model.getAmericanPrice(lattice1, SPOT, strike, time, vol, rate, steps, isCall);
  //          //          System.out.println(res1);
  //          if (!isCall) {
  //            final double res2 = _model.getAmericanPrice(lattice2, SPOT, strike, time, volRef, rateRef, steps, isCall);
  //            assertEquals(res1, res2, Math.max(res2, 1.) * Math.pow(0.1, 1. / time));
  //            //          System.out.println(res2);
  //          } else {
  //            final double res3 = _model.getEuropeanPrice(lattice1, SPOT, strike, time, vol, rate, steps, isCall);
  //            assertEquals(res1, res3, 1.e-12);
  //            //          System.out.println(res3);
  //          }
  //        }
  //      }
  //    }
  //  }

  @Test
  public void americanGreeksContDividendTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final boolean[] tfSet = new boolean[] {true, false };

    final double time = 10.;

    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int nTf = 2;
    final int nDiv = DIVIDENDS.length;
    final int steps = 417;
    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            for (int n = 0; n < nTf; ++n) {
              for (int i = 0; i < nDiv; ++i) {
                final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], steps, tfSet[n]);
                final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, time, VOLS[l], INTERESTS[j], DIVIDENDS[i]);
                final BjerksundStenslandModel bs = new BjerksundStenslandModel();
                final double[] first = bs.getPriceAdjoint(SPOT, STRIKES[k], INTERESTS[j], INTERESTS[j] - DIVIDENDS[i], time, VOLS[l], tfSet[n]);
                final double[] deltaGamma = bs.getPriceDeltaGamma(SPOT, STRIKES[k], INTERESTS[j], INTERESTS[j] - DIVIDENDS[i], time, VOLS[l], tfSet[n]);
                //                System.out.println(SPOT + "\t" + STRIKES[k] + "\t" + INTERESTS[j] + "\t" + DIVIDENDS[i] + "\t" + time + "\t" + VOLS[l] + "\t" + tfSet[n] + "\t" + m);

                assertEquals(resNew.get(Greek.DELTA), deltaGamma[1], Math.abs(deltaGamma[1]));
                /*
                 * If the spot is close to the exercise boundary, c_{20) = c_{21} = c_{22} sometimes happens in some lattice specification leading to vanishing gamma
                 * In this case, the gamma value is not accurate and we need to try other lattice specifications in order to to check if this is an artifact or not
                 */
                assertEquals(resNew.get(Greek.GAMMA), deltaGamma[2], Math.max(Math.abs(deltaGamma[2]), 0.1));
                /*
                 * Bjerksund-Stensland Model produces a negative "theta" by some reason
                 */
                if (first[5] > 0.) {
                  assertEquals(resNew.get(Greek.THETA), -first[5], Math.max(Math.abs(first[5]), 0.1));
                }

              }
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
  public void dTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new TianLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(),
        new LeisenReimerLatticeSpecification() };
    //    final double spot = 105.0;
    //    final double strike = 81.0;
    //    final double interest = -0.01;
    //    final double dividend = 0.0;
    //    final double time = 10.0;
    //    final double vol = 0.05;
    //    final boolean isCall = true;

    final double spot = 105.0;
    final double strike = 138.0;
    final double interest = 0.01;
    final double dividend = 0.0;
    final double time = 10.0;
    final double vol = 0.1;
    final boolean isCall = false;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double[] firstBs = bs.getPriceAdjoint(spot, strike, interest, interest - dividend, time, vol, isCall);
    final BaroneAdesiWhaleyModel baw = new BaroneAdesiWhaleyModel();
    final double[] firstBaw = baw.getPriceAdjoint(spot, strike, interest, interest - dividend, time, vol, isCall);
    System.out.println(firstBs[5]);
    System.out.println(firstBaw[5]);

    final int nSteps = 617;

    for (final LatticeSpecification lattice : lattices) {
      final double[] resDiv = _model.getAmericanGreeks(lattice, spot, strike, time, vol, interest, dividend, nSteps, isCall);
      final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, nSteps, isCall);
      final GreekResultCollection resNew = _model.getGreeks(lattice, function, spot, time, vol, interest, dividend);

      System.out.println(resDiv[0] + "\t" + resDiv[3] + "\t" + resNew.get(Greek.THETA));
    }
  }

  @Test
  public void americanPutGreeksTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int steps = 1117;

    /*
     * Reference values computed by Leisen & Reimer with n=1517
     */
    final double[][][] exDelta = new double[][][] { { {-0.014881626500638, -0.122320306886183, -0.234365060695953 }, {
        -0.339205802251104, -0.388140843643441, -0.291610061032319 }, {
        -0.640018487753769, -0.540781192942660, -0.318687114262474 }, {
        -0.643486955912236, -0.542623510155043, -0.319018720523979 }, {
        -0.877139461972654, -0.692711380546793, -0.347845580051385 }, {
        -0.998755591884291, -0.924427280526098, -0.418882111538830 } }, { {-0.004897699744968, -0.085603286420193, -0.221980661540731 }, {
        -0.204863382616579, -0.312406895502999, -0.277708801913732 }, {
        -0.479569229779019, -0.459192008296251, -0.304203903689547 }, {
        -0.483270095510765, -0.461035189281400, -0.304528899959058 }, {
        -0.773692454817612, -0.617407543618158, -0.332827896274769 }, {
        -0.995538044089594, -0.890768587980812, -0.402946231412317 } }, { {-0.004359663851066, -0.082635496280172, -0.221039092578087 }, {
        -0.194675346985548, -0.306306489596917, -0.276738870762684 }, {
        -0.467861792085781, -0.453143653469079, -0.303244197015975 }, {
        -0.471614945555351, -0.454997113579120, -0.303569414952370 }, {
        -0.773762547122799, -0.613193112736144, -0.331896785778517 }, {
        -1.000000000000000, -0.897032874544827, -0.402166458946481 } }, { {-0.002718398851028, -0.071926275952036, -0.217768812175972 }, {
        -0.160046104951321, -0.285073304969120, -0.273573506780415 }, {
        -0.434578188835797, -0.434274902201187, -0.300242522038988 }, {
        -0.438716786916757, -0.436205122256318, -0.300570217125642 }, {
        -0.821505741180240, -0.605995994831714, -0.329157980446281 }, {
        -1.000000000000000, -0.957415459022728, -0.400479269245190 } }, { {-0.001471871574208, -0.060447364696806, -0.214193661718878 }, {
        -0.125406038139822, -0.262290629158548, -0.270341499298573 }, {
        -0.407792576384516, -0.416612620804891, -0.297335864919290 }, {
        -0.412630608150302, -0.418679612797512, -0.297668226568210 }, {
        -0.962854119062361, -0.608703017663675, -0.326727321400418 }, {
        -1.000000000000000, -1.000000000000000, -0.399814406410798 } } };
    final double[][][] exGamma = new double[][][] { { {0.003503805481590, 0.009432955804714, 0.002853154917297 }, {
        0.034041463367876, 0.017813793247544, 0.003190059240294 }, {
        0.034783499077940, 0.018448167151535, 0.003317888080333 }, {
        0.034666416887891, 0.018439178355934, 0.003319333747143 }, {
        0.018914950520415, 0.016336220066209, 0.003434198128643 }, {
        0.000383497870915, 0.006620379246081, 0.003629148565422 } }, { {0.001323250417423, 0.007279341181473, 0.002767446770111 }, {
        0.026414590899216, 0.016458270403081, 0.003116592448500 }, {
        0.037042283861517, 0.018448166856191, 0.003251755525703 }, {
        0.037058252084875, 0.018456716028671, 0.003253296087852 }, {
        0.027977157456387, 0.017734544974307, 0.003376822568943 }, {
        0.001216801835783, 0.008697935186808, 0.003595855557379 } }, { {0.001193629984056, 0.007097376469900, 0.002764379147412 }, {
        0.025740817970497, 0.016380009581449, 0.003116904386891 }, {
        0.037556354385415, 0.018550045767377, 0.003253974256007 }, {
        0.037592591302524, 0.018561089156062, 0.003255539350256 }, {
        0.030084620771250, 0.018058047361325, 0.003381305326465 }, {
        0, 0.009360073399808, 0.003606760222134 } }, { {0.000784673757861, 0.006435086766269, 0.002761737676874 }, {
        0.023498065265425, 0.016249673656883, 0.003131819743348 }, {
        0.041360397172803, 0.019312837399267, 0.003278742898400 }, {
        0.041511333183470, 0.019336680526852, 0.003280434748468 }, {
        0.045383252199437, 0.020027001354998, 0.003417784472141 }, {
        0, 0.014028030261333, 0.003676695651071 } }, { {0.000453167081619, 0.005693670613991, 0.002767712806744 }, {
        0.020996909681787, 0.016243112150641, 0.003163841577838 }, {
        0.048265326559198, 0.020672573685421, 0.003325401936554 }, {
        0.048629035127462, 0.020717086287208, 0.003327282324859 }, {
        0.080702324548011, 0.023488350349200, 0.003481891522142 }, {
        0, 0, 0.003791052234156 } } };
    final double[][][] exTheta = new double[][][] { { {-0.064498095331651, -0.662667881399460, -4.454029185506673 }, {
        -0.850973988605841, -1.455847936374344, -5.087193616867534 }, {
        -1.221350671097671, -1.696654895399556, -5.349918769099305 }, {
        -1.224087467382668, -1.698742102335414, -5.352999868515796 }, {
        -1.326823165954796, -1.804905598695592, -5.608751333744251 }, {
        -1.443209376168559, -1.731525014810447, -6.143960962242465 } }, { {-0.018236044815116, -0.401273682628685, -3.813887580059414 }, {
        -0.364026080829825, -0.907262155969830, -4.295053968088670 }, {
        -0.510488974466533, -1.016955197947514, -4.481325583860082 }, {
        -0.510709036544678, -1.017426471080479, -4.483448671071259 }, {
        -0.385560201195838, -0.977616791708686, -4.653683602823921 }, {
        -0.016769050299388, -0.479473677172797, -4.955538440013044 } }, { {-0.015976908751189, -0.381684509059388, -3.761161011237013 }, {
        -0.333084983614123, -0.866079482874228, -4.231008002073541 }, {
        -0.464353431787041, -0.966630131242269, -4.411694385747229 }, {
        -0.464407868350514, -0.966990915383428, -4.413747889735914 }, {
        -0.323294460516038, -0.917194869816280, -4.577807148389552 }, {
        0.138000000000000, -0.388069722995840, -4.863115293071950 } }, { {-0.009341876862604, -0.313260441725454, -3.568706772905693 }, {
        -0.235174530028392, -0.725123406429818, -3.999808376016376 }, {
        -0.324583713479766, -0.798483276398991, -4.161639853043242 }, {
        -0.324256298162962, -0.798530329652530, -4.163459431091307 }, {
        -0.147390357143069, -0.721098779451898, -4.306849290838867 }, {
        0.690000000000000, -0.105326327647521, -4.537546019103879 } }, { {-0.004653989990591, -0.244408936636584, -3.351477870191915 }, {
        -0.151089126194059, -0.583508304708060, -3.741512554579546 }, {
        -0.208563910485908, -0.633600472942907, -3.883653254072278 }, {
        -0.208059169618752, -0.633401019727906, -3.885229656370252 }, {
        -0.011102790723010, -0.535414823227399, -4.007232143496033 }, {
        1.379999999999999, 1.379999999999999, -4.182265615165154 } } };

    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            final double[] greeks = _model.getAmericanGreeks(lattice, SPOT, STRIKES[k], TIME, VOLS[l], INTERESTS[j], steps, false);
            if (lattice instanceof LeisenReimerLatticeSpecification) {
              assertEquals(greeks[1], exDelta[j][k][l], Math.max(exDelta[j][k][l], 1.) / steps);
              assertEquals(greeks[2], exGamma[j][k][l], Math.max(exGamma[j][k][l], 1.) / steps);
              assertEquals(greeks[3], exTheta[j][k][l], Math.max(exTheta[j][k][l], 1.) / Math.sqrt(steps));
            } else {
              assertEquals(greeks[1], exDelta[j][k][l], Math.max(Math.abs(exDelta[j][k][l]), 1.) / Math.sqrt(steps));
              assertEquals(greeks[2], exGamma[j][k][l], Math.max(Math.abs(exGamma[j][k][l]), 1.) / Math.sqrt(steps));
              /*
               * Because theta is poorly approximated in binomial models, the output is not tested here
               * For the 3 cases below, the resulting value hugely depends on lattice specifications
               */
              if (exTheta[j][k][l] != 0.138 && exTheta[j][k][l] != 0.69 && exTheta[j][k][l] != 1.379999999999999) {
                assertEquals(greeks[3], exTheta[j][k][l], Math.max(Math.abs(exTheta[j][k][l]), 1.) / Math.sqrt(steps));
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void timeVaryingVolEuropeanTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 801;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
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
          final double rateRef = constA + 0.5 * constB * time;
          final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));

          final OptionFunctionProvider1D functionVanilla = new EuropeanVanillaOptionFunctionProvider(strike, steps, isCall);
          final double resPrice = _model.getPrice(functionVanilla, SPOT, time, vol, rate, dividend);
          final GreekResultCollection resGreeks = _model.getGreeks(functionVanilla, SPOT, time, vol, rate, dividend);

          final double resPriceConst = _model.getPrice(lattice1, functionVanilla, SPOT, time, volRef, rateRef, dividend[0]);
          final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, functionVanilla, SPOT, time, volRef, rateRef, dividend[0]);
          assertEquals(resPrice, resPriceConst, Math.abs(resPriceConst) * 1.e-1);
          assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));
          //          System.out.println(resGreeks.get(Greek.THETA) + "\t" + resGreeksConst.get(Greek.THETA));

          final double[] barrierSet = new double[] {SPOT * 0.9, SPOT * 1.1 };
          for (final double barrier : barrierSet) {
            final OptionFunctionProvider1D functionBarrierDown = new EuropeanSingleBarrierOptionFunctionProvider(strike, steps, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUp = new EuropeanSingleBarrierOptionFunctionProvider(strike, steps, isCall, barrier,
                EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.UpAndOut);

            final double resPriceBarrierDown = _model.getPrice(functionBarrierDown, SPOT, time, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierDown = _model.getGreeks(functionBarrierDown, SPOT, time, vol, rate, dividend);
            final double resPriceConstBarrierDown = _model.getPrice(lattice1, functionBarrierDown, SPOT, time, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierDown = _model.getGreeks(lattice1, functionBarrierDown, SPOT, time, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierDown, resPriceConstBarrierDown, Math.max(Math.abs(resPriceConstBarrierDown), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierDown.get(Greek.DELTA), resGreeksConstBarrierDown.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.GAMMA), resGreeksConstBarrierDown.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.THETA), resGreeksConstBarrierDown.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.THETA)), 0.1));

            final double resPriceBarrierUp = _model.getPrice(functionBarrierUp, SPOT, time, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierUp = _model.getGreeks(functionBarrierUp, SPOT, time, vol, rate, dividend);
            final double resPriceConstBarrierUp = _model.getPrice(lattice1, functionBarrierUp, SPOT, time, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierUp = _model.getGreeks(lattice1, functionBarrierUp, SPOT, time, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierUp, resPriceConstBarrierUp, Math.max(Math.abs(resPriceConstBarrierUp), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierUp.get(Greek.DELTA), resGreeksConstBarrierUp.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.GAMMA), resGreeksConstBarrierUp.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.THETA), resGreeksConstBarrierUp.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.THETA)), 0.1));
          }

        }
      }
    }
  }

  @Test
  public void timeVaryingVolAmericanTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 801;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
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
          final double rateRef = constA + 0.5 * constB * time;
          final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));

          final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, steps, isCall);
          final double resPrice = _model.getPrice(function, SPOT, time, vol, rate, dividend);
          final GreekResultCollection resGreeks = _model.getGreeks(function, SPOT, time, vol, rate, dividend);

          final double resPriceConst = _model.getPrice(lattice1, function, SPOT, time, volRef, rateRef, dividend[0]);
          final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, function, SPOT, time, volRef, rateRef, dividend[0]);
          assertEquals(resPrice, resPriceConst, Math.abs(resPriceConst) * 1.e-1);
          assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));
          //          System.out.println(resGreeks.get(Greek.THETA) + "\t" + resGreeksConst.get(Greek.THETA));

          final double[] barrierSet = new double[] {SPOT * 0.9, SPOT * 1.1 };
          for (final double barrier : barrierSet) {
            final OptionFunctionProvider1D functionBarrierDown = new AmericanSingleBarrierOptionFunctionProvider(strike, steps, isCall, barrier,
                AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
            final OptionFunctionProvider1D functionBarrierUp = new AmericanSingleBarrierOptionFunctionProvider(strike, steps, isCall, barrier,
                AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.UpAndOut);

            final double resPriceBarrierDown = _model.getPrice(functionBarrierDown, SPOT, time, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierDown = _model.getGreeks(functionBarrierDown, SPOT, time, vol, rate, dividend);
            final double resPriceConstBarrierDown = _model.getPrice(lattice1, functionBarrierDown, SPOT, time, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierDown = _model.getGreeks(lattice1, functionBarrierDown, SPOT, time, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierDown, resPriceConstBarrierDown, Math.max(Math.abs(resPriceConstBarrierDown), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierDown.get(Greek.DELTA), resGreeksConstBarrierDown.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.GAMMA), resGreeksConstBarrierDown.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierDown.get(Greek.THETA), resGreeksConstBarrierDown.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierDown.get(Greek.THETA)), 0.1));

            final double resPriceBarrierUp = _model.getPrice(functionBarrierUp, SPOT, time, vol, rate, dividend);
            final GreekResultCollection resGreeksBarrierUp = _model.getGreeks(functionBarrierUp, SPOT, time, vol, rate, dividend);
            final double resPriceConstBarrierUp = _model.getPrice(lattice1, functionBarrierUp, SPOT, time, volRef, rateRef, dividend[0]);
            final GreekResultCollection resGreeksConstBarrierUp = _model.getGreeks(lattice1, functionBarrierUp, SPOT, time, volRef, rateRef, dividend[0]);
            assertEquals(resPriceBarrierUp, resPriceConstBarrierUp, Math.max(Math.abs(resPriceConstBarrierUp), 0.1) * 1.e-1);
            assertEquals(resGreeksBarrierUp.get(Greek.DELTA), resGreeksConstBarrierUp.get(Greek.DELTA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.DELTA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.GAMMA), resGreeksConstBarrierUp.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.GAMMA)), 0.1) * 0.1);
            assertEquals(resGreeksBarrierUp.get(Greek.THETA), resGreeksConstBarrierUp.get(Greek.THETA), Math.max(Math.abs(resGreeksConstBarrierUp.get(Greek.THETA)), 0.1));
          }

        }
      }
    }
  }

  /*************************************************************************
   * Tests below compare old method and new method
   *************************************************************************/

  @Test
  public void EuropeanPriceLatticeNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final int[] choicesSteps = new int[] {11, 105 };
                for (final int nSteps : choicesSteps) {
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, nSteps, isCall);
                  final double resNew = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                  final double resOld = _model.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  //                  System.out.println(resNew + "\t" + resOld);
                  assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void EuropeanGreekLatticeNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final int[] choicesSteps = new int[] {11, 105 };
                for (final int nSteps : choicesSteps) {
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, nSteps, isCall);
                  final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, dividend);
                  final double[] resOld = _model.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  //                  System.out.println(resNew + "\t" + resOld);
                  assertEquals(resNew.get(Greek.FAIR_PRICE), resOld[0], Math.max(Math.abs(resOld[0]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.DELTA), resOld[1], Math.max(Math.abs(resOld[1]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.GAMMA), resOld[2], Math.max(Math.abs(resOld[2]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.THETA), resOld[3], Math.max(Math.abs(resOld[3]), 1.) * 1.e-14);
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void AmericanPriceLatticeNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final int[] choicesSteps = new int[] {11, 105 };
                for (final int nSteps : choicesSteps) {
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, nSteps, isCall);
                  final double resNew = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, dividend);
                  final double resOld = _model.getAmericanPrice(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  //                    System.out.println(resNew + "\t" + resOld);
                  assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void AmericanGreekLatticeNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (final double dividend : DIVIDENDS) {
                final int[] choicesSteps = new int[] {11, 105 };
                for (final int nSteps : choicesSteps) {
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, nSteps, isCall);
                  final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, dividend);
                  final double[] resOld = _model.getAmericanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                  //                  System.out.println(resNew + "\t" + resOld);
                  assertEquals(resNew.get(Greek.FAIR_PRICE), resOld[0], Math.max(Math.abs(resOld[0]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.DELTA), resOld[1], Math.max(Math.abs(resOld[1]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.GAMMA), resOld[2], Math.max(Math.abs(resOld[2]), 1.) * 1.e-14);
                  assertEquals(resNew.get(Greek.THETA), resOld[3], Math.max(Math.abs(resOld[3]), 1.) * 1.e-14);
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void barrierEuropeanNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    //    new LogEqualProbabiliesLatticeSpecification()  //removed

    final double[] vols = new double[] {0.02, 0.09 };

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        final BinomialTreeOptionPricingModel modelB = new BinomialTreeOptionPricingModel(barrier, type);
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  final int[] choicesSteps = new int[] {11, 105 };
                  for (final int nSteps : choicesSteps) {
                    final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                        EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    final double resNew = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, 0.);
                    final double resOld = modelB.getEuropeanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                    assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void barrierEuropeanGreekNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    //    new LogEqualProbabiliesLatticeSpecification()  //removed

    final double[] vols = new double[] {0.02, 0.09 };

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        final BinomialTreeOptionPricingModel modelB = new BinomialTreeOptionPricingModel(barrier, type);
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  for (final double dividend : DIVIDENDS) {
                    final int[] choicesSteps = new int[] {11, 105 };
                    for (final int nSteps : choicesSteps) {
                      final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                          EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, dividend);
                      final double[] resOld = modelB.getEuropeanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                      //                  System.out.println(resNew + "\t" + resOld);
                      assertEquals(resNew.get(Greek.FAIR_PRICE), resOld[0], Math.max(Math.abs(resOld[0]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.DELTA), resOld[1], Math.max(Math.abs(resOld[1]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.GAMMA), resOld[2], Math.max(Math.abs(resOld[2]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.THETA), resOld[3], Math.max(Math.abs(resOld[3]), 1.) * 1.e-14);
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

  @Test
  public void barrierAmericanNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    //    new LogEqualProbabiliesLatticeSpecification()  //removed

    final double[] vols = new double[] {0.02, 0.09 };

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        final BinomialTreeOptionPricingModel modelB = new BinomialTreeOptionPricingModel(barrier, type);
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  final int[] choicesSteps = new int[] {11, 105 };
                  for (final int nSteps : choicesSteps) {
                    final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                        AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    final double resNew = _model.getPrice(lattice, function, SPOT, TIME, vol, interest, 0.);
                    final double resOld = modelB.getAmericanPrice(lattice, SPOT, strike, TIME, vol, interest, nSteps, isCall);
                    assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void barrierAmericanGreekNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    //    new LogEqualProbabiliesLatticeSpecification()  //removed

    final double[] vols = new double[] {0.02, 0.09 };

    final double[] barrierSet = new double[] {90, 121 };
    final String[] typeSet = new String[] {"DownAndOut", "UpAndOut" };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final double barrier : barrierSet) {
      for (final String type : typeSet) {
        final BinomialTreeOptionPricingModel modelB = new BinomialTreeOptionPricingModel(barrier, type);
        for (final LatticeSpecification lattice : lattices) {
          for (final boolean isCall : tfSet) {
            for (final double strike : STRIKES) {
              for (final double interest : INTERESTS) {
                for (final double vol : vols) {
                  for (final double dividend : DIVIDENDS) {
                    final int[] choicesSteps = new int[] {11, 105 };
                    for (final int nSteps : choicesSteps) {
                      final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, nSteps, isCall, barrier,
                          AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, TIME, vol, interest, dividend);
                      final double[] resOld = modelB.getAmericanGreeks(lattice, SPOT, strike, TIME, vol, interest, dividend, nSteps, isCall);
                      //                  System.out.println(resNew + "\t" + resOld);
                      assertEquals(resNew.get(Greek.FAIR_PRICE), resOld[0], Math.max(Math.abs(resOld[0]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.DELTA), resOld[1], Math.max(Math.abs(resOld[1]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.GAMMA), resOld[2], Math.max(Math.abs(resOld[2]), 1.) * 1.e-14);
                      assertEquals(resNew.get(Greek.THETA), resOld[3], Math.max(Math.abs(resOld[3]), 1.) * 1.e-14);
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

  @Test
  public void europeanPriceDiscreteDividendsNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] cashDividendsMat = new double[][] { {5., 5., 5. }, {3., 4.5, 2.9 } };
    final double[][] propDividendsMat = new double[][] { {0.05, 0.05, 0.05 }, {0.08, 0.1, 0.097 } };
    final int divDim = cashDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (int j = 0; j < divDim; ++j) {
                final double[] dividendTimes = dividendTimesMat[j];
                final double[] cashDividends = cashDividendsMat[j];
                final double[] propDividends = propDividendsMat[j];
                double oldCash = _model.getEuropeanPriceCashDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, steps, isCall);
                double oldProp = _model.getEuropeanPriceProportionalDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, propDividends, steps, isCall);
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, steps, isCall);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double newCash = _model.getPrice(model, function, SPOT, TIME, vol, interest, cashDividend);
                final double newProp = _model.getPrice(model, function, SPOT, TIME, vol, interest, propDividend);
                assertEquals(newCash, oldCash, Math.max(Math.abs(oldCash), 1.) * 1.e-13);
                assertEquals(newProp, oldProp, Math.max(Math.abs(oldProp), 1.) * 1.e-13);
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void americanPriceDiscreteDividendsNewMethodTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] cashDividendsMat = new double[][] { {5., 5., 5. }, {3., 4.5, 2.9 } };
    final double[][] propDividendsMat = new double[][] { {0.05, 0.05, 0.05 }, {0.08, 0.1, 0.097 } };
    final int divDim = cashDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              for (int j = 0; j < divDim; ++j) {
                final double[] dividendTimes = dividendTimesMat[j];
                final double[] cashDividends = cashDividendsMat[j];
                final double[] propDividends = propDividendsMat[j];
                double oldCash = _model.getAmericanPriceCashDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, steps, isCall);
                double oldProp = _model.getAmericanPriceProportionalDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, propDividends, steps, isCall);
                final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, steps, isCall);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double newCash = _model.getPrice(model, function, SPOT, TIME, vol, interest, cashDividend);
                final double newProp = _model.getPrice(model, function, SPOT, TIME, vol, interest, propDividend);
                assertEquals(newCash, oldCash, Math.max(Math.abs(oldCash), 1.) * 1.e-13);
                assertEquals(newProp, oldProp, Math.max(Math.abs(oldProp), 1.) * 1.e-13);
              }
            }
          }
        }
      }
    }
  }

  //  @Test
  //  public void americanPutGreeksTinyTimeTest() {
  //    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
  //        new LogEqualProbabiliesLatticeSpecification(), new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(),
  //        new LeisenReimerLatticeSpecification() };
  //    final int nLattices = lattices.length;
  //    final int nStrikes = STRIKES.length;
  //    final int nInterests = INTERESTS.length;
  //    final int nVols = VOLS.length;
  //    final int steps = 317;
  //
  //    final double tinyTime = 1.e-2;
  //    _model.getAmericanGreeks(lattices[nLattices - 1], SPOT, STRIKES[0], tinyTime, VOLS[0], INTERESTS[0], steps, false);
  //
  //    for (int m = 0; m < nLattices; ++m) {
  //      final LatticeSpecification lattice = lattices[m];
  //      for (int j = 0; j < nInterests; ++j) {
  //        for (int k = 0; k < nStrikes; ++k) {
  //          for (int l = 0; l < nVols; ++l) {
  //            final double[] greeks = _model.getAmericanGreeks(lattice, SPOT, STRIKES[k], tinyTime, VOLS[l], INTERESTS[j], steps, false);
  //            if (Double.isNaN(greeks[0])) {
  //              System.out.println(j);
  //            }
  //            final double bsmPrice = BlackScholesFormulaRepository.price(SPOT, STRIKES[k], tinyTime, VOLS[l], INTERESTS[j], INTERESTS[j], false);
  //            final double bsmDelta = BlackScholesFormulaRepository.delta(SPOT, STRIKES[k], tinyTime, VOLS[l], INTERESTS[j], INTERESTS[j], false);
  //            final double bsmGamma = BlackScholesFormulaRepository.gamma(SPOT, STRIKES[k], tinyTime, VOLS[l], INTERESTS[j], INTERESTS[j]);
  //            final double bsmTheta = BlackScholesFormulaRepository.theta(SPOT, STRIKES[k], tinyTime, VOLS[l], INTERESTS[j], INTERESTS[j], false);
  //            if (lattice instanceof LeisenReimerLatticeSpecification) {
  //              assertEquals(greeks[0], bsmPrice, Math.max(bsmPrice, 1.) / steps);
  //              assertEquals(greeks[1], bsmDelta, Math.max(Math.abs(bsmDelta), 1.) / steps);
  //              assertEquals(greeks[2], bsmGamma, Math.max(Math.abs(bsmGamma), 1.) / steps);
  //              //              assertEquals(greeks[3], bsmTheta, Math.max(Math.abs(bsmTheta), 1.) / steps);
  //              //            } else {
  //              //              /*
  //              //               * As the price by LogEqualProbabiliesLatticeSpecification tends to converge to the "true" value from above, 
  //              //               * this lattice specification must be tested separately. 
  //              //               */
  //              //              if (lattice instanceof LogEqualProbabiliesLatticeSpecification) {
  //              //                final double refDelta = exDelta[j][k][l] * (steps + 1.) / (steps - 1.);
  //              //                assertEquals(greeks[1], refDelta, Math.max(Math.abs(refDelta), 1.) / Math.sqrt(steps));
  //              //                final double refGamma = exGamma[j][k][l] * (steps + 1.) / (steps - 1.);
  //              //                assertEquals(greeks[2], refGamma, Math.max(Math.abs(refGamma), 1.) / Math.sqrt(steps));
  //              //                /*
  //              //                 * Because theta is poorly approximated in binomial models, the output is not tested here
  //              //                 */
  //              //                //                final double refTheta = exTheta[j][k][l] * (steps + 1.) / (steps - 1.);
  //              //                //                assertEquals(greeks[3], refTheta, Math.max(Math.abs(refTheta), 1.) / Math.sqrt(steps));
  //              //              } else {
  //              //                assertEquals(greeks[1], exDelta[j][k][l], Math.max(Math.abs(exDelta[j][k][l]), 1.) / Math.sqrt(steps));
  //              //                assertEquals(greeks[2], exGamma[j][k][l], Math.max(Math.abs(exGamma[j][k][l]), 1.) / Math.sqrt(steps));
  //              //                /*
  //              //                 * Because theta is poorly approximated in binomial models, the output is not tested here
  //              //                 */
  //              //                //                assertEquals(greeks[3], exTheta[j][k][l], Math.max(Math.abs(exTheta[j][k][l]), 1.) / Math.sqrt(steps));
  //              //              }
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }

  @Test(enabled = false)
  public void eqpTest() {
    final LatticeSpecification lattice1 = new CoxRossRubinsteinLatticeSpecification();
    final LatticeSpecification lattice3 = new TrigeorgisLatticeSpecification();
    final LatticeSpecification lattice4 = new TianLatticeSpecification();
    int steps;
    for (int i = 0; i < 100; ++i) {
      steps = 2 + i;
      final double exact = BlackScholesFormulaRepository.price(SPOT, STRIKES[1], TIME, VOLS[2], INTERESTS[1], INTERESTS[1], false);
      System.out.println(steps + "\t" + (_model.getAmericanPrice(lattice1, SPOT, STRIKES[1], TIME, VOLS[2], INTERESTS[1], steps, false) - exact) + "\t" +
          (_model.getEuropeanPrice(lattice3, SPOT, STRIKES[1], TIME, VOLS[2], INTERESTS[1], steps, false) - exact) + "\t" +
          (_model.getEuropeanPrice(lattice4, SPOT, STRIKES[1], TIME, VOLS[2], INTERESTS[1], steps, false) - exact));
      //    System.out.println(steps + "\t" + _model.getAmericanPrice(lattice2, SPOT, STRIKES[0], TIME, VOLS[0], INTERESTS[0], steps, false));
    }
  }

  @Test(enabled = false)
  public void americanGreekTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
    final double[] res0 = _model.getAmericanGreeks(lattices[0], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(new DoubleMatrix1D(res0));
    final double[] res1 = _model.getAmericanGreeks(lattices[1], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(new DoubleMatrix1D(res1));
    final double[] res2 = _model.getAmericanGreeks(lattices[2], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(new DoubleMatrix1D(res2));

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double[] bsGreeks = bs.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(new DoubleMatrix1D(bsGreeks));
    final BaroneAdesiWhaleyModel baw = new BaroneAdesiWhaleyModel();
    final double[] bawGreeks = baw.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(new DoubleMatrix1D(bawGreeks));
    System.out.println(BlackScholesFormulaRepository.theta(120, 110, 1., 1., 1., 1., false));
  }

  @Test(enabled = false)
  public void americanThetaTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final double[] res0 = _model.getAmericanGreeks(lattices[0], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res0[3]);
    final double[] res1 = _model.getAmericanGreeks(lattices[1], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res1[3]);
    final double[] res2 = _model.getAmericanGreeks(lattices[2], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res2[3]);
    final double[] res3 = _model.getAmericanGreeks(lattices[3], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res3[3]);
    final double[] res4 = _model.getAmericanGreeks(lattices[4], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res4[3]);
    final double[] res5 = _model.getAmericanGreeks(lattices[5], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res5[3]);
    final double[] res6 = _model.getAmericanGreeks(lattices[6], 120, 110, 1., 1., 1., 1001, false);
    System.out.println(res6[3]);
    final double[] ref = _model.getAmericanGreeks(lattices[6], 120, 110, 1., 1., 1., 30001, false);
    System.out.println(ref[3]);

    System.out.println("\n");

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double[] bsGreeks = bs.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(bsGreeks[5]);
    final BaroneAdesiWhaleyModel baw = new BaroneAdesiWhaleyModel();
    final double[] bawGreeks = baw.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(bawGreeks[5]);
    System.out.println(BlackScholesFormulaRepository.theta(120, 110, 1., 1., 1., 1., false));
  }

  @Test(enabled = false)
  public void americanTheta1Test() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    for (int i = 0; i < 500; ++i) {
      int nSteps = 3 + 2 * i;
      final double[] res0 = _model.getAmericanGreeks(lattices[0], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res1 = _model.getAmericanGreeks(lattices[1], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res2 = _model.getAmericanGreeks(lattices[2], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res3 = _model.getAmericanGreeks(lattices[3], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res4 = _model.getAmericanGreeks(lattices[4], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res5 = _model.getAmericanGreeks(lattices[5], 120, 110, 1., 1., 1., nSteps, false);
      final double[] res6 = _model.getAmericanGreeks(lattices[6], 120, 110, 1., 1., 1., nSteps, false);
      System.out.println(nSteps + "\t" + res0[3] + "\t" + res1[3] + "\t" + res2[3] + "\t" + res3[3] + "\t" + res4[3] + "\t" + res5[3] + "\t" + res6[3]);
    }

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double[] bsGreeks = bs.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(bsGreeks[5]);
    final BaroneAdesiWhaleyModel baw = new BaroneAdesiWhaleyModel();
    final double[] bawGreeks = baw.getPriceAdjoint(120, 110, 1., 1., 1., 1., false);
    System.out.println(bawGreeks[5]);
    System.out.println(BlackScholesFormulaRepository.theta(120, 110, 1., 1., 1., 1., false));
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
