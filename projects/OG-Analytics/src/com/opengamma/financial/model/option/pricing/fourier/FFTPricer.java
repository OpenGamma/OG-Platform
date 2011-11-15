/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.number.ComplexNumber.ZERO;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.fft.JTransformsWrapper;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class FFTPricer {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final IntegralLimitCalculator LIMIT_CALCULATOR = new IntegralLimitCalculator();

  /**
   * Price a European option across a range of strikes using a FFT. The terminal price is assumed to be of the form S = F*exp(x), where F is the forward,
   * and x is a random variable with a known characteristic function.
   * @param forward The forward value of the underlying
   * @param discountFactor 
   * @param t Time to expiry
   * @param isCall true for call 
   * @param ce The Characteristic Exponent (log of characteristic function) of the returns of the underlying
   * @param lowestStrike The lowest strike to return (the actual value will depend on the set up, but is guaranteed to be less than this) 
   * @param highestStrike The highest strike to return (the actual value will depend on the set up, but is guaranteed to be greater than this) 
   * @param minStrikesDisplayed minimum number of strikes returned (actual number depends on set up) 
   * @param limitSigma An estimate of the implied vol used to calculate limits in the numerical routines 
   * @param alpha Regularization factor. Values of 0 or -1 are not allowed. -0.5 is recommended  
   * @param tol Tolerance - smaller values give higher accuracy 
   * @return array of arrays of strikes and prices 
   */
  public double[][] price(final double forward, final double discountFactor, final double t, final boolean isCall, final MartingaleCharacteristicExponent ce, final double lowestStrike,
      final double highestStrike, final int minStrikesDisplayed, final double limitSigma, final double alpha, final double tol) {

    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(tol > 0.0, "need tol > 0");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(highestStrike >= lowestStrike, "need highestStrike >= lowestStrike");
    //   Validate.isTrue(lowestStrike <= forward, "need lowestStrike <= forward");
    //    Validate.isTrue(highestStrike >= forward, "need highestStrike >= forward");
    Validate.isTrue(limitSigma > 0.0, "need limitSigma > 0");

    double kMax;
    final double limitSigmaRootT = limitSigma * Math.sqrt(t);
    final double atm = NORMAL.getCDF(limitSigmaRootT / 2.0);

    if (alpha > 0) {
      kMax = -Math.log((2 * atm - 1) * tol) / alpha;
    } else if (alpha < -1.0) {
      kMax = Math.log((2 * atm - 1) * tol) / (1 + alpha);
    } else {
      kMax = -Math.log(2 * (1 - atm) * tol) * Math.max(-1.0 / alpha, 1 / (1 + alpha));
    }

    final EuropeanCallFourierTransform psi = new EuropeanCallFourierTransform(ce);
    final Function1D<ComplexNumber, ComplexNumber> psiFunction = psi.getFunction(t);
    final double xMax = LIMIT_CALCULATOR.solve(psiFunction, alpha, tol);

    double deltaK;
    if (highestStrike == lowestStrike) {
      deltaK = Math.PI / xMax;
    } else {
      deltaK = Math.min(Math.log(highestStrike / lowestStrike) / (minStrikesDisplayed - 1), Math.PI / xMax);
    }

    final double log2 = Math.log(2);
    final int twoPow = (int) Math.ceil(Math.log(kMax / deltaK) / log2);

    final int n = (int) Math.pow(2, twoPow);
    final double delta = 2 * Math.PI / n / deltaK;
    final int m = (int) (xMax * deltaK * n / 2 / Math.PI);

    final int nLowStrikes = (int) Math.max(0, Math.ceil(Math.log(forward / lowestStrike) / deltaK));
    final int nHighStrikes = (int) Math.max(0, Math.ceil(Math.log(highestStrike / forward) / deltaK));

    return price(forward, discountFactor, t, isCall, ce, nLowStrikes, nHighStrikes, alpha, delta, n, m);
  }

  /**
   * Price a European option across a range of strikes using a FFT. The terminal price is assumed to be of the form S = F*exp(x), where F is the forward,
   * and x is a random variable with a known characteristic function.
   * @param forward The forward value of the underlying
   * @param discountFactor 
   * @param t Time to expiry
   * @param isCall true for call 
   * @param ce The Characteristic Exponent (log of characteristic function) of the returns of the underlying
   * @param nStrikes maximum number of strikes (centred around ATM) to be returned 
   * @param maxDeltaMoneyness Gives the (maximum) step size of the strikes in moneyness m = ln(K/F), where K is strike and F is forward 
   * @param limitSigma An estimate of the implied vol used to calculate limits in the numerical routines 
   * @param alpha Regularization factor. Values of 0 or -1 are not allowed. -0.5 is recommended  
   * @param tol Tolerance - smaller values give higher accuracy 
   * @return array of arrays of strikes and prices 
   */
  public double[][] price(final double forward, final double discountFactor, final double t, final boolean isCall, final MartingaleCharacteristicExponent ce, final int nStrikes,
      final double maxDeltaMoneyness,
      final double limitSigma, final double alpha,
      final double tol) {

    Validate.notNull(ce, "characteristicExponent");
    Validate.isTrue(tol > 0.0, "need tol > 0");
    Validate.isTrue(nStrikes > 0, "need number of strikes > 0");
    Validate.isTrue(maxDeltaMoneyness > 0, "need max delta moneyness > 0");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(limitSigma > 0.0, "need limitSigma > 0");

    double kMax;
    final double limitSigmaRootT = limitSigma * Math.sqrt(t);
    final double atm = NORMAL.getCDF(limitSigmaRootT / 2.0);

    if (alpha > 0) {
      kMax = -Math.log((2 * atm - 1) * tol) / alpha;
    } else if (alpha < -1.0) {
      kMax = Math.log((2 * atm - 1) * tol) / (1 + alpha);
    } else {
      kMax = -Math.log(2 * (1 - atm) * tol) * Math.max(-1.0 / alpha, 1 / (1 + alpha));
    }

    final Function1D<ComplexNumber, ComplexNumber> psi = new EuropeanCallFourierTransform(ce).getFunction(t);
    final double xMax = LIMIT_CALCULATOR.solve(psi, alpha, tol);

    final double deltaK = Math.min(maxDeltaMoneyness, Math.PI / xMax);

    final double log2 = Math.log(2);
    final int twoPow = (int) Math.ceil(Math.log(kMax / deltaK) / log2);

    final int n = (int) Math.pow(2, twoPow);
    final double delta = 2 * Math.PI / n / deltaK;
    final int m = (int) (xMax * deltaK * n / 2 / Math.PI);

    int nLowStrikes;
    int nHighStrikes;
    if (nStrikes % 2 != 0) {
      nLowStrikes = (nStrikes - 1) / 2;
      nHighStrikes = nLowStrikes;
    } else {
      nLowStrikes = nStrikes / 2;
      nHighStrikes = nLowStrikes - 1;
    }
    return price(forward, discountFactor, t, isCall, ce, nLowStrikes, nHighStrikes, alpha, delta, n, m);

  }

  /**
   * Price a European option across a range of strikes using a FFT. The terminal price is assumed to be of the form S = F*exp(x), where F is the forward,
   * and x is a random variable with a known characteristic function. <b>Note: this method is for expert use only</b>
   * @param forward The forward value of the underlying
   * @param discountFactor 
   * @param t Time to expiry
   * @param isCall true for call 
   * @param ce The Characteristic Exponent (log of characteristic function) of the returns of the underlying
   * @param nStrikesBelowATM maximum number of strikes below ATM to be returned 
   * @param nStrikesAboveATM maximum number of strikes above ATM to be returned 
   * @param alpha Regularization factor. Values of 0 or -1 are not allowed. -0.5 is recommended  
   * @param delta The spacing for sampling the function 
   * @param n The (zero padded) array of sample values. <b>Use a power of 2</b>
   * @param m The actual number of samples. Need n >= 2m-1
   * @return array of arrays of strikes and prices 
   */
  public double[][] price(final double forward, final double discountFactor, final double t, final boolean isCall, final MartingaleCharacteristicExponent ce, final int nStrikesBelowATM,
      final int nStrikesAboveATM, final double alpha, final double delta, final int n, final int m) {

    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(nStrikesBelowATM >= 0, "nStrikesBelowATM >= 0");
    Validate.isTrue(nStrikesAboveATM >= 0, "nStrikesAboveATM >= 0");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(delta > 0.0, "need delta > 0");
    Validate.isTrue(m > 0, "need m > 0");
    Validate.isTrue(n >= 2 * m - 1, "need n > 2m-1");

    final Function1D<ComplexNumber, ComplexNumber> func = new EuropeanCallFourierTransform(ce).getFunction(t);
    final int halfN = n % 2 == 0 ? n / 2 : (n + 1) / 2;
    final double a = -(halfN - 1) * delta;
    final ComplexNumber[] z = getPaddedArray(alpha, delta, n, m, func, halfN);
    final ComplexNumber[] x = JTransformsWrapper.transform1DComplex(z);
    final int nLowStrikes = Math.min(halfN, nStrikesBelowATM);
    final int nHighStrikes = Math.min(n - halfN, nStrikesAboveATM);
    final int p = 1 + nLowStrikes + nHighStrikes;
    final double[][] res = new double[p][2];
    final double deltaK = 2 * Math.PI / delta / n;
    for (int i = 0; i < nLowStrikes; i++) {
      final double k = (i - nLowStrikes) * deltaK;
      res[i][0] = forward * Math.exp(k);
      res[i][1] = discountFactor * forward * getReducedPrice(x[i + n - nLowStrikes], alpha, delta, k, a, isCall);
    }
    for (int i = nLowStrikes; i < p; i++) {
      final double k = (i - nLowStrikes) * deltaK;
      res[i][0] = forward * Math.exp(k);
      res[i][1] = discountFactor * forward * getReducedPrice(x[i - nLowStrikes], alpha, delta, k, a, isCall);
    }
    return res;
  }

  private ComplexNumber[] getPaddedArray(final double alpha, final double delta, final int n, final int m, final Function1D<ComplexNumber, ComplexNumber> func, final int halfN) {
    final ComplexNumber[] z = new ComplexNumber[n];

    final int lowerPadOutSize = halfN - m;
    final int upperPadOutSize = n - halfN + 1 - m;

    for (int i = 0; i < lowerPadOutSize; i++) {
      z[i] = ZERO;
    }

    for (int i = n - upperPadOutSize; i < n; i++) {
      z[i] = ZERO;
    }

    ComplexNumber u = new ComplexNumber(0.0, -(1 + alpha));
    final int offset = halfN - 1;
    z[offset] = func.evaluate(u);

    for (int i = 1; i < m; i++) {
      u = new ComplexNumber(i * delta, -(1 + alpha));
      final ComplexNumber f = func.evaluate(u);
      z[offset + i] = f;
      z[offset - i] = ComplexMathUtils.conjugate(f); //TODO the FFT should take care of this
    }
    return z;
  }

  private double getReducedPrice(final ComplexNumber x, final double alpha, final double delta, final double k, final double a, final boolean isCall) {
    final ComplexNumber temp = ComplexMathUtils.multiply(ComplexMathUtils.exp(new ComplexNumber(-alpha * k, -k * a)), x);
    final double y = delta * temp.getReal() / 2 / Math.PI;
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
      return y - 1 + Math.exp(k);
    } else if (alpha < -1.0) {
      return y;
    }
    return y + Math.exp(k);
  }
}
