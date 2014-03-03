/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanVanillaOptionFunctionProviderTest {
  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {81., 97., 105., 105.1, 114., 138. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.001, 0.005, 0.01 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };

  private static final double[] DIVIDENDS = new double[] {0.005, 0.02 };

  /**
   * 
   */
  @Test
  public void ImpliedTreeEuropeanRecoveryTest() {
    final double interest = 0.06;
    final YieldAndDiscountCurve yCrv = YieldCurve.from(ConstantDoublesCurve.from(interest));
    final double cost = 0.02;
    final double atmVol = 0.47;
    final ZonedDateTime date = DateUtils.getUTCDate(2010, 7, 1);
    final double spot = 100;
    final Function<Double, Double> smile = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        return atmVol + (spot - k) * 0.0005;
      }
    };

    final StandardOptionDataBundle data = new StandardOptionDataBundle(yCrv, cost, new VolatilitySurface(FunctionalDoublesSurface.from(smile)), spot, date);

    final double[] strikes = new double[] {spot * 0.9, spot, spot * 1.11 };
    final int nSteps = 7;
    final double time = 1.;

    for (int i = 0; i < strikes.length; ++i) {
      final double strike = strikes[i];
      final boolean isCall = strike >= spot ? true : false;
      final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, time, nSteps, isCall);
      final double tree = _modelTrinomial.getPrice(function, data);
      final double black = BlackScholesFormulaRepository.price(spot, strike * 0.9, time, data.getVolatility(time, strike), interest, cost, isCall);
      assertEquals(tree, black, black * 0.2);
    }

    try {
      _model.getPrice(new EuropeanVanillaOptionFunctionProvider(strikes[2], time, nSteps, true), data);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * 
   * 
   */
  @Test
  public void smoothConvergenceTest() {
    final LatticeSpecification lattice = new FlexibleLatticeSpecification();

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              double prev = SPOT;
              double diff = 0.;
              if (interest - dividend > 0.) {
                for (int i = 0; i < 15; ++i) {
                  final int nSteps = 10 + 50 * i;
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double resDiv = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  diff = Math.abs(exactDiv - resDiv);
                  assertTrue(diff < prev);
                  prev = diff;
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
  public void priceLatticeTrinomialTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 621;
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double resDiv = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
                final double refDiv = lattice instanceof CoxRossRubinsteinLatticeSpecification ? Math.max(exactDiv, 1.) * 1.e-2 : Math.max(exactDiv, 1.) * 1.e-3;
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
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 177;
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final GreekResultCollection resDiv = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                final double priceDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) * 1.e-2;
                assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                final double deltaDiv = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 0.1) * 1.e-1;
                assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                final double gammaDiv = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                final double refGammaDiv = Math.max(Math.abs(gammaDiv), 0.1) * 1.e-1;
                assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                final double thetaDiv = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refThetaDiv = Math.max(Math.abs(thetaDiv), 0.1) * 1.e-1;
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
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {31, 115, 301 };
              for (final int nSteps : choicesSteps) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final double exactDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double resDiv = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
                  final double refDiv = (lattice instanceof LeisenReimerLatticeSpecification) ? Math.max(exactDiv, 1.) / nSteps / nSteps : Math.max(exactDiv, 1.) / Math.sqrt(nSteps);
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
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {5., 10., 8. };
    final double[] dividendTimes = new double[] {TIME / 9., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {33, 115 };
              for (final int nSteps : choicesSteps) {
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
                final double df = Math.exp(-interest * TIME);
                final double resSpot = SPOT * Math.exp(interest * TIME) * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
                final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                    Math.exp(-interest * dividendTimes[2]);
                final double exactProp = df * BlackFormulaRepository.price(resSpot, strike, TIME, vol, isCall);
                final double appCash = BlackScholesFormulaRepository.price(modSpot, strike, TIME, vol, interest, interest, isCall);
                final double resProp = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);
                final double refProp = Math.max(exactProp, 1.) / Math.sqrt(nSteps);
                assertEquals(resProp, exactProp, refProp);
                final double resCash = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                final double refCash = Math.max(appCash, 1.) / Math.sqrt(nSteps);
                assertEquals(resCash, appCash, refCash);

                if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                    lattice instanceof TianLatticeSpecification) {
                  final double resPropTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, propDividend);
                  final double resCashTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
                  assertEquals(resPropTrinomial, resProp, Math.max(resProp, 1.) / Math.sqrt(nSteps));
                  assertEquals(resCashTrinomial, resCash, Math.max(resCash, 1.) / Math.sqrt(nSteps));
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
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int[] choicesSteps = new int[] {31, 112, 301 };
              for (final int nSteps : choicesSteps) {
                for (final double dividend : DIVIDENDS) {
                  final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                  final GreekResultCollection resDiv = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                  final double priceDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) / Math.sqrt(nSteps);
                  assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                  final double deltaDiv = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) / Math.sqrt(nSteps);
                  assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                  final double gammaDiv = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                  final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) / Math.sqrt(nSteps);
                  assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                  final double thetaDiv = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                  final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) / Math.sqrt(nSteps);
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
  public void greekLeisenReimerTest() {
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            final int[] choicesSteps = new int[] {31, 109, 301 };
            for (final int nSteps : choicesSteps) {
              for (final double dividend : DIVIDENDS) {
                final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
                final GreekResultCollection resDiv = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
                final double priceDiv = BlackScholesFormulaRepository.price(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refPriceDiv = Math.max(Math.abs(priceDiv), 1.) / nSteps;
                assertEquals(resDiv.get(Greek.FAIR_PRICE), priceDiv, refPriceDiv);
                final double deltaDiv = BlackScholesFormulaRepository.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refDeltaDiv = Math.max(Math.abs(deltaDiv), 1.) / nSteps;
                assertEquals(resDiv.get(Greek.DELTA), deltaDiv, refDeltaDiv);
                final double gammaDiv = BlackScholesFormulaRepository.gamma(SPOT, strike, TIME, vol, interest, interest - dividend);
                final double refGammaDiv = Math.max(Math.abs(gammaDiv), 1.) / nSteps;
                assertEquals(resDiv.get(Greek.GAMMA), gammaDiv, refGammaDiv);
                final double thetaDiv = BlackScholesFormulaRepository.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
                final double refThetaDiv = Math.max(Math.abs(thetaDiv), 1.) / nSteps;
                assertEquals(resDiv.get(Greek.THETA), thetaDiv, refThetaDiv * 10.);
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
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.01, 0.01, 0.01 };
    final double[] cashDividends = new double[] {5., 10., 8. };
    final double[] dividendTimes = new double[] {TIME / 420., TIME / 203., TIME / 2. };

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

              final OptionFunctionProvider1D function = new EuropeanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);
              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);

              assertEquals(resProp.get(Greek.FAIR_PRICE), exactPriceProp, Math.max(1., Math.abs(exactPriceProp)) * 1.e-2);
              assertEquals(resProp.get(Greek.DELTA), exactDeltaProp, Math.max(1., Math.abs(exactDeltaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.GAMMA), exactGammaProp, Math.max(1., Math.abs(exactGammaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.THETA), exactThetaProp, Math.max(1., Math.abs(exactThetaProp)) * 1.e-1);

              assertEquals(resCash.get(Greek.FAIR_PRICE), appPriceCash, Math.max(1., Math.abs(appPriceCash)) * 1.e-2);
              assertEquals(resCash.get(Greek.DELTA), appDeltaCash, Math.max(1., Math.abs(appDeltaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.GAMMA), appGammaCash, Math.max(1., Math.abs(appGammaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.THETA), appThetaCash, Math.max(1., Math.abs(appThetaCash)));

              if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                  lattice instanceof TianLatticeSpecification) {
                final GreekResultCollection resPropTrinomial = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
                final GreekResultCollection resCashTrinomial = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);

                assertEquals(resPropTrinomial.get(Greek.FAIR_PRICE), resProp.get(Greek.FAIR_PRICE), Math.max(1., Math.abs(resProp.get(Greek.FAIR_PRICE))) * 1.e-2);
                assertEquals(resPropTrinomial.get(Greek.DELTA), resProp.get(Greek.DELTA), Math.max(1., Math.abs(resProp.get(Greek.DELTA))) * 1.e-2);

                assertEquals(resPropTrinomial.get(Greek.GAMMA), resProp.get(Greek.GAMMA), Math.max(1., Math.abs(resProp.get(Greek.GAMMA))) * 1.e-2);
                assertEquals(resPropTrinomial.get(Greek.THETA), resProp.get(Greek.THETA), Math.max(1., Math.abs(resProp.get(Greek.THETA))) * 1.e-1);

                assertEquals(resCashTrinomial.get(Greek.FAIR_PRICE), resCash.get(Greek.FAIR_PRICE), Math.max(1., Math.abs(resCash.get(Greek.FAIR_PRICE))) * 1.e-2);
                assertEquals(resCashTrinomial.get(Greek.DELTA), resCash.get(Greek.DELTA), Math.max(1., Math.abs(resCash.get(Greek.DELTA))) * 1.e-2);
                assertEquals(resCashTrinomial.get(Greek.GAMMA), resCash.get(Greek.GAMMA), Math.max(1., Math.abs(resCash.get(Greek.GAMMA))) * 1.e-2);
                assertEquals(resCashTrinomial.get(Greek.THETA), resCash.get(Greek.THETA), Math.max(1., Math.abs(resCash.get(Greek.THETA))) * 1.e-1);
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
    final int steps = 701;
    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 661;
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

          final OptionFunctionProvider1D functionVanilla = new EuropeanVanillaOptionFunctionProvider(strike, time, steps, isCall);
          final double resPrice = _model.getPrice(functionVanilla, SPOT, vol, rate, dividend);
          final GreekResultCollection resGreeks = _model.getGreeks(functionVanilla, SPOT, vol, rate, dividend);

          final double resPriceConst = _model.getPrice(lattice1, functionVanilla, SPOT, volRef, rateRef, dividend[0]);
          final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, functionVanilla, SPOT, volRef, rateRef, dividend[0]);
          assertEquals(resPrice, resPriceConst, Math.max(Math.abs(resPriceConst), .1) * 1.e-1);
          assertEquals(resGreeks.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));

          final OptionFunctionProvider1D functionTri = new EuropeanVanillaOptionFunctionProvider(strike, time, stepsTri, isCall);
          final double resPriceTrinomial = _modelTrinomial.getPrice(functionTri, SPOT, volTri, rateTri, dividendTri);
          assertEquals(resPriceTrinomial, resPriceConst, Math.max(Math.abs(resPriceConst), .1) * 1.e-1);
          final GreekResultCollection resGreeksTrinomial = _modelTrinomial.getGreeks(functionTri, SPOT, volTri, rateTri, dividendTri);
          assertEquals(resGreeksTrinomial.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));
        }
      }
    }
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeTest() {
    new EuropeanVanillaOptionFunctionProvider(-100., 1., 53, true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeTest() {
    new EuropeanVanillaOptionFunctionProvider(100., -1., 53, true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void smallNumberOfStepsTest() {
    new EuropeanVanillaOptionFunctionProvider(100., 1., 2, true);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider1D ref = new EuropeanVanillaOptionFunctionProvider(100., 1., 53, true);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new EuropeanVanillaOptionFunctionProvider(100., 1., 53, true),
        new EuropeanVanillaOptionFunctionProvider(100., 1., 53, false), new EuropeanVanillaOptionFunctionProvider(110., 1., 53, true), new EuropeanVanillaOptionFunctionProvider(100., 2., 53, true),
        new EuropeanVanillaOptionFunctionProvider(100., 2., 54, true), new AmericanVanillaOptionFunctionProvider(100., 1., 53, true), null };
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

}
