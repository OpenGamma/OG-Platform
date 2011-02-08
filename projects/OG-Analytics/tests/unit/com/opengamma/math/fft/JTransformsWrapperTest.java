/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.fft;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class JTransformsWrapperTest {
  private static final Function1D<Double, Double> F1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double t) {
      return Math.cos(6 * Math.PI * t) + Math.cos(8 * Math.PI * t) + Math.cos(9 * Math.PI * t);
    }

  };
  private static final double[] A = new double[100];
  private static final double EPS = 1e-12;
  static {
    final double step = 0.04;
    double t = -2;
    for (int i = 0; i < 100; i++) {
      A[i] = F1.evaluate(t);
      t += step;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull1() {
    JTransformsWrapper.transform1DComplex(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    JTransformsWrapper.inverseTransform1DComplex(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull3() {
    JTransformsWrapper.fullTransform1DReal(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull4() {
    JTransformsWrapper.fullInverseTransform1DReal(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull5() {
    JTransformsWrapper.transform1DReal(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull6() {
    JTransformsWrapper.inverseTransform1DReal(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty1() {
    JTransformsWrapper.transform1DComplex(new ComplexNumber[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty2() {
    JTransformsWrapper.inverseTransform1DComplex(new ComplexNumber[0], false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty3() {
    JTransformsWrapper.fullTransform1DReal(new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty4() {
    JTransformsWrapper.fullInverseTransform1DReal(new double[0], false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty5() {
    JTransformsWrapper.transform1DReal(new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty6() {
    JTransformsWrapper.inverseTransform1DReal(new double[0], false);
  }

  @Test
  public void testForwardBackwardFull() {
    final int n = A.length;
    final ComplexNumber[] transform = JTransformsWrapper.fullTransform1DReal(A);
    ComplexNumber[] inverse = JTransformsWrapper.inverseTransform1DComplex(transform, true);
    final double[] realTransform = new double[n];
    final ComplexNumber[] complex = new ComplexNumber[n];
    for (int i = 0; i < n; i++) {
      realTransform[i] = transform[i].getReal();
      complex[i] = new ComplexNumber(A[i], 0);
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }
    final ComplexNumber[] transformComplex = JTransformsWrapper.transform1DComplex(complex);
    inverse = JTransformsWrapper.fullInverseTransform1DReal(realTransform, true);
    for (int i = 0; i < n; i++) {
      assertComplexEquals(transform[i], transformComplex[i]);
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }
  }

  @Test
  public void testForwardBackwardReal() {
    final ComplexNumber[] transform = JTransformsWrapper.transform1DReal(A);
    final ComplexNumber[] transformFull = JTransformsWrapper.fullTransform1DReal(A);
    final double[] realTransform = new double[A.length];
    for (int i = 0; i < transform.length; i++) {
      realTransform[i] = transform[i].getReal();
      assertComplexEquals(transform[i], transformFull[i]);
    }
    final ComplexNumber[] inverse = JTransformsWrapper.inverseTransform1DReal(realTransform, true);
    for (int i = 0; i < inverse.length; i++) {
      assertEquals(inverse[i].getReal(), A[i], EPS);
    }
  }

  private void assertComplexEquals(final ComplexNumber z1, final ComplexNumber z2) {
    assertEquals(z1.getReal(), z2.getReal(), EPS);
    assertEquals(z1.getImaginary(), z2.getImaginary(), EPS);
  }
}
