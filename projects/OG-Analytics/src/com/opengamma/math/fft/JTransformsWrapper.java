/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.fft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.math.number.ComplexNumber;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Wrapper class for 1D FFTs.
 */
public class JTransformsWrapper {
  // TODO this needs to be changed
  private static final Map<Integer, DoubleFFT_1D> CACHE_1D = new HashMap<Integer, DoubleFFT_1D>();

  /**
   * The forward discrete Fourier transform (DFT). <b>Note:</b> In this definition -i appears in the exponential rather than the normal i. <br/>
   * If z is a array of N complex values sampled at intervals of Delta from a function h(t), then the transform H(f) = int^{infty}_{-infty} exp(-i*2*pi*f*t)h(t)dt is sampled at N points at intervals
   * of 1/(N*Delta). The first N/2+1 values  (i = 0 to N/2) are f = i/(N*Delta), while the values i = N/2 to N - 1 are f = (i-N)/(N*Delta) (i.e. the negative frequencies with 
   *  H(1/(2*Delta)) = H(-1/(2*Delta))).
   * @param z Array of N complex values
   * @return  Array of N complex values 
   */
  public static ComplexNumber[] transform1DComplex(final ComplexNumber[] z) {
    Validate.notNull(z, "array of complex number");
    int n = z.length;
    double[] a = packFull(z);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexForward(a);
    return unpackFull(a);
  }

  /**
   * The backward discrete Fourier transform (DFT). <b>Note:</b> In this definition i appears in the exponential rather than the normal -i. <br/>
   * If z is a array of N complex values sampled at intervals of delta from a function H(f), then the transform h(t) = int^{infty}_{-infty} exp(i*2*pi*f*t)H(f)df is sampled at N points at intervals
   * of 1/(N*delta). The first N/2+1 values  (i = 0 to N/2) are t = i/(N*delta), while the values i = N/2 to N - 1 are t = (i-N)/(N*delta) (i.e. the negative times with 
   *  h(1/(2*delta)) = h(-1/(2*delta))).
   * @param z Array of N complex values
   * @param scale Scale the output by 1/N 
   * @return  Array of N complex values 
   */
  public static ComplexNumber[] inverseTransform1DComplex(final ComplexNumber[] z, final boolean scale) {
    Validate.notNull(z, "array of complex number");
    final int n = z.length;
    double[] a = packFull(z);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexInverse(a, scale);
    return unpackFull(a);
  }

  /**
   * The forward discrete Fourier transform (DFT). <b>Note:</b> In this definition -i appears in the exponential rather than the normal i. <br/>
   * If h is a array of N real values sampled at intervals of Delta from a function h(t), then the transform H(f) = int^{infty}_{-infty} exp(-i*2*pi*f*t)h(t)dt is sampled at N points at intervals
   * of 1/(N*Delta). The first N/2+1 values  (i = 0 to N/2) are f = i/(N*Delta), while the values i = N/2 to N - 1 are f = (i-N)/(N*Delta) (i.e. the negative frequencies with 
   *  H(1/(2*Delta)) = H(-1/(2*Delta))). Since h(t) is real, H(f) = H(-f)*, so the second half of the array (negative values of f) are superfluous  
   * @param h Array of N real values
   * @return  Array of N complex values 
   */
  public static ComplexNumber[] fullTransform1DReal(final double[] h) {
    Validate.notNull(h, "array of doubles");
    final int n = h.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(h, 2 * n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realForwardFull(a);
    return unpackFull(a);
  }

  /**
   * The backward discrete Fourier transform (DFT). <b>Note:</b> In this definition i appears in the exponential rather than the normal -i. <br/>
   * If x is a array of N real values sampled at intervals of delta from a function H(f), then the transform h(t) = int^{infty}_{-infty} exp(i*2*pi*f*t)H(f)df is sampled at N points at intervals
   * of 1/(N*delta). The first N/2+1 values  (i = 0 to N/2) are t = i/(N*delta), while the values i = N/2 to N - 1 are t = (i-N)/(N*delta) (i.e. the negative times with 
   *  h(1/(2*delta)) = h(-1/(2*delta))). Since H(f) is real, h(t) = h(-t)*, so the second half of the array (negative values of t) are superfluous  
   * @param x Array of N real values
   * @param scale Scale the output by 1/N 
   * @return  Array of N complex values 
   */
  public static ComplexNumber[] fullInverseTransform1DReal(final double[] x, final boolean scale) {
    Validate.notNull(x, "array of doubles");
    final int n = x.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(x, 2 * n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realInverseFull(a, scale);
    return unpackFull(a);
  }

  /**
   * The forward discrete Fourier transform (DFT). <b>Note:</b> In this definition -i appears in the exponential rather than the normal i. 
   * If h is a array of N real values sampled at intervals of Delta from a function h(t), then the transform H(f) = int^{infty}_{-infty} exp(-i*2*pi*f*t)h(t)dt is sampled at N/2 points at intervals
   * of 1/(N*Delta); f = i/(N*Delta) for i = 0 to N/2-1. Since h(t) is real, H(f) = H(-f)*, so the negative values of f, which would be in N/2 to N-1 of the return array are suppressed 
   * @param h Array of N real values
   * @return  Array of N/2 complex values 
   */
  public static ComplexNumber[] transform1DReal(final double[] h) {
    Validate.notNull(h, "array of doubles");
    final int n = h.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(h, n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realForward(a);
    return unpack(a);
  }

  /**
   * The backward discrete Fourier transform (DFT). <b>Note:</b> In this definition i appears in the exponential rather than the normal -i. <br/>
   * If x is a array of N real values sampled at intervals of delta from a function H(f), then the transform h(t) = int^{infty}_{-infty} exp(i*2*pi*f*t)H(f)df is sampled at N/2 points at intervals
   * of 1/(N*delta); t = i/(N*delta) for i = 0 to N/2-1. Since H(f) is real, h(t) = h(-t)*, so the negative values of t, which would be in N/2 to N-1 of the return array are suppressed 
   * @param x Array of N real values
   * @param scale Scale the output by 1/N 
   * @return  Array of N complex values 
   */
  public static ComplexNumber[] inverseTransform1DReal(final double[] x, final boolean scale) {
    Validate.notNull(x, "array of doubles");
    final int n = x.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(x, n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realInverse(a, scale);
    return unpack(a);
  }

  private static double[] packFull(final ComplexNumber[] z) {
    int n = z.length;
    Validate.isTrue(n > 0);
    double[] a = new double[2 * n];
    for (int i = 0; i < n; i++) {
      a[2 * i] = z[i].getReal();
      a[2 * i + 1] = z[i].getImaginary();
    }
    return a;
  }

  private static ComplexNumber[] unpackFull(final double[] a) {
    final int n = a.length;
    if (n % 2 != 0) {
      throw new IllegalArgumentException("Had an odd number of entries: should be impossible");
    }
    final ComplexNumber[] z = new ComplexNumber[n / 2];
    for (int i = 0; i < n; i += 2) {
      z[i / 2] = new ComplexNumber(a[i], a[i + 1]);
    }
    return z;
  }

  private static ComplexNumber[] unpack(final double[] a) {
    final int n = a.length;
    if (n % 2 == 0) {
      int m = n / 2 + 1;
      final ComplexNumber[] z = new ComplexNumber[m];
      z[0] = new ComplexNumber(a[0]);
      z[n / 2] = new ComplexNumber(a[1]);
      for (int i = 1; i < n / 2; i++) {
        z[i] = new ComplexNumber(a[i * 2], a[i * 2 + 1]);
      }
      return z;
    }

    int m = (n - 1) / 2 + 1;
    final ComplexNumber[] z = new ComplexNumber[m];
    z[0] = new ComplexNumber(a[0]);
    z[m - 1] = new ComplexNumber(a[n - 1], a[1]);
    for (int i = 1; i < m - 2; i++) {
      z[i] = new ComplexNumber(a[i * 2], a[i * 2 + 1]);
    }
    return z;

  }

}
