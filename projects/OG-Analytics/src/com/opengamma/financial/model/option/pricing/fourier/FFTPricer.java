/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.fft.JTransformsWrapper;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class FFTPricer {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  // private final double _alpha;
  // private final double _delta;
  // private final int _m;
  // private final int _n;
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();

  /**
   * Price a European option across a range of strikes using a FFT. The terminal price is assumed to be of the form S = F*exp(x), where F is the forward,
   * and x is a random variable with a known characteristic function.
   * @param forward The forward value of the underlying 
   * @param maturity The option maturity 
   * @param discountFactor Discount factor
   * @param isCall true for call, false for put
   * @param ce The Characteristic Exponent (log of characteristic function) of the returns of the underlying
   * @param nStrikes maximum number of strikes (centred around ATM) to be returned 
   * @param maxDeltaMoneyness Gives the (maximum) step size of the strikes in moneyness m = ln(K/F), where K is strike and F is forward 
   * @param alpha Regularization factor. Values of 0 or -1 are. -0.5 is recommended  
   * @param tol Tolerance - smaller values give more accuracy 
   * @param limitSigma Approximate Balck vol - used to calculate size of FFT
   * @return array of arrays of strikes and prices 
   */
  public double[][] price(final double forward, final double maturity, final double discountFactor, final boolean isCall, final CharacteristicExponent ce, final int nStrikes,
      final double maxDeltaMoneyness, final double alpha, final double tol, final double limitSigma) {
    Validate.isTrue(tol > 0.0, "need tol > 0");
    Validate.isTrue(limitSigma > 0.0, "need limitSigma");
    double kMax;
    final double limitSigmaRootT = limitSigma * Math.sqrt(maturity);
    double atm = NORMAL.getCDF(limitSigmaRootT / 2.0);

    if (alpha > 0) {
      kMax = -Math.log((2 * atm - 1) * tol) / alpha;
    } else if (alpha < -1.0) {
      kMax = Math.log((2 * atm - 1) * tol) / (1 + alpha);
    } else {
      kMax = -Math.log(2 * (1 - atm) * tol) * Math.max(-1.0 / alpha, 1 / (1 + alpha));
    }

    Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return Math.exp(-limitSigmaRootT * limitSigmaRootT * x * x / 2) / x / x - Math.abs(tol / alpha / (1 + alpha));
      }
    };

    final double[] range = s_bracketRoot.getBracketedPoints(func, 1.0, 100.0);
    double xMax = s_root.getRoot(func, range[0], range[1]);

    double deltaK = Math.min(maxDeltaMoneyness, Math.PI / xMax);

    double log2 = Math.log(2);
    int twoPow = (int) Math.ceil(Math.log(kMax / deltaK) / log2);

    int n = (int) Math.pow(2, twoPow);
    double delta = 2 * Math.PI / n / deltaK;
    int m = (int) (xMax * deltaK * n / 2 / Math.PI);

    return price(forward, maturity, discountFactor, isCall, ce, nStrikes, alpha, delta, n, m);

  }

  /**
   * Price a European option across a range of strikes using a FFT. The terminal price is assumed to be of the form S = F*exp(x), where F is the forward,
   * and x is a random variable with a known characteristic function. <b>Note: this method is for expert use only</b>
   * @param forward The forward value of the underlying 
   * @param maturity The option maturity 
   * @param discountFactor Discount factor
   * @param isCall true for call, false for put
   * @param ce The Characteristic Exponent (log of characteristic function) of the returns of the underlying
   * @param nStrikes maximum number of strikes (centred around ATM) to be returned 
   * @param alpha Regularization factor. Values of 0 or -1 are. -0.5 is recommended  
   * @param delta The spacing for sampling the function 
   * @param n The (zero padded) array of sample values. <b>Use a power of 2</b>
   * @param m The actual number of samples. Need n >= 2m-1
   * @return array of arrays of strikes and prices 
   */
  public double[][] price(final double forward, final double maturity, final double discountFactor, final boolean isCall, final CharacteristicExponent ce, final int nStrikes, final double alpha,
      final double delta, final int n, final int m) {

    Validate.isTrue(maturity > 0.0, "need maturity > 0");
    Validate.notNull(ce, "null Characteristic Exponent");
    Validate.isTrue(nStrikes > 0, "need at least one strike");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(delta > 0.0, "need delta > 0");
    Validate.isTrue(n > 0, "need n > 0");
    Validate.isTrue(m > 0, "need m > 0");
    Validate.isTrue(n >= 2 * m - 1, "need n > 2m-1");

    Function1D<ComplexNumber, ComplexNumber> func = new EuropeanCallFT(ce, maturity);

    int halfN;
    if (n % 2 == 0) {
      halfN = n / 2;
    } else {
      halfN = (n + 1) / 2;
    }

    double a = -(halfN - 1) * delta;
    ComplexNumber[] z = new ComplexNumber[n];

    int lowerPadOutSize = halfN - m;
    int upperPadOutSize = n - halfN + 1 - m;

    for (int i = 0; i < lowerPadOutSize; i++) {
      z[i] = new ComplexNumber(0.0);
    }

    for (int i = n - upperPadOutSize; i < n; i++) {
      z[i] = new ComplexNumber(0.0);
    }

    ComplexNumber u = new ComplexNumber(0.0, -(1 + alpha));
    int offset = halfN - 1;
    z[offset] = func.evaluate(u);

    for (int i = 1; i < m; i++) {
      u = new ComplexNumber(i * delta, -(1 + alpha));
      ComplexNumber f = func.evaluate(u);
      z[offset + i] = f;
      z[offset - i] = ComplexMathUtils.conjugate(f);
    }

    ComplexNumber[] x = JTransformsWrapper.transform1DComplex(z);

    int p = Math.min(n, nStrikes);

    double[][] res = new double[p][2];

    double deltaK = 2 * Math.PI / delta / n;

    for (int i = 0; i < p / 2; i++) {
      double k = (i - p / 2) * deltaK;
      res[i][0] = forward * Math.exp(k);
      res[i][1] = discountFactor * forward * getReducedPrice(x[i + n - p / 2], alpha, delta, k, a, isCall);
    }
    for (int i = p / 2; i < p; i++) {
      double k = (i - p / 2) * deltaK;
      res[i][0] = forward * Math.exp(k);
      res[i][1] = discountFactor * forward * getReducedPrice(x[i - p / 2], alpha, delta, k, a, isCall);
    }
    return res;
  }

  private double getReducedPrice(final ComplexNumber x, final double alpha, final double delta, final double k, final double a, final boolean isCall) {

    ComplexNumber temp = ComplexMathUtils.multiply(ComplexMathUtils.exp(new ComplexNumber(-alpha * k, -k * a)), x);

    double y = delta * temp.getReal() / 2 / Math.PI;
    if (isCall) {
      if (alpha > 0.0) {
        return y;
      } else if (alpha < -1.0) {
        return y + 1 - Math.exp(k);
      } else {
        return y + 1;
      }
    }
    if (alpha > 0.0) {
      return y - 1 - Math.exp(k);
    } else if (alpha < -1.0) {
      return y;
    } else {
      return y + Math.exp(k);
    }
  }
}
