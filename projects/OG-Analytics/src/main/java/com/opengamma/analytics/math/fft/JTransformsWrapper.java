/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.fft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Class wrapping the 1D FFT methods of the JTransforms library.
 */
public class JTransformsWrapper {
  // TODO this needs to be changed
  private static final Map<Integer, DoubleFFT_1D> CACHE_1D = new HashMap<>();

  /**
   * The forward discrete Fourier transform. *Note:* In this definition $-i$
   * appears in the exponential rather than $i$.
   * <p>
   * If $z$ is an array of $N$ complex values sampled at intervals $\Delta$
   * from a function $h(t)$, then the transform
   * $$
   * $H(f) = \int^{\infty}_{-\infty} e^{-2i\pi f t} h(t) dt$
   * $$
   * is sampled at $N$ points at intervals of $\frac{1}{N\Delta}$.
   * <p>
   * The first $\frac{N}{2} + 1$ values ($i = 0$ to $\frac{N}{2}$) are
   * $f = \frac{i}{N\Delta}$, while the values $i = \frac{N}{2}$ to $N-1$
   * are $f = \frac{i-N}{N \Delta}$ (i.e. negative frequencies with
   * $H(\frac{1}{2 \Delta}) = H(\frac{-1}{2 \Delta})$).
   * @param z Array of N complex values
   * @return The Fourier transform of the array
   */
  public static ComplexNumber[] transform1DComplex(final ComplexNumber[] z) {
    ArgumentChecker.notNull(z, "array of complex number");
    final int n = z.length;
    final double[] a = packFull(z);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexForward(a);
    return unpackFull(a);
  }

  /**
   * The inverse (backward) discrete Fourier transform. *Note:* In this
   * definition $i$ appears in the exponential rather than $-i$.
   * <p>
   * If $z$ is a array of $N$ complex values sampled at intervals $\Delta$ from
   * a function $H(f)$, then the transform
   * $$
   * $h(t) = \int^{\infty}_{-\infty} e^{2i\pi f t} H(f) df$
   * $$
   * is sampled at $N$ at intervals of $\frac{1}{N\Delta}$.
   * <p>
   * The first $\frac{N}{2} + 1$ values ($i = 0$ to $\frac{N}{2}$) are $t =
   * \frac{i}{N\Delta}$, while the values $i = \frac{N}{2}$ to $N - 1$ are $t =
   * \frac{i-N}{N\Delta}$ (i.e. negative times with $h(\frac{1}{2\Delta}) =
   * h(\frac{-1}{2\Delta})$).
   * @param z Array of N complex values
   * @param scale Scale the output by $\frac{1}{N}$
   * @return The inverse Fourier transform of the array
   */
  public static ComplexNumber[] inverseTransform1DComplex(final ComplexNumber[] z, final boolean scale) {
    ArgumentChecker.notNull(z, "array of complex number");
    final int n = z.length;
    final double[] a = packFull(z);
    DoubleFFT_1D fft = CACHE_1D.get(n);
    if (fft == null) {
      fft = new DoubleFFT_1D(n);
      CACHE_1D.put(n, fft);
    }
    fft.complexInverse(a, scale);
    return unpackFull(a);
  }

  /**
   * The forward discrete Fourier transform. *Note:* In this definition
   * $-i$ appears in the exponential rather than $i$.
   * <p>
   * If $z$ is an array of $N$ complex values sampled at intervals $\Delta$
   * from a function $h(t)$, then the transform
   * $$
   * $H(f) = \int^{\infty}_{-\infty} e^{-2i\pi f t} h(t) dt$
   * $$
   * is sampled at $N$ points at intervals of $\frac{1}{N\Delta}$.
   * <p>
   * The first $\frac{N}{2} + 1$ values ($i = 0$ to $\frac{N}{2}$) are $f =
   * \frac{i}{N\Delta}$, while the values $i = \frac{N}{2}$ to $N - 1$ are $f =
   * \frac{i-N}{N\Delta}$ (i.e. negative frequencies with $H(\frac{1}{2\Delta})
   * = H(\frac{-1}{2\Delta})$).
   * <p>
   * As $h(t)$ is real, $H(f) = H(-f)^*$, so the second half of the array
   * (negative values of f) are superfluous.
   * @param h Array of N real values
   * @return The Fourier transform of the array
   */
  public static ComplexNumber[] fullTransform1DReal(final double[] h) {
    ArgumentChecker.notEmpty(h, "array of doubles");
    final int n = h.length;
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
   * The inverse (backward) discrete Fourier transform. *Note:* In this
   * definition $i$ appears in the exponential rather than $-i$.
   * <p>
   * If $z$ is a array of $N$ complex values sampled at intervals $\Delta$ from
   * a function $H(f)$, then the transform
   * $$
   * $h(t) = \int^{\infty}_{-\infty} e^{2i\pi f t} H(f) df$
   * $$
   * is sampled at $N$ at intervals of $\frac{1}{N\Delta}$.
   * <p>
   * The first $\frac{N}{2} + 1$ values ($i = 0$ to $\frac{N}{2}$) are $t =
   * \frac{i}{N\Delta}$, while the values $i = \frac{N}{2}$ to $N - 1$ are $t =
   * \frac{i-N}{N\Delta}$ (i.e. negative times with $h(\frac{1}{2\Delta}) =
   * h(\frac{-1}{2\Delta})$).
   * <p>
   * As $H(f)$ is real, $h(t) = h(-t)^*$, so the second half of the array
   * (negative values of t) are superfluous.
   * @param x Array of N real values
   * @param scale Scale the output by $\frac{1}{N}$
   * @return The inverse Fourier transform of the array
   */
  public static ComplexNumber[] fullInverseTransform1DReal(final double[] x, final boolean scale) {
    ArgumentChecker.notEmpty(x, "array of doubles");
    final int n = x.length;
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
   * The forward discrete Fourier transform. *Note:* In this definition $-i$
   * appears in the exponential rather than $i$.  If $h$ is a array of $N$ real
   * values sampled at intervals of $\Delta$ from a function $h(t)$, then the
   * transform:
   * $$
   * $H(f) = \int^{\infty}_{-\infty} e^{-2i\pi f t} h(t) dt$
   * $$
   * is sampled at $\frac{N}{2}$ points at intervals of $\frac{1}{N\Delta}$,
   * with $f = \frac{i}{N\Delta}$ for $i = 0$ to $\frac{N}{2} - 1$.
   * <p>
   * As $h(t)$ is real, $H(f) = F(-f)^*$, so the negative values of f, which
   * would be in $\frac{N}{2}$ to $N - 1$ of the return array, are suppressed.
   * @param h Array of $N$ real values
   * @return The Fourier transform of the array
   */
  public static ComplexNumber[] transform1DReal(final double[] h) {
    ArgumentChecker.notEmpty(h, "array of doubles");
    final int n = h.length;
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
   * The backward discrete Fourier transform. *Note:* In this definition $i$
   * appears in the exponential rather than $-i$.
   * <p>
   * If $x$ is a array of $N$ real values sampled at intervals of $\Delta$ from
   * a function $H(f)$, then the transform
   * $$
   * $h(t) = \int^{\infty}_{-\infty} e^{2i\pi f t} H(f) df$
   * $$
   * is sampled at $\frac{N}{2}$ points at intervals of $\frac{1}{N\Delta}$; $t
   * = \frac{i}{N\Delta}$ for $i = 0$ to $\frac{N}{2} - 1$.
   * <p>
   * As $H(f)$ is real, $h(t) = h(-t)^*$, so the negative values of t, which
   * would be in $\frac{N}{2}$ to $N - 1$ of the return array, are suppressed.
   * @param x Array of $N$ real values
   * @param scale Scale the output by $\frac{1}{N}$
   * @return The inverse Fourier transform of the array
   */
  public static ComplexNumber[] inverseTransform1DReal(final double[] x, final boolean scale) {
    ArgumentChecker.notEmpty(x, "array of doubles");
    final int n = x.length;
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
    final int n = z.length;
    final double[] a = new double[2 * n];
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
      final int m = n / 2 + 1;
      final ComplexNumber[] z = new ComplexNumber[m];
      z[0] = new ComplexNumber(a[0]);
      z[n / 2] = new ComplexNumber(a[1]);
      for (int i = 1; i < n / 2; i++) {
        z[i] = new ComplexNumber(a[i * 2], a[i * 2 + 1]);
      }
      return z;
    }

    final int m = (n - 1) / 2 + 1;
    final ComplexNumber[] z = new ComplexNumber[m];
    z[0] = new ComplexNumber(a[0]);
    z[m - 1] = new ComplexNumber(a[n - 1], a[1]);
    for (int i = 1; i < m - 2; i++) {
      z[i] = new ComplexNumber(a[i * 2], a[i * 2 + 1]);
    }
    return z;

  }

}
