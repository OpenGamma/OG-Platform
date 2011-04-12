/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Class wrapping the 1D FFT methods of the JTransforms library.
 */
public class JTransformsWrapper {
  // TODO this needs to be changed
  private static final Map<Integer, DoubleFFT_1D> CACHE_1D = new HashMap<Integer, DoubleFFT_1D>();

  /**
   * The forward discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $-i$} appears in the exponential rather than {@latex.inline $i$}. 
   * <p>
   * If {@latex.inline $z$} is an array of {@latex.inline $N$} complex values sampled at intervals {@latex.inline $\\Delta$} from a function {@latex.inline $h(t)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $H(f) = \\int^{\\infty}_{-\\infty} e^{-2i\\pi f t} h(t) dt$
   * }
   * is sampled at {@latex.inline $N$} points at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}. 
   * <p>
   * The first {@latex.inline $\\frac{N}{2} + 1$} 
   * values ({@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2}$}) are {@latex.inline $f = \\frac{i}{N\\Delta}$}, while the values 
   * {@latex.inline $i = \\frac{N}{2}$} to {@latex.inline $N - 1$} are {@latex.inline $f = \\frac{i-N}{N \\Delta}$} (i.e. negative frequencies with 
   * {@latex.inline $H(\\frac{1}{2 \\Delta}) = H(\\frac{-1}{2 \\Delta})$}).
   * @param z Array of N complex values
   * @return The Fourier transform of the array
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
   * The inverse (backward) discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $i$} appears in the exponential rather than {@latex.inline $-i$}.
   * <p>
   * If {@latex.inline $z$} is a array of {@latex.inline $N$} complex values sampled at intervals {@latex.inline $\\Delta$} from a function {@latex.inline $H(f)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $h(t) = \\int^{\\infty}_{-\\infty} e^{2i\\pi f t} H(f) df$
   * }
   * is sampled at {@latex.inline $N$} at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}. 
   * <p>
   * The first {@latex.inline $\\frac{N}{2} + 1$} values ({@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2}$}) are {@latex.inline $t = \\frac{i}{N\\Delta}$}, while the values 
   * {@latex.inline $i = \\frac{N}{2}$} to {@latex.inline $N - 1$} are {@latex.inline $t = \\frac{i-N}{N\\Delta}$} (i.e. negative times with 
   * {@latex.inline $h(\\frac{1}{2\\Delta}) = h(\\frac{-1}{2\\Delta})$}).
   * @param z Array of N complex values
   * @param scale Scale the output by {@latex.inline $\\frac{1}{N}$} 
   * @return The inverse Fourier transform of the array
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
   * The forward discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $-i$} appears in the exponential rather than {@latex.inline $i$}. 
   * <p>
   * If {@latex.inline $z$} is an array of {@latex.inline $N$} complex values sampled at intervals {@latex.inline $\\Delta$} from a function {@latex.inline $h(t)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $H(f) = \\int^{\\infty}_{-\\infty} e^{-2i\\pi f t} h(t) dt$
   * }
   * is sampled at {@latex.inline $N$} points at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}. 
   * <p>
   * The first {@latex.inline $\\frac{N}{2} + 1$} 
   * values ({@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2}$}) are {@latex.inline $f = \\frac{i}{N\\Delta}$}, while the values 
   * {@latex.inline $i = \\frac{N}{2}$} to {@latex.inline $N - 1$} are {@latex.inline $f = \\frac{i-N}{N\\Delta}$} (i.e. negative frequencies with 
   * {@latex.inline $H(\\frac{1}{2\\Delta}) = H(\\frac{-1}{2\\Delta})$}).
   * <p>
   * As {@latex.inline $h(t)$} is real, {@latex.inline $H(f) = H(-f)^*$}, so the second half of the array (negative values of f) are superfluous.
   * @param h Array of N real values
   * @return The Fourier transform of the array
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
   * The inverse (backward) discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $i$} appears in the exponential rather than {@latex.inline $-i$}.
   * <p>
   * If {@latex.inline $z$} is a array of {@latex.inline $N$} complex values sampled at intervals {@latex.inline $\\Delta$} from a function {@latex.inline $H(f)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $h(t) = \\int^{\\infty}_{-\\infty} e^{2i\\pi f t} H(f) df$
   * }
   * is sampled at {@latex.inline $N$} at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}. 
   * <p>
   * The first {@latex.inline $\\frac{N}{2} + 1$} values ({@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2}$}) are {@latex.inline $t = \\frac{i}{N\\Delta}$}, while the values 
   * {@latex.inline $i = \\frac{N}{2}$} to {@latex.inline $N - 1$} are {@latex.inline $t = \\frac{i-N}{N\\Delta}$} (i.e. negative times with 
   * {@latex.inline $h(\\frac{1}{2\\Delta}) = h(\\frac{-1}{2\\Delta})$}).
   * <p> 
   * As {@latex.inline $H(f)$} is real, {@latex.inline $h(t) = h(-t)^*$}, so the second half of the array (negative values of t) are superfluous. 
   * @param x Array of N real values
   * @param scale Scale the output by {@latex.inline $\\frac{1}{N}$} 
   * @return The inverse Fourier transform of the array
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
   * The forward discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $-i$} appears in the exponential rather than {@latex.inline $i$}. 
   * If {@latex.inline $h$} is a array of {@latex.inline $N$} real values sampled at intervals of {@latex.inline $\\Delta$} from a function {@latex.inline $h(t)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $H(f) = \\int^{\\infty}_{-\\infty} e^{-2i\\pi f t} h(t) dt$
   * }
   * is sampled at {@latex.inline $\\frac{N}{2}$} points at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}, with {@latex.inline $f = \\frac{i}{N\\Delta}$} for 
   * {@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2} - 1$}. 
   * <p> 
   * As {@latex.inline $h(t)$} is real, {@latex.inline $H(f) = F(-f)^*$}, so the negative values of f, which would be in {@latex.inline $\\frac{N}{2}$} to {@latex.inline $N - 1$}
   * of the return array, are suppressed.
   * @param h Array of {@latex.inline $N$} real values
   * @return The Fourier transform of the array
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
   * The backward discrete Fourier transform. <b>Note:</b> In this definition {@latex.inline $i$} appears in the exponential rather than {@latex.inline $-i$}. 
   * <p>
   * If {@latex.inline $x$} is a array of {@latex.inline $N$} real values sampled at intervals of {@latex.inline $\\Delta$} from a function {@latex.inline $H(f)$}, 
   * then the transform 
   * {@latex.ilb %preamble{\\usepackage{amsmath}} 
   * $h(t) = \\int^{\\infty}_{-\\infty} e^{2i\\pi f t} H(f) df$
   * } 
   * is sampled at {@latex.inline $\\frac{N}{2}$} points at intervals of {@latex.inline $\\frac{1}{N\\Delta}$}; {@latex.inline $t = \\frac{i}{N\\Delta}$} for
   * {@latex.inline $i = 0$} to {@latex.inline $\\frac{N}{2} - 1$}.
   * <p> 
   * As {@latex.inline $H(f)$} is real, {@latex.inline $h(t) = h(-t)^*$}, so the negative values of t, which would be in {@latex.inline $\\frac{N}{2}$} to {@latex.inline $N - 1$}
   * of the return array, are suppressed. 
   * @param x Array of {@latex.inline $N$} real values
   * @param scale Scale the output by {@latex.inline $\\frac{1}{N}$}
   * @return The inverse Fourier transform of the array
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
