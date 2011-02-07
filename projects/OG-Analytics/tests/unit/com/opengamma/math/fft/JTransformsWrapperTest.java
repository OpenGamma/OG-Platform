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
      return Math.cos(6 * Math.PI * t) + Math.cos(8 * Math.PI * t);
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
    JTransformsWrapper.transformComplex(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    JTransformsWrapper.inverseTransformComplex(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull3() {
    JTransformsWrapper.fullTransformReal(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull4() {
    JTransformsWrapper.fullInverseTransformReal(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty1() {
    JTransformsWrapper.transformComplex(new ComplexNumber[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty2() {
    JTransformsWrapper.inverseTransformComplex(new ComplexNumber[0], false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty3() {
    JTransformsWrapper.fullTransformReal(new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty4() {
    JTransformsWrapper.fullInverseTransformReal(new double[0], false);
  }

  @Test
  public void testForwardBackward() {
    final int n = A.length;
    final ComplexNumber[] transform = JTransformsWrapper.fullTransformReal(A);
    ComplexNumber[] inverse = JTransformsWrapper.inverseTransformComplex(transform, true);
    final double[] realTransform = new double[n];
    final ComplexNumber[] complex = new ComplexNumber[n];
    for (int i = 0; i < n; i++) {
      realTransform[i] = transform[i].getReal();
      complex[i] = new ComplexNumber(A[i], 0);
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }
    final ComplexNumber[] transformComplex = JTransformsWrapper.transformComplex(complex);
    inverse = JTransformsWrapper.fullInverseTransformReal(realTransform, true);
    for (int i = 0; i < n; i++) {
      assertComplexEquals(transform[i], transformComplex[i]);
      assertComplexEquals(inverse[i], new ComplexNumber(A[i], 0));
    }
  }

  private void assertComplexEquals(final ComplexNumber z1, final ComplexNumber z2) {
    assertEquals(z1.getReal(), z2.getReal(), EPS);
    assertEquals(z1.getImaginary(), z2.getImaginary(), EPS);
  }
}
