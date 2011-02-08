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
  //TODO this needs to be changed
  private static final Map<Integer, DoubleFFT_1D> CACHE_1D = new HashMap<Integer, DoubleFFT_1D>();

  public static ComplexNumber[] transform1DComplex(final ComplexNumber[] z) {
    Validate.notNull(z, "array of complex number");
    final int n = z.length;
    Validate.isTrue(n > 0);
    final double[] a = new double[2 * n];
    for (int i = 0; i < n; i++) {
      a[2 * i] = z[i].getReal();
      a[2 * i + 1] = z[i].getImaginary();
    }
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexForward(a);
    return convertToComplex(a);
  }

  public static ComplexNumber[] inverseTransform1DComplex(final ComplexNumber[] z, final boolean scale) {
    Validate.notNull(z, "array of complex number");
    final int n = z.length;
    Validate.isTrue(n > 0);
    final double[] a = new double[2 * n];
    for (int i = 0; i < n; i++) {
      a[2 * i] = z[i].getReal();
      a[2 * i + 1] = z[i].getImaginary();
    }
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexInverse(a, scale);
    return convertToComplex(a);
  }

  public static ComplexNumber[] fullTransform1DReal(final double[] x) {
    Validate.notNull(x, "array of doubles");
    final int n = x.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(x, 2 * n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realForwardFull(a);
    return convertToComplex(a);
  }

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
    return convertToComplex(a);
  }

  public static ComplexNumber[] transform1DReal(final double[] x) {
    Validate.notNull(x, "array of doubles");
    final int n = x.length;
    Validate.isTrue(n > 0);
    final double[] a = Arrays.copyOf(x, n);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.realForward(a);
    return convertToComplex(a);
  }

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
    return convertToComplex(a);
  }

  private static ComplexNumber[] convertToComplex(final double[] a) {
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
}
