/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MarkovChain {

  private final double _vol1;
  private final double _vol2;
  private final double _lambda12;
  private final double _lambda21;
  private final double _probState1;
  @SuppressWarnings("unused")
  private final double _pi1;

  private final MersenneTwister _rand;

  public MarkovChain(final double vol1, final double vol2, final double lambda12, final double lambda21, final double probState1) {
    this(vol1, vol2, lambda12, lambda21, probState1, MersenneTwister64.DEFAULT_SEED);
  }

  public MarkovChain(final double vol1, final double vol2, final double lambda12, final double lambda21, final double probState1, int seed) {
    Validate.isTrue(vol1 >= 0);
    Validate.isTrue(vol2 >= 0);
    Validate.isTrue(lambda12 >= 0);
    Validate.isTrue(lambda21 >= 0);
    Validate.isTrue(probState1 >= 0 && probState1 <= 1.0);
    _vol1 = vol1;
    _vol2 = vol2;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
    _probState1 = probState1;
    _pi1 = lambda21 / (lambda12 + lambda21);
    _rand = new MersenneTwister64(seed);
  }

  public double price(final double forward, final double df, final double strike, final double timeToExiry, double[] sigmas) {
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExiry, true);
    BlackPriceFunction func = new BlackPriceFunction();
    Function1D<BlackFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;
    for (double sigma : sigmas) {
      BlackFunctionData data = new BlackFunctionData(forward, df, sigma);
      sum += priceFunc.evaluate(data);
    }
    return sum / sigmas.length;
  }

  public double priceCEV(final double forward, final double df, final double strike, final double timeToExiry, final double beta, double[] sigmas) {
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExiry, true);
    CEVPriceFunction func = new CEVPriceFunction();
    Function1D<CEVFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;
    for (double sigma : sigmas) {
      CEVFunctionData data = new CEVFunctionData(forward, df, sigma, beta);
      sum += priceFunc.evaluate(data);
    }
    return sum / sigmas.length;
  }

  public double[][] price(final double[] forwards, final double[] df, final double[] strike, final double[] expiries, double[][] sigmas) {
    int nTime = forwards.length;
    int nStrikes = strike.length;
    Validate.isTrue(nTime == df.length);
    Validate.isTrue(nTime == expiries.length);
    Validate.isTrue(nTime == sigmas.length);

    BlackPriceFunction func = new BlackPriceFunction();
    double[][] price = new double[nTime][nStrikes];

    double t, k;
    for (int j = 0; j < nTime; j++) {
      t = expiries[j];
      double[] tSigmas = sigmas[j];
      for (int i = 0; i < nStrikes; i++) {
        k = strike[i];
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        Function1D<BlackFunctionData, Double> priceFunc = func.getPriceFunction(option);
        double sum = 0;
        for (double sigma : tSigmas) {
          BlackFunctionData data = new BlackFunctionData(forwards[j], df[j], sigma);
          sum += priceFunc.evaluate(data);
        }
        price[j][i] = sum / tSigmas.length;
      }

    }

    return price;
  }

  public double[] getMoments(double t, double[] sigmas) {
    double sum1 = 0;
    double sum2 = 0;
    double sum3 = 0;
    for (double sigma : sigmas) {
      double var = sigma * sigma;
      sum1 += var;
      sum2 += var * var;
      sum3 += var * var * var;
    }

    int n = sigmas.length;
    double m1 = sum1 / n;
    double m2 = (sum2 - n * m1 * m1) / (n - 1);
    double m3 = (sum3 - 3 * m1 * sum2 + 2 * n * m1 * m1 * m1) / n;

    //System.out.println("MC m1: " + m1 + " m2: " + m2 + " m3: " + m3);
    return new double[] {m1, m2, m3 };
  }

  public double[] simulate(double timeToExpiry, int n) {

    double vol, lambda, tau;
    double[] vols = new double[n];
    double sum = 0;
    for (int i = 0; i < n; i++) {
      boolean state1 = _probState1 > _rand.nextDouble();
      double t = 0;
      double var = 0.0;
      while (t < timeToExpiry) {
        if (state1) {
          vol = _vol1;
          lambda = _lambda12;
        } else {
          vol = _vol2;
          lambda = _lambda21;
        }
        tau = -Math.log(_rand.nextDouble()) / lambda;
        if (t + tau < timeToExpiry) {
          var += tau * vol * vol;
          state1 = !state1;
        } else {
          var += (timeToExpiry - t) * vol * vol;
        }
        t += tau;
      }
      vols[i] = Math.sqrt(var / timeToExpiry);
      sum += var;
    }
    sum /= n;
    // debug
    // double ave = _pi1 * timeToExpiry + (probState1 - _pi1) / (_lambda12 + _lambda21) * (1 - Math.exp(-(_lambda12 + _lambda21) * timeToExpiry));
    // double exvar = _vol2 * _vol2 * timeToExpiry + (_vol1 * _vol1 - _vol2 * _vol2) * ave;
    // System.out.println("debug " + "\t" + sum + "\t" + exvar);
    return vols;
  }

  public double[][] simulate(double[] expiries, int n) {
    return simulate(expiries, n, 0.0, 1.0);
  }

  public double[][] simulate(final double[] expiries, final int n, final double a, final double b) {
    Validate.notNull(expiries);
    Validate.isTrue(b > a, "need b > a");
    Validate.isTrue(a >= 0.0, "Nedd a >= 0.0");
    Validate.isTrue(b <= 1.0, "Nedd b <= 1.0");
    int m = expiries.length;
    Validate.isTrue(m > 0);
    for (int j = 1; j < m; j++) {
      Validate.isTrue(expiries[j] > expiries[j - 1]);
    }

    double vol, lambda, tau;
    double[][] vols = new double[m][n];
    // double[] sum = new double[m];

    for (int i = 0; i < n; i++) {
      int j = 0;
      boolean state1 = _probState1 > _rand.nextDouble();
      double t = 0;
      double var = 0.0;
      while (j < m && t < expiries[m - 1]) {
        if (state1) {
          vol = _vol1;
          lambda = _lambda12;
        } else {
          vol = _vol2;
          lambda = _lambda21;
        }
        //        if (t == 0) {
        //          double x = (0.5 + i) / n;
        //          tau = -Math.log(x) / lambda;
        //        } else {
        if (t == 0) {
          tau = -Math.log(a + (b - a) * _rand.nextDouble()) / lambda;
        } else {
          tau = -Math.log(_rand.nextDouble()) / lambda;
        }

        state1 = !state1;
        t += tau;
        if (t < expiries[j]) {
          var += tau * vol * vol;
        } else {
          var += (expiries[j] - t + tau) * vol * vol;
          //  sum[j] += var;
          vols[j][i] = Math.sqrt(var / expiries[j]);
          j++;

          while (j < m && t > expiries[j]) {
            var += (expiries[j] - expiries[j - 1]) * vol * vol;
            // sum[j] += var;
            vols[j][i] = Math.sqrt(var / expiries[j]);
            j++;
          }

          var += (t - expiries[j - 1]) * vol * vol;
        }
      }
    }

    return vols;
  }
}
