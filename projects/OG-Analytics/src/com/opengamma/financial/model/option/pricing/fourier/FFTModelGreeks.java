/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.MINUS_I;
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
public class FFTModelGreeks {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final IntegralLimitCalculator LIMIT_CALCULATOR = new IntegralLimitCalculator();

 /**
  * 
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
  * @return an array of arrays where is first array is the strikes, the second the prices, the first the derivatives of price wrt the first parameter etc 
  */
  //TODO this is cut and paste from FFTPricer - the calculation of the sample size and spacing should be extracted 
  public double[][] getGreeks(final double forward, final double discountFactor, final double t, final boolean isCall, final MartingaleCharacteristicExponent ce, final double lowestStrike,
      final double highestStrike, final int minStrikesDisplayed, final double limitSigma, final double alpha, final double tol) {

    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(tol > 0.0, "need tol > 0");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");

    Validate.isTrue(lowestStrike <= forward, "need lowestStrike <= forward");
    Validate.isTrue(highestStrike >= forward, "need highestStrike >= forward");
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

    final int nLowStrikes = (int) Math.ceil(Math.log(forward / lowestStrike) / deltaK);
    final int nHighStrikes = (int) Math.ceil(Math.log(highestStrike / forward) / deltaK);

    return getGreeks(forward, discountFactor, t, isCall, ce, nLowStrikes, nHighStrikes, alpha, delta, n, m);
  }

  /**
   * 
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
   * @return  an array of arrays where is first array is the strikes, the second the prices, the first the derivatives of price wrt the first parameter etc 
   */
  public double[][] getGreeks(final double forward, final double discountFactor, final double t, final boolean isCall, final MartingaleCharacteristicExponent ce, final int nStrikesBelowATM,
      final int nStrikesAboveATM, final double alpha, final double delta, final int n, final int m) {

    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(nStrikesBelowATM >= 0, "nStrikesBelowATM >= 0");
    Validate.isTrue(nStrikesAboveATM >= 0, "nStrikesAboveATM >= 0");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(delta > 0.0, "need delta > 0");
    Validate.isTrue(m > 0, "need m > 0");
    Validate.isTrue(n >= 2 * m - 1, "need n > 2m-1");

    final Function1D<ComplexNumber, ComplexNumber[]> func = ce.getAdjointFunction(t);
    final int halfN = n % 2 == 0 ? n / 2 : (n + 1) / 2;
    final double a = -(halfN - 1) * delta;
    final ComplexNumber[][] z = getPaddedArrays(alpha, delta, n, m, func, halfN);
    int size = z.length;
    final ComplexNumber[][] x = new ComplexNumber[size][];
    for (int i = 0; i < size; i++) {
      x[i] = JTransformsWrapper.transform1DComplex(z[i]);
    }
    final int nLowStrikes = Math.min(halfN, nStrikesBelowATM);
    final int nHighStrikes = Math.min(n - halfN, nStrikesAboveATM);
    final int p = 1 + nLowStrikes + nHighStrikes;
    final double[][] res = new double[size + 1][p];
    final double deltaK = 2 * Math.PI / delta / n;
    for (int i = 0; i < nLowStrikes; i++) {
      final double k = (i - nLowStrikes) * deltaK;
      res[0][i] = forward * Math.exp(k);
      res[1][i] = discountFactor * forward * getReducedPrice(x[0][i + n - nLowStrikes], alpha, delta, k, a, isCall);
      for (int j = 1; j < size; j++) {
        res[j + 1][i] = discountFactor * forward * getReducedGreek(x[j][i + n - nLowStrikes], alpha, delta, k, a);
      }
    }
    for (int i = nLowStrikes; i < p; i++) {
      final double k = (i - nLowStrikes) * deltaK;
      res[0][i] = forward * Math.exp(k);
      res[1][i] = discountFactor * forward * getReducedPrice(x[0][i - nLowStrikes], alpha, delta, k, a, isCall);
      for (int j = 1; j < size; j++) {
        res[j + 1][i] = discountFactor * forward * getReducedGreek(x[j][i - nLowStrikes], alpha, delta, k, a);
      }
    }
    return res;
  }

  private ComplexNumber[][] getPaddedArrays(final double alpha, final double delta, final int n, final int m, final Function1D<ComplexNumber, ComplexNumber[]> ajointFunc, final int halfN) {
    //TODO this is a bit of a fudge 
    int size = ajointFunc.evaluate(MINUS_I).length;

    final ComplexNumber[][] z = new ComplexNumber[size][n];

    final int lowerPadOutSize = halfN - m;
    final int upperPadOutSize = n - halfN + 1 - m;

    for (int j = 0; j < size; j++) {
      for (int i = 0; i < lowerPadOutSize; i++) {
        z[j][i] = ZERO;
      }
      for (int i = n - upperPadOutSize; i < n; i++) {
        z[j][i] = ZERO;
      }
    }

    ComplexNumber u = new ComplexNumber(0.0, -(1 + alpha));
    final int offset = halfN - 1;
    ComplexNumber[] f = ajointFunc.evaluate(u);
    ComplexNumber num = exp(f[0]);
    ComplexNumber denom = multiply(u, subtract(MINUS_I, u));
    ComplexNumber v = divide(num, denom);
    z[0][offset] = v;
    for (int j = 1; j < size; j++) {
      ComplexNumber temp = multiply(v, f[j]);
      z[j][offset] = temp;
    }
    for (int i = 1; i < m; i++) {
      u = new ComplexNumber(i * delta, -(1 + alpha));
      f = ajointFunc.evaluate(u);
      num = exp(f[0]);
      denom = multiply(u, subtract(MINUS_I, u));
      v = divide(num, denom);
      z[0][offset + i] = v;
      z[0][offset - i] = ComplexMathUtils.conjugate(v);
      for (int j = 1; j < size; j++) {
        ComplexNumber temp = multiply(v, f[j]);
        z[j][offset + i] = temp;
        z[j][offset - i] = ComplexMathUtils.conjugate(temp); //TODO the FFT should take care of this
      }
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

  private double getReducedGreek(final ComplexNumber x, final double alpha, final double delta, final double k, final double a) {
    final ComplexNumber temp = ComplexMathUtils.multiply(ComplexMathUtils.exp(new ComplexNumber(-alpha * k, -k * a)), x);
    final double y = delta * temp.getReal() / 2 / Math.PI;
    return y;
  }

}
