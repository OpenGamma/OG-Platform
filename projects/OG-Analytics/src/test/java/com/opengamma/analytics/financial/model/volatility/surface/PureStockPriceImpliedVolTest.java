/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;

/**
 * The stock price, S_t, is given by S_t = (F_t-D_t)X_t + D_t where F_t is the forward, D_t is the discounted value of future dividend payments
 * and X_t is the "pure stock price" process. See Buehler, Hans. Volatility and Dividends 
 */
@Test(groups = TestGroup.UNIT)
public class PureStockPriceImpliedVolTest {

  private static final Integrator1D<Double, Double> DEFAULT_INTEGRATOR = new RungeKuttaIntegrator1D();

  private static final double SPOT = 100.;
  private static final double RISK_FREE_RATE = 0.04;
  private static final double PURE_STOCK_VOL = 0.4;

  private static int N_DIVS = 10;
  private static final double[] TAU = new double[N_DIVS];
  private static final double[] ALPHA = new double[N_DIVS];
  private static final double[] BETA = new double[N_DIVS];

  private static final Function1D<Double, Double> R;
  private static final Function1D<Double, Double> D;
  private static final Function1D<Double, Double> F;

  private static final Surface<Double, Double, Double> OTM_PRICE_SURFACE;

  static {
    for (int i = 0; i < N_DIVS; i++) {
      double t = 0.1 + 0.5 * i;
      TAU[i] = t;
    }
    //  Arrays.fill(ALPHA, 0.1);
    Arrays.fill(ALPHA, 0, N_DIVS, 1.0);
    Arrays.fill(BETA, 0, N_DIVS, 0.01);
    //    for (int i = 5; i < 10; i++) {
    //      ALPHA[i] = 2.0 * (9. - i) / 5.;
    //      BETA[i] = 0.02 * (i - 5.) / 5.;
    //    }
    //    Arrays.fill(BETA, 10, N_DIVS, 0.02);
    R = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        int index = 0;
        double prod = Math.exp(t * RISK_FREE_RATE);
        while (index < N_DIVS && t >= TAU[index]) {
          prod *= (1 - BETA[index]);
          index++;
        }
        return prod;
      }
    };

    D = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        for (int index = 0; index < N_DIVS; index++) {
          if (TAU[index] > t) {
            sum += ALPHA[index] / R.evaluate(TAU[index]);
          }
        }
        return sum * r_t;
      }
    };

    F = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        for (int index = 0; index < N_DIVS; index++) {
          if (TAU[index] <= t) {
            sum += ALPHA[index] / R.evaluate(TAU[index]);
          }
        }
        return r_t * (SPOT - sum);
      }
    };

    Function<Double, Double> price = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final boolean isCall = k > f;
        final double x = (k - d) / (f - d);

        final double result = BlackFormulaRepository.price(1.0, x, t, PURE_STOCK_VOL, isCall) * (f - d);
        return result;
      }
    };

    OTM_PRICE_SURFACE = FunctionalDoublesSurface.from(price);

  }

  @Test(enabled = false)
  public void printForward() {
    System.out.println("PureStockPriceImpliedVolTest.printForward");
    for (int i = 0; i < 101; i++) {
      double t = 0.7 * i / 100.;
      double f = F.evaluate(t);
      double d = D.evaluate(t);
      double r = R.evaluate(t);
      System.out.println(t + "\t" + f + "\t" + d + "\t" + r);
    }
  }

  @Test(enabled = false)
  public void testFlatImpledVol() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatImpledVol");
    final double impVol = 0.4;
    final Function<Double, Double> pureImpVolFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final boolean isCall = x > 1.0;
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final double k = (f - d) * x + d;
        final double price = BlackFormulaRepository.price(f, k, t, impVol, isCall) / (f - d);
        return BlackFormulaRepository.impliedVolatility(price, 1.0, x, t, isCall);
      }
    };

    final Surface<Double, Double, Double> pureImpVolSurface = FunctionalDoublesSurface.from(pureImpVolFunc);
    PDEUtilityTools.printSurface("pure implied vol", pureImpVolSurface, 0.01, 2.0, 0.5, 2.0);

  }

  @Test(enabled = false)
  public void testFlatPureImpledVol() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double pureImpVol = 0.4;
    final Function<Double, Double> impVolFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final boolean isCall = k > f;
        final double x = (k - d) / (f - d);

        final double price = BlackFormulaRepository.price(1.0, x, t, pureImpVol, isCall) * (f - d);
        return BlackFormulaRepository.impliedVolatility(price, f, k, t, isCall);
      }
    };

    final Surface<Double, Double, Double> impVolSurface = FunctionalDoublesSurface.from(impVolFunc);
    PDEUtilityTools.printSurface("implied vol", impVolSurface, 0.01, 2.0, 30, 200);

  }

  @Test(enabled = false)
  public void testFlatPureImpledVol2() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double pureImpVol = 0.4;
    final Function<Double, Double> impVolFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        final double t = tx[0];
        final double k = tx[1];
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);

        return (k - d) / k * pureImpVol;
      }
    };

    final Surface<Double, Double, Double> impVolSurface = FunctionalDoublesSurface.from(impVolFunc);
    PDEUtilityTools.printSurface("implied vol", impVolSurface, 0.01, 0.2, 20, 200);

  }

  @Test(enabled = false)
  public void calandarSpreadTest() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double pureImpVol = 0.4;
    final double kM = 110;
    final double t = 0.1;
    final double dt = 1e-12;
    //before
    final double fM = F.evaluate(t - dt);
    final double dM = D.evaluate(t - dt);
    final double xM = (kM - dM) / (fM - dM);
    final double ppM = BlackFormulaRepository.price(1.0, xM, t - dt, pureImpVol, true);
    final double pM = (fM - dM) * ppM;
    final double pivM = BlackFormulaRepository.impliedVolatility(ppM, 1.0, xM, t - dt, true);
    final double ivM = BlackFormulaRepository.impliedVolatility(pM, fM, kM, t - dt, true);
    //after
    final double k = (1 - BETA[0]) * kM - ALPHA[0];
    final double f = F.evaluate(t);
    final double d = D.evaluate(t);
    final double x = (k - d) / (f - d);
    final double x2 = (kM - d) / (f - d);
    System.out.println("x: " + x + "\t" + x2);
    final double pp = BlackFormulaRepository.price(1.0, x, t, pureImpVol, true);
    final double p = pp * (f - d);
    final double piv = BlackFormulaRepository.impliedVolatility(pp, 1.0, x, t, true);
    final double iv = BlackFormulaRepository.impliedVolatility(p, f, k, t, true);
    System.out.println(fM + "\t" + f + "|\t" + ppM + "\t" + pp + "|\t" + (1 - BETA[0]) * pM + "\t" + p + "|\t" + pivM + "\t" + piv + "|\t" + ivM + "\t" + iv);
    final double delta = BlackFormulaRepository.delta(fM, kM, t - dt, 0.37, true);
    System.out.println(delta);

    final double dd = BlackFormulaRepository.dualDelta(1.0, xM, t - dt, pureImpVol, true);
    final double corrDelta = ppM / (fM - dM) - xM * dd;
    System.out.println(corrDelta);
  }

  @Test(enabled = false)
  public void calandarSpreadTest2() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double impVol = 0.4;
    final double kM = 110;
    final double t = 0.1;
    final double dt = 1e-12;
    //before
    final double fM = F.evaluate(t - dt);
    final double dM = D.evaluate(t - dt);
    final double xM = (kM - dM) / (fM - dM);
    final double pM = BlackFormulaRepository.price(fM, kM, t - dt, impVol, true);
    final double ppM = pM / (fM - dM);
    final double ivM = BlackFormulaRepository.impliedVolatility(pM, fM, kM, t - dt, true);
    final double pivM = BlackFormulaRepository.impliedVolatility(ppM, 1.0, xM, t - dt, true);
    //after
    final double k = (1 - BETA[0]) * kM - ALPHA[0];
    final double f = F.evaluate(t);
    final double d = D.evaluate(t);
    final double x = (kM - d) / (f - d);
    final double p = BlackFormulaRepository.price(f, k, t, impVol, true);
    final double pp = p / (f - d);
    final double iv = BlackFormulaRepository.impliedVolatility(p, f, k, t, true);
    final double piv = BlackFormulaRepository.impliedVolatility(pp, 1.0, x, t, true);
    System.out.println(fM + "\t" + f + "|\t" + ppM + "\t" + pp + "|\t" + (1 - BETA[0]) * pM + "\t" + p + "|\t" + pivM + "\t" + piv + "|\t" + ivM + "\t" + iv);
    final double delta = BlackFormulaRepository.delta(fM, kM, t - dt, impVol, true);
    System.out.println(delta);
  }

  @Test(enabled = false)
  public void calandarSpreadTest3() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double t = 0.1;
    final double dt = 1e-12;
    final double p = 0.2;
    final double pM = p / (1 - BETA[0]);
    final double kM = 70;
    final double k = (1 - BETA[0]) * kM - ALPHA[0];
    final double fM = F.evaluate(t - dt);
    final double f = F.evaluate(t);
    final double ivM = BlackFormulaRepository.impliedVolatility(pM, fM, kM, t - dt, false);
    final double iv = BlackFormulaRepository.impliedVolatility(p, f, k, t, false);
    System.out.println(ivM + "\t" + iv);
  }

  @Test(enabled = false)
  public void testRoundTrip() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double impVol = 0.4;
    final Function<Double, Double> pureImpVolFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final boolean isCall = x > 1.0;
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final double k = (f - d) * x + d;
        final double price = BlackFormulaRepository.price(f, k, t, impVol, isCall) / (f - d);
        return BlackFormulaRepository.impliedVolatility(price, 1.0, x, t, isCall);
      }
    };

    final Function<Double, Double> impVolFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final boolean isCall = k > f;
        final double x = (k - d) / (f - d);

        final double price = BlackFormulaRepository.price(1.0, x, t, pureImpVolFunc.evaluate(t, x), isCall) * (f - d);
        return BlackFormulaRepository.impliedVolatility(price, f, k, t, isCall);
      }
    };

    final Surface<Double, Double, Double> impVolSurface = FunctionalDoublesSurface.from(impVolFunc);
    PDEUtilityTools.printSurface("implied vol", impVolSurface, 0.01, 3.0, 50.0, 200.0);

  }

  @Test(enabled = false)
  public void varianceSwapTest() {
    System.out.println("PureStockPriceImpliedVolTest.testFlatPureImpledVol");
    final double expiry = 0.11;

    final Function1D<Double, Double> integral = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        final double price = OTM_PRICE_SURFACE.getZValue(expiry, k);
        return price / k / k;
      }
    };

    final double f = F.evaluate(expiry);
    double var = DEFAULT_INTEGRATOR.integrate(integral, 0.01 * f, 10.0 * f);
    int index = 0;
    while (index < N_DIVS && TAU[index] <= expiry) {
      double temp = correction(index);
      System.out.println("correction " + index + " " + temp + " " + var);
      var -= temp;
      index++;
    }

    var *= 2 / expiry;
    System.out.println("k:" + Math.sqrt(var));
  }

  private double correction(final int index) {
    final double expiry = TAU[index];
    final Function1D<Double, Double> integral = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        final double price = OTM_PRICE_SURFACE.getZValue(expiry, k);
        final double dPP = dPPrime(k, index);
        return price * dPP;
      }
    };
    double res = DEFAULT_INTEGRATOR.integrate(integral, 0.1, 1000.0);
    double f = F.evaluate(expiry);
    res += d(f, index);
    return res;
  }

  private double h(final double x, final int index) {
    final double a = ALPHA[index];
    final double b = BETA[index];
    return (x * b + a) / (x + a);
  }

  private double hPrime(final double x, final int index) {
    final double a = ALPHA[index];
    final double b = BETA[index];
    final double temp = x + a;
    return a * (b - 1) / temp / temp;
  }

  private double hPPrime(final double x, final int index) {
    final double a = ALPHA[index];
    final double b = BETA[index];
    final double temp = x + a;
    return 2 * a * (1 - b) / temp / temp / temp;
  }

  private double d(final double x, final int index) {
    final double a = ALPHA[index];
    final double b = BETA[index];
    final double h = h(x, index);
    return Math.log(x * (1 - b) / (x + a)) + h - 0.5 * h * h;
  }

  private double dPPrime(final double x, final int index) {
    final double h = h(x, index);
    final double hPrime = hPrime(x, index);
    final double hPPrime = hPPrime(x, index);
    final double temp = x + ALPHA[index];
    double res = -1 / x / x + 1 / temp / temp + (1 - 2 * h) * hPPrime - 2 * hPrime * hPrime;
    return res;
  }

}
