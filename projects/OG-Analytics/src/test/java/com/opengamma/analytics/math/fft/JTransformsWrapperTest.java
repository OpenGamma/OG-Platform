/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.fft;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.ComplexMathUtils;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JTransformsWrapperTest {

  private final static double MU = -1.0;
  private final static double SIMGA = 0.5;
  private final static double X_MIN;
  private final static double DELTAX;
  private final static int N = 256;

  private static final Function1D<Double, Double> F1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double t) {
      return Math.cos(6 * Math.PI * t) + Math.cos(8 * Math.PI * t) + Math.cos(9 * Math.PI * t);
    }

  };

  private static final Function1D<Double, Double> F2 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double t) {
      return Math.sin(10 * t);
    }

  };

  private static final Function1D<Double, Double> GAUSS = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double t) {
      return Math.exp(-0.5 * FunctionUtils.square((t - MU) / SIMGA)) / Math.sqrt(2 * Math.PI) / SIMGA;
    }

  };

  private static final Function1D<ComplexNumber, ComplexNumber> GAUSS_TRANSFORM = new Function1D<ComplexNumber, ComplexNumber>() {

    @Override
    public ComplexNumber evaluate(final ComplexNumber x) {
      ComplexNumber temp = ComplexMathUtils.multiply(x, new ComplexNumber(SIMGA));
      temp = ComplexMathUtils.multiply(0.5, ComplexMathUtils.multiply(temp, temp));
      final ComplexNumber z = ComplexMathUtils.subtract(ComplexMathUtils.multiply(x, new ComplexNumber(0, -MU)), temp);
      return ComplexMathUtils.exp(z);
    }

  };

  private static final double[] A = new double[N];
  private static final double[] A2 = new double[N];

  private static final double[] G;
  private static final ComplexNumber[] G_TRANS;

  private static final double EPS = 1e-12;
  static {
    final double step = 0.04;
    double t = -step * N / 2.0;
    for (int i = 0; i < N; i++) {
      A[i] = F1.evaluate(t);
      A2[i] = F2.evaluate(t);
      t += step;
    }

    DELTAX = Math.sqrt(2 * Math.PI / N); // make delta same in normal and Fourier space
    X_MIN = -N * DELTAX / 2.0;

    G = new double[N];
    G_TRANS = new ComplexNumber[N];
    double x = X_MIN;
    for (int i = 0; i < N; i++) {
      G[i] = GAUSS.evaluate(x);
      G_TRANS[i] = GAUSS_TRANSFORM.evaluate(new ComplexNumber(x));
      x += DELTAX;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    JTransformsWrapper.transform1DComplex(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    JTransformsWrapper.inverseTransform1DComplex(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    JTransformsWrapper.fullTransform1DReal(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    JTransformsWrapper.fullInverseTransform1DReal(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    JTransformsWrapper.transform1DReal(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    JTransformsWrapper.inverseTransform1DReal(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty1() {
    JTransformsWrapper.transform1DComplex(new ComplexNumber[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty2() {
    JTransformsWrapper.inverseTransform1DComplex(new ComplexNumber[0], false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty3() {
    JTransformsWrapper.fullTransform1DReal(new double[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty4() {
    JTransformsWrapper.fullInverseTransform1DReal(new double[0], false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty5() {
    JTransformsWrapper.transform1DReal(new double[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty6() {
    JTransformsWrapper.inverseTransform1DReal(new double[0], false);
  }

  @Test
  public void testForwardBackwardFull() {

    final ComplexNumber[] transform = JTransformsWrapper.fullTransform1DReal(A);
    assertEquals(N, transform.length);
    ComplexNumber[] inverse = JTransformsWrapper.inverseTransform1DComplex(transform, true);
    assertEquals(N, inverse.length);

    final double[] realTransform = new double[N];
    final ComplexNumber[] complex = new ComplexNumber[N];
    for (int i = 0; i < N; i++) {
      realTransform[i] = transform[i].getReal();
      complex[i] = new ComplexNumber(A[i], 0);
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }

    final ComplexNumber[] transformComplex = JTransformsWrapper.transform1DComplex(complex);
    assertEquals(N, transformComplex.length);
    inverse = JTransformsWrapper.fullInverseTransform1DReal(realTransform, true);
    assertEquals(N, inverse.length);
    for (int i = 0; i < N; i++) {
      assertComplexEquals(transform[i], transformComplex[i]);
      // The DFT of cos functions will generally contain imaginary parts unless it is symmetrically sampled
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }
  }

  @Test
  public void testForwardBackwardReal() {
    final ComplexNumber[] transform = JTransformsWrapper.transform1DReal(A);
    final ComplexNumber[] transformFull = JTransformsWrapper.fullTransform1DReal(A);

    assertEquals(N / 2, transform.length - 1);
    assertEquals(N, transformFull.length);

    final double[] realTransform = new double[A.length];
    for (int i = 0; i < transform.length; i++) {
      realTransform[i] = transform[i].getReal();
      assertComplexEquals(transform[i], transformFull[i]);
    }
    //final ComplexNumber[] inverse = JTransformsWrapper.inverseTransform1DReal(realTransform, true);
    //for (final ComplexNumber element : inverse) {
      // TODO fix test assertEquals(inverse[i].getReal(), A[i], EPS);
    //}
  }

  // @Test
  // public void testSin() {
  // final ComplexNumber[] transform = JTransformsWrapper.fullTransform1DReal(A2);
  // int n = transform.length;
  // double deltaOmega = 2 * Math.PI / n / 0.04;
  // double omega;
  //
  // for (int i = n / 2; i < n; i++) {
  // omega = (i - n) * deltaOmega;
  //
  // ComplexNumber res = ComplexMathUtils.multiply(0.04, transform[i]);
  // // res = ComplexMathUtils.conjugate(res);// TODO why?
  // System.out.println(omega + "\t" + res.getReal() + "\t" + res.getImaginary());
  // }
  //
  // for (int i = 0; i <= n / 2; i++) {
  // omega = i * deltaOmega;
  // ComplexNumber res = ComplexMathUtils.multiply(0.04, transform[i]);
  // // res = ComplexMathUtils.conjugate(res);// TODO why?
  //
  // System.out.println(omega + "\t" + res.getReal() + "\t" + res.getImaginary());
  // }
  // }

  @Test
  public void testParsevalsTheorem() {
    final ComplexNumber[] transform = JTransformsWrapper.transform1DReal(A);
    final int n = A.length;
    double sum1 = 0;
    double sum2 = 0;
    for (int i = 0; i < n; i++) {
      sum1 += A[i] * A[i];
    }
    for (final ComplexNumber element : transform) {
      final double temp = ComplexMathUtils.mod(element);
      sum2 += temp * temp;
    }
    sum2 *= 2.0; // since A is real
    sum2 /= n;
    assertEquals(1.0, sum1 / sum2, 1e-2);
  }

  @Test
  public void testGauss() {

    final ComplexNumber[] transform = JTransformsWrapper.transform1DReal(G);
    final int n = transform.length;
    assertEquals(N / 2, n - 1, 0);

    final double deltaOmega = 2 * Math.PI / N / DELTAX;
    double omega;

    for (int i = 0; i < n; i++) {
      omega = i * deltaOmega;
      final ComplexNumber scale = ComplexMathUtils.multiply(DELTAX, ComplexMathUtils.exp(new ComplexNumber(0.0, omega * X_MIN)));
      final ComplexNumber res = ComplexMathUtils.multiply(scale, transform[i]);
      assertComplexEquals(GAUSS_TRANSFORM.evaluate(new ComplexNumber(omega)), res);
    }

  }

  @Test
  public void testGauss2() {

    final ComplexNumber[] transform = JTransformsWrapper.fullTransform1DReal(G);

    final int n = transform.length;
    assertEquals(n, N, 0);

    final double deltaF = 2 * Math.PI / N / DELTAX;
    double omega;

    for (int i = n / 2; i < n; i++) {
      omega = (i - n) * deltaF;
      final ComplexNumber scale = ComplexMathUtils.multiply(DELTAX, ComplexMathUtils.exp(new ComplexNumber(0.0, omega * X_MIN)));
      final ComplexNumber res = ComplexMathUtils.multiply(scale, transform[i]);
      assertComplexEquals(GAUSS_TRANSFORM.evaluate(new ComplexNumber(omega)), res);
      // System.out.println(omega + "\t" + res.getReal() + "\t" + res.getImaginary());
    }

    for (int i = 0; i <= n / 2; i++) {
      omega = i * deltaF;
      final ComplexNumber scale = ComplexMathUtils.multiply(DELTAX, ComplexMathUtils.exp(new ComplexNumber(0.0, omega * X_MIN)));
      final ComplexNumber res = ComplexMathUtils.multiply(scale, transform[i]);

      assertComplexEquals(GAUSS_TRANSFORM.evaluate(new ComplexNumber(omega)), res);
      // System.out.println(omega + "\t" + res.getReal() + "\t" + res.getImaginary());
    }

  }

  @Test
  public void testGaussBackTransform() {

    final ComplexNumber[] transform = JTransformsWrapper.inverseTransform1DComplex(G_TRANS, false);
    final int n = transform.length;
    assertEquals(n, G_TRANS.length, 0);

    final double deltaX = 2 * Math.PI / n / DELTAX;
    double x;

    for (int i = n / 2; i < n; i++) {
      x = (i - n) * deltaX;
      final ComplexNumber scale = ComplexMathUtils.multiply(DELTAX / 2 / Math.PI, ComplexMathUtils.exp(new ComplexNumber(0.0, -x * X_MIN)));
      final ComplexNumber res = ComplexMathUtils.multiply(scale, transform[i]);
      assertComplexEquals(new ComplexNumber(GAUSS.evaluate(x), 0.0), res);
      // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    }
    for (int i = 0; i <= n / 2; i++) {
      x = i * deltaX;
      final ComplexNumber scale = ComplexMathUtils.multiply(DELTAX / 2 / Math.PI, ComplexMathUtils.exp(new ComplexNumber(0.0, -x * X_MIN)));
      final ComplexNumber res = ComplexMathUtils.multiply(scale, transform[i]);
      assertComplexEquals(new ComplexNumber(GAUSS.evaluate(x), 0.0), res);
      // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    }

  }

  private void assertComplexEquals(final ComplexNumber z1, final ComplexNumber z2) {
    assertEquals(z1.getReal(), z2.getReal(), EPS);
    assertEquals(z1.getImaginary(), z2.getImaginary(), EPS);
  }
}
