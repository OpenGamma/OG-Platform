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
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

public class BinomialTreeOptionPricingModelTest {

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
      final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, time, nSteps, isCall);
      final GreekResultCollection resNew = _model.getGreeks(lattice, function, spot, vol, interest, dividend);

      System.out.println(resDiv[0] + "\t" + resDiv[3] + "\t" + resNew.get(Greek.THETA));
    }
  }

  /*************************************************************************
   * Tests below compare old method and new method, removed later
   *************************************************************************/

  @Test(enabled = false)
  public void EuropeanSpreadPriceNewMethodTest() {

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              final int[] choicesSteps = new int[] {11, 105 };
              for (final int nSteps : choicesSteps) {
                final OptionFunctionProvider2D function = new EuropeanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double resNew = _model.getPrice(function, SPOT, SPOT * 0.95, vol, vol * 1.05, 0.5, interest, dividend, dividend * 1.05);
                final double resOld = _model.getEuropeanSpreadPrice(SPOT, SPOT * 0.95, strike, TIME, vol, vol * 1.05, 0.5, interest, dividend, dividend * 1.05, nSteps, isCall);
                //                  System.out.println(resNew + "\t" + resOld);
                assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
              }
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
  public void AmericanSpreadPriceNewMethodTest() {

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              final int[] choicesSteps = new int[] {11, 105 };
              for (final int nSteps : choicesSteps) {
                final OptionFunctionProvider2D function = new AmericanSpreadOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double resNew = _model.getPrice(function, SPOT, SPOT * 0.95, vol, vol * 1.05, 0.5, interest, dividend, dividend * 1.05);
                final double resOld = _model.getAmericanSpreadPrice(SPOT, SPOT * 0.95, strike, TIME, vol, vol * 1.05, 0.5, interest, dividend, dividend * 1.05, nSteps, isCall);
                //                  System.out.println(resNew + "\t" + resOld);
                assertEquals(resNew, resOld, Math.max(resOld, 1.) * 1.e-14);
              }
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
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
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double resNew = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double resNew = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                    final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                        EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    final double resNew = _model.getPrice(lattice, function, SPOT, vol, interest, 0.);
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

  @Test(enabled = false)
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
                      final OptionFunctionProvider1D function = new EuropeanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                          EuropeanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                    final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                        AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                    final double resNew = _model.getPrice(lattice, function, SPOT, vol, interest, 0.);
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

  @Test(enabled = false)
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
                      final OptionFunctionProvider1D function = new AmericanSingleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, barrier,
                          AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
                      final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
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

  @Test(enabled = false)
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
                final double oldCash = _model.getEuropeanPriceCashDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, steps, isCall);
                final double oldProp = _model.getEuropeanPriceProportionalDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, propDividends, steps, isCall);
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, steps, isCall);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double newCash = _model.getPrice(model, function, SPOT, vol, interest, cashDividend);
                final double newProp = _model.getPrice(model, function, SPOT, vol, interest, propDividend);
                assertEquals(newCash, oldCash, Math.max(Math.abs(oldCash), 1.) * 1.e-13);
                assertEquals(newProp, oldProp, Math.max(Math.abs(oldProp), 1.) * 1.e-13);
              }
            }
          }
        }
      }
    }
  }

  @Test(enabled = false)
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
                final double oldCash = _model.getAmericanPriceCashDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, cashDividends, steps, isCall);
                final double oldProp = _model.getAmericanPriceProportionalDividends(model, SPOT, strike, TIME, vol, interest, dividendTimes, propDividends, steps, isCall);
                final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, steps, isCall);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double newCash = _model.getPrice(model, function, SPOT, vol, interest, cashDividend);
                final double newProp = _model.getPrice(model, function, SPOT, vol, interest, propDividend);
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
      final int nSteps = 3 + 2 * i;
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
