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
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleBarrierOptionFunctionProviderTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 114. };
  private static final double TIME = 4.2;
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] INTERESTS = new double[] {0.015, 0.05 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.01 };

  /**
   * 
   */
  @Test
  public void priceTestTrinomial() {
    final LatticeSpecification lattice = new TianLatticeSpecification();
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 3585;
    final double lower = 85.;
    final double upper = 135.;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : vols) {
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower, upper,
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              double exact = price(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double res = _modelTrinomial.getPrice(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res, exact, Math.max(exact, .1) * 1.e-1);
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
    final LatticeSpecification lattice = new TianLatticeSpecification();
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 3585;
    final double lower = 85.;
    final double upper = 135.;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : vols) {
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower, upper,
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              final double price = price(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double delta = delta(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double deltaSpotUp = delta(SPOT + eps, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double deltaSpotDown = delta(SPOT - eps, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double priceTimeUp = price(SPOT, strike, TIME + eps, vol, interest, dividend, isCall, lower, upper);
              final double priceTimeDown = price(SPOT, strike, TIME - eps, vol, interest, dividend, isCall, lower, upper);
              final double gamma = 0.5 * (deltaSpotUp - deltaSpotDown) / eps;
              final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;

              final GreekResultCollection res = _modelTrinomial.getGreeks(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, .1) * 1.e-1);
              assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, .1) * 1.e-1);
              assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, .1) * 1.e-1);
              assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-1);
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
  public void putCallSymmetryTest() {
    /*
     * Two sample lattices are checked 
     */
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final int steps = 401;
    final double lower = 90.;
    final double upper = 120.;
    DoubleBarrierOptionFunctionProvider.BarrierTypes out = DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut");
    for (final double strike : STRIKES) {
      final OptionFunctionProvider1D functionCall = new DoubleBarrierOptionFunctionProvider(strike, TIME, steps, true, lower, upper, out);
      final OptionFunctionProvider1D functionPut = new DoubleBarrierOptionFunctionProvider(SPOT * SPOT / strike, TIME, steps, false, SPOT * SPOT / upper, SPOT * SPOT / lower, out);
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
              assertEquals(priceCall, refPrice, Math.max(refPrice, 0.1) * 1.e-5);
              assertEquals(thetaCall, refTheta, Math.max(Math.abs(refTheta), 0.1) * 1.e-2);
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
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 3321;
    final double lower = 85.;
    final double upper = 135.;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : vols) {
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower, upper,
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              double exact = price(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res, exact, Math.max(exact, .1) * 1.e-1);
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
  public void priceOutOfRangeTest() {
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 11;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      final double strike = isCall ? 115. : 95.;
      final double[] lower = new double[] {85., 112., isCall ? 80. : 96. };
      final double[] upper = new double[] {101., 130., isCall ? 114. : 130. };
      for (final double interest : INTERESTS) {
        for (final double vol : vols) {
          for (final double dividend : DIVIDENDS) {
            for (int i = 0; i < lower.length; ++i) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower[i], upper[i],
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              final double res = _model.getPrice(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res, 0.);
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
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 3321;
    final double lower = 85.;
    final double upper = 135.;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : vols) {
            for (final double dividend : DIVIDENDS) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower, upper,
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              final double price = price(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double delta = delta(SPOT, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double deltaSpotUp = delta(SPOT + eps, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double deltaSpotDown = delta(SPOT - eps, strike, TIME, vol, interest, dividend, isCall, lower, upper);
              final double priceTimeUp = price(SPOT, strike, TIME + eps, vol, interest, dividend, isCall, lower, upper);
              final double priceTimeDown = price(SPOT, strike, TIME - eps, vol, interest, dividend, isCall, lower, upper);
              final double gamma = 0.5 * (deltaSpotUp - deltaSpotDown) / eps;
              final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;

              final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, .1) * 1.e-1);
              assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, .1) * 1.e-1);
              assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, .1) * 1.e-1);
              assertEquals(res.get(Greek.THETA), theta, Math.max(theta, .1) * 1.e-1);
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
  public void greeksOutOfRangeTest() {
    final double eps = 1.e-6;
    /*
     * Due to slow convergence, only one lattice is used in this test
     */
    final LatticeSpecification lattice = new LeisenReimerLatticeSpecification();
    final double[] vols = new double[] {0.15, 0.25 };

    final int nSteps = 21;
    final boolean[] tfSet = new boolean[] {true, false };
    final String type = "DoubleKnockOut";
    for (final boolean isCall : tfSet) {
      final double strike = isCall ? 115. : 95.;
      final double[] lower = new double[] {85., 112., isCall ? 80. : 96. };
      final double[] upper = new double[] {101., 130., isCall ? 114. : 130. };
      for (final double interest : INTERESTS) {
        for (final double vol : vols) {
          for (final double dividend : DIVIDENDS) {
            for (int i = 0; i < lower.length; ++i) {
              final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, nSteps, isCall, lower[i], upper[i],
                  DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf(type));
              final double price = price(SPOT, strike, TIME, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double delta = delta(SPOT, strike, TIME, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double deltaSpotUp = delta(SPOT + eps, strike, TIME, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double deltaSpotDown = delta(SPOT - eps, strike, TIME, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double priceTimeUp = price(SPOT, strike, TIME + eps, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double priceTimeDown = price(SPOT, strike, TIME - eps, vol, interest, dividend, isCall, lower[i], upper[i]);
              final double gamma = 0.5 * (deltaSpotUp - deltaSpotDown) / eps;
              final double theta = -0.5 * (priceTimeUp - priceTimeDown) / eps;

              final GreekResultCollection res = _model.getGreeks(lattice, function, SPOT, vol, interest, dividend);
              assertEquals(res.get(Greek.FAIR_PRICE), price, Math.max(price, 1.) * 1.e-3);
              assertEquals(res.get(Greek.DELTA), delta, Math.max(delta, 1.) * 1.e-3);
              assertEquals(res.get(Greek.GAMMA), gamma, Math.max(gamma, 1.) * 1.e-3);
              assertEquals(res.get(Greek.THETA), theta, Math.max(theta, 1.) * 1.e-3);
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
  public void binomialTrinomialDiscreteDividendTest() {
    final LatticeSpecification lattice = new TianLatticeSpecification();

    final double time = 5.;
    final double[] propDividends = new double[] {0.05, 0.06, 0.05 };
    final double[] cashDividends = new double[] {3., 2., 4.5 };
    final double[] dividendTimes = new double[] {time / 6., time / 3., time / 2. };
    final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
    final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
    final int steps = 139;
    final int stepsTri = 114;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      final double strike = isCall ? 115. : 95.;
      final double[] lower = new double[] {85., isCall ? 95. : 80. };
      final double[] upper = new double[] {125., isCall ? 130. : 115. };
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (int i = 0; i < lower.length; ++i) {
            final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(strike, TIME, steps, isCall, lower[i], upper[i],
                DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
            final double priceCash = _model.getPrice(lattice, function, SPOT, vol, interest, cashDividend);
            final double priceProp = _model.getPrice(lattice, function, SPOT, vol, interest, propDividend);
            final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);
            final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
            final OptionFunctionProvider1D functionTri = new DoubleBarrierOptionFunctionProvider(strike, TIME, stepsTri, isCall, lower[i], upper[i],
                DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
            final double priceCashTrinomial = _modelTrinomial.getPrice(lattice, functionTri, SPOT, vol, interest, cashDividend);
            final double pricePropTrinomial = _modelTrinomial.getPrice(lattice, functionTri, SPOT, vol, interest, propDividend);
            final GreekResultCollection resCashTrinomial = _modelTrinomial.getGreeks(lattice, functionTri, SPOT, vol, interest, cashDividend);
            final GreekResultCollection resPropTrinomial = _modelTrinomial.getGreeks(lattice, functionTri, SPOT, vol, interest, propDividend);

            assertEquals(resCash.get(Greek.FAIR_PRICE), priceCash, 1.e-14);
            assertEquals(resCashTrinomial.get(Greek.FAIR_PRICE), priceCashTrinomial, 1.e-14);
            assertEquals(resCash.get(Greek.FAIR_PRICE), resCashTrinomial.get(Greek.FAIR_PRICE), Math.max(Math.abs(resCashTrinomial.get(Greek.FAIR_PRICE)), 1.) * 1.e-1);
            assertEquals(resCash.get(Greek.DELTA), resCashTrinomial.get(Greek.DELTA), Math.max(Math.abs(resCashTrinomial.get(Greek.DELTA)), 1.) * 1.e-1);
            assertEquals(resCash.get(Greek.GAMMA), resCashTrinomial.get(Greek.GAMMA), Math.max(Math.abs(resCashTrinomial.get(Greek.GAMMA)), 1.) * 1.e-1);
            assertEquals(resCash.get(Greek.THETA), resCashTrinomial.get(Greek.THETA), Math.max(Math.abs(resCashTrinomial.get(Greek.THETA)), 1.) * 1.);

            assertEquals(resProp.get(Greek.FAIR_PRICE), priceProp, 1.e-14);
            assertEquals(resPropTrinomial.get(Greek.FAIR_PRICE), pricePropTrinomial, 1.e-14);
            assertEquals(resProp.get(Greek.FAIR_PRICE), resPropTrinomial.get(Greek.FAIR_PRICE), Math.max(Math.abs(resPropTrinomial.get(Greek.FAIR_PRICE)), 1.) * 1.e-1);
            assertEquals(resProp.get(Greek.DELTA), resPropTrinomial.get(Greek.DELTA), Math.max(Math.abs(resPropTrinomial.get(Greek.DELTA)), 1.) * 1.e-1);
            assertEquals(resProp.get(Greek.GAMMA), resPropTrinomial.get(Greek.GAMMA), Math.max(Math.abs(resPropTrinomial.get(Greek.GAMMA)), 1.) * 1.e-1);
            assertEquals(resProp.get(Greek.THETA), resPropTrinomial.get(Greek.THETA), Math.max(Math.abs(resPropTrinomial.get(Greek.THETA)), 1.) * 1.);
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
    final int stepsTri = 621;
    final double[] volTri = new double[stepsTri];
    final double[] rateTri = new double[stepsTri];
    final double[] dividendTri = new double[stepsTri];
    final double constA = 0.01;
    final double constB = 0.001;
    final double constC = 0.1;
    final double constD = 0.05;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      final double strike = isCall ? 115. : 95.;
      final double[] lower = new double[] {85., isCall ? 95. : 80. };
      final double[] upper = new double[] {125., isCall ? 130. : 115. };
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

        for (int i = 0; i < lower.length; ++i) {
          final OptionFunctionProvider1D functionVanilla = new DoubleBarrierOptionFunctionProvider(strike, TIME, steps, isCall, lower[i], upper[i],
              DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
          final double resPrice = _model.getPrice(functionVanilla, SPOT, vol, rate, dividend);
          final GreekResultCollection resGreeks = _model.getGreeks(functionVanilla, SPOT, vol, rate, dividend);

          final double resPriceConst = _model.getPrice(lattice1, functionVanilla, SPOT, volRef, rateRef, dividend[0]);
          final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, functionVanilla, SPOT, volRef, rateRef, dividend[0]);
          assertEquals(resPrice, resPriceConst, Math.max(Math.abs(resPriceConst), .1) * 1.e-1);
          assertEquals(resGreeks.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));

          final OptionFunctionProvider1D functionTri = new DoubleBarrierOptionFunctionProvider(strike, TIME, stepsTri, isCall, lower[i], upper[i],
              DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
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

  /**
   * 
   */
  @Test
  public void getLowerBarrierTest() {
    final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 88., 121.,
        DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
    assertEquals(((DoubleBarrierOptionFunctionProvider) function).getLowerBarrier(), 88.);
  }

  /**
   * 
   */
  @Test
  public void getUpperBarrierTest() {
    final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 88., 121.,
        DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
    assertEquals(((DoubleBarrierOptionFunctionProvider) function).getUpperBarrier(), 121.);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getUnspecifiedBarrierTest() {
    final OptionFunctionProvider1D function = new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 88., 121.,
        DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
    ((DoubleBarrierOptionFunctionProvider) function).getBarrier();
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = NotImplementedException.class)
  public void DoubleKnockInTest() {
    new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 88., 121., DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockIn"));
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeUpperBarrierTest() {
    new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 88., -121., DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void smallUpperBarrierTest() {
    new DoubleBarrierOptionFunctionProvider(100., TIME, 21, true, 188., 121., DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut"));
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final DoubleBarrierOptionFunctionProvider.BarrierTypes type = DoubleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DoubleKnockOut");
    final AmericanSingleBarrierOptionFunctionProvider.BarrierTypes type1 = AmericanSingleBarrierOptionFunctionProvider.BarrierTypes.valueOf("DownAndOut");
    final OptionFunctionProvider1D ref = new DoubleBarrierOptionFunctionProvider(100., 1., 53, true, 90., 120., type);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new DoubleBarrierOptionFunctionProvider(100., 1., 53, true, 90., 120., type),
        new DoubleBarrierOptionFunctionProvider(100., 1., 53, true, 90., 121., type), new AmericanSingleBarrierOptionFunctionProvider(100., 1., 53, true, 90., type1),
        new AmericanVanillaOptionFunctionProvider(100., 1., 53, true), null };
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

  private double price(final double spot, final double strike, final double time, final double vol, final double interest, final double div, final boolean isCall,
      final double lower, final double upper) {
    final int rg = 4;
    final double sigmaRootT = vol * Math.sqrt(time);
    final double second = (interest - div + 0.5 * vol * vol) * time;
    final double mu = 2. * (interest - div) / vol / vol + 1.;

    if (spot <= lower || spot >= upper) {
      return 0.;
    }

    double tmp1 = 0.;
    double tmp2 = 0.;
    if (isCall) {
      for (int i = -rg; i < rg + 1; ++i) {
        final double d1 = (Math.log(spot * Math.pow(upper, 2. * i) / strike / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
        final double d2 = (Math.log(spot * Math.pow(upper, 2. * i - 1) / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
        final double d3 = (Math.log(Math.pow(lower, 2. * i + 2) / spot / strike / Math.pow(upper, 2. * i)) + second) / sigmaRootT;
        final double d4 = (Math.log(Math.pow(lower, 2. * i + 2.) / spot / Math.pow(upper, 2. * i + 1.)) + second) / sigmaRootT;

        tmp1 += Math.pow(upper / lower, i * mu) * (NORMAL.getCDF(d1) - NORMAL.getCDF(d2)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) * (NORMAL.getCDF(d3) - NORMAL.getCDF(d4));
        tmp2 += Math.pow(upper / lower, i * mu - i * 2.) * (NORMAL.getCDF(d1 - sigmaRootT) - NORMAL.getCDF(d2 - sigmaRootT)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
            (NORMAL.getCDF(d3 - sigmaRootT) - NORMAL.getCDF(d4 - sigmaRootT));
      }
      final double res = tmp1 * spot * Math.exp(-div * time) - tmp2 * strike * Math.exp(-interest * time);
      return res <= 0. ? 0. : res;
    }
    for (int i = -rg; i < rg + 1; ++i) {
      final double y1 = (Math.log(spot * Math.pow(upper, 2. * i) / Math.pow(lower, 2. * i + 1.)) + second) / sigmaRootT;
      final double y2 = (Math.log(spot * Math.pow(upper, 2. * i) / strike / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
      final double y3 = (Math.log(Math.pow(lower, 2. * i + 1.) / spot / Math.pow(upper, 2. * i)) + second) / sigmaRootT;
      final double y4 = (Math.log(Math.pow(lower, 2. * i + 2.) / spot / strike / Math.pow(upper, 2. * i)) + second) / sigmaRootT;

      tmp2 += Math.pow(upper / lower, i * mu - i * 2.) * (NORMAL.getCDF(y1 - sigmaRootT) - NORMAL.getCDF(y2 - sigmaRootT)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
          (NORMAL.getCDF(y3 - sigmaRootT) - NORMAL.getCDF(y4 - sigmaRootT));
      tmp1 += Math.pow(upper / lower, i * mu) * (NORMAL.getCDF(y1) - NORMAL.getCDF(y2)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) * (NORMAL.getCDF(y3) - NORMAL.getCDF(y4));
    }
    final double res = tmp2 * strike * Math.exp(-interest * time) - tmp1 * spot * Math.exp(-div * time);
    return res <= 0. ? 0. : res;
  }

  private double delta(final double spot, final double strike, final double time, final double vol, final double interest, final double div, final boolean isCall,
      final double lower, final double upper) {
    final int rg = 4;
    final double sigmaRootT = vol * Math.sqrt(time);
    final double second = (interest - div + 0.5 * vol * vol) * time;
    final double mu = 2. * (interest - div) / vol / vol + 1.;

    if (spot < lower || spot > upper) {
      return 0.;
    }

    double tmp1 = 0.;
    double tmp1Diff = 0.;
    double tmp2Diff = 0.;
    if (isCall) {
      final double d12Diff = 1. / spot / sigmaRootT;
      final double d34Diff = -d12Diff;
      for (int i = -rg; i < rg + 1; ++i) {
        final double d1 = (Math.log(spot * Math.pow(upper, 2. * i) / strike / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
        final double d2 = (Math.log(spot * Math.pow(upper, 2. * i - 1) / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
        final double d3 = (Math.log(Math.pow(lower, 2. * i + 2) / spot / strike / Math.pow(upper, 2. * i)) + second) / sigmaRootT;
        final double d4 = (Math.log(Math.pow(lower, 2. * i + 2.) / spot / Math.pow(upper, 2. * i + 1.)) + second) / sigmaRootT;

        tmp1 += Math.pow(upper / lower, i * mu) * (NORMAL.getCDF(d1) - NORMAL.getCDF(d2)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) * (NORMAL.getCDF(d3) - NORMAL.getCDF(d4));
        tmp1Diff += Math.pow(upper / lower, i * mu) * (NORMAL.getPDF(d1) - NORMAL.getPDF(d2)) * d12Diff - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) *
            (NORMAL.getPDF(d3) - NORMAL.getPDF(d4)) * d34Diff + mu * Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) / spot * (NORMAL.getCDF(d3) - NORMAL.getCDF(d4));
        tmp2Diff += Math.pow(upper / lower, i * mu - i * 2.) * (NORMAL.getPDF(d1 - sigmaRootT) - NORMAL.getPDF(d2 - sigmaRootT)) * d12Diff -
            Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
            (NORMAL.getPDF(d3 - sigmaRootT) - NORMAL.getPDF(d4 - sigmaRootT)) * d34Diff + (mu - 2.) / spot * Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
            (NORMAL.getCDF(d3 - sigmaRootT) - NORMAL.getCDF(d4 - sigmaRootT));
      }
      final double res = tmp1 * Math.exp(-div * time) + tmp1Diff * spot * Math.exp(-div * time) - tmp2Diff * strike * Math.exp(-interest * time);
      return res;
    }
    final double y12Diff = 1. / spot / sigmaRootT;
    final double y34Diff = -y12Diff;
    for (int i = -rg; i < rg + 1; ++i) {
      final double y1 = (Math.log(spot * Math.pow(upper, 2. * i) / Math.pow(lower, 2. * i + 1.)) + second) / sigmaRootT;
      final double y2 = (Math.log(spot * Math.pow(upper, 2. * i) / strike / Math.pow(lower, 2. * i)) + second) / sigmaRootT;
      final double y3 = (Math.log(Math.pow(lower, 2. * i + 1.) / spot / Math.pow(upper, 2. * i)) + second) / sigmaRootT;
      final double y4 = (Math.log(Math.pow(lower, 2. * i + 2.) / spot / strike / Math.pow(upper, 2. * i)) + second) / sigmaRootT;

      tmp2Diff += Math.pow(upper / lower, i * mu - i * 2.) * (NORMAL.getPDF(y1 - sigmaRootT) - NORMAL.getPDF(y2 - sigmaRootT)) * y12Diff -
          Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
          (NORMAL.getPDF(y3 - sigmaRootT) - NORMAL.getPDF(y4 - sigmaRootT)) * y34Diff + (mu - 2.) / spot * Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu - 2.) *
          (NORMAL.getCDF(y3 - sigmaRootT) - NORMAL.getCDF(y4 - sigmaRootT));
      tmp1Diff += Math.pow(upper / lower, i * mu) * (NORMAL.getPDF(y1) - NORMAL.getPDF(y2)) * y12Diff - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) *
          (NORMAL.getPDF(y3) - NORMAL.getPDF(y4)) * y34Diff
          + mu / spot * Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) * (NORMAL.getCDF(y3) - NORMAL.getCDF(y4));
      tmp1 += Math.pow(upper / lower, i * mu) * (NORMAL.getCDF(y1) - NORMAL.getCDF(y2)) - Math.pow(Math.pow(lower, i + 1.) / Math.pow(upper, i) / spot, mu) * (NORMAL.getCDF(y3) - NORMAL.getCDF(y4));
    }
    final double res = tmp2Diff * strike * Math.exp(-interest * time) - tmp1Diff * spot * Math.exp(-div * time) - tmp1 * Math.exp(-div * time);
    return res;
  }

  /**
   * test for analytic formula
   */
  @Test(enabled = false)
  public void functionTest() {
    final boolean[] tfSet = new boolean[] {true, false };
    final double eps = 1.e-6;
    final double lower = 85.;
    final double upper = 135.;
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              final double delta = delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall, lower, upper);
              final double upSpot = price(SPOT + eps, strike, TIME, vol, interest, interest - dividend, isCall, lower, upper);
              final double downSpot = price(SPOT - eps, strike, TIME, vol, interest, interest - dividend, isCall, lower, upper);
              assertEquals(delta, 0.5 * (upSpot - downSpot) / eps, eps);
            }
          }
        }
      }
    }
  }

  //  @Test
  //  public void test() {
  //    final double spot = 100.;
  //    final double strike = 100.;
  //    final double interest = 0.1;
  //    final double div = 0.;
  //
  //    final double[] vol = new double[] {0.15, 0.25, 0.35 };
  //    final double[] time = new double[] {0.25, 0.5 };
  //    final double[] upper = new double[] {150., 140., 130., 120., 110. };
  //    final double[] lower = new double[] {50., 60., 70., 80., 90. };
  //
  //    for (int i = 0; i < upper.length; ++i) {
  //      for (int j = 0; j < time.length; ++j) {
  //        for (int k = 0; k < vol.length; ++k) {
  //          final double callPrice = price(spot, strike, time[j], vol[k], interest, div, true, upper[i], lower[i]);
  //          System.out.print(callPrice + "\t");
  //          final double dual = price(strike, spot, time[j], vol[k], div, interest, false, spot * strike / lower[i], spot * strike / upper[i]);
  //          assertEquals(callPrice, dual, Math.max(callPrice, 1.) * 1.e-10);
  //
  //        }
  //      }
  //      System.out.print("\n");
  //    }
  //
  //  }
}
