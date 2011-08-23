/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.BitSet;

import org.testng.annotations.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class TransformParametersTest {
  private static final DoubleMatrix1D INIT = new DoubleMatrix1D(new double[] {1, 2, 3, 4});
  private static final ParameterLimitsTransform[] NULLS = new ParameterLimitsTransform[] {new NullTransform(), new NullTransform(), new NullTransform(), new NullTransform()};
  private static final BitSet FIXED = new BitSet(4);
  private static final TransformParameters PARAMS;

  static {
    FIXED.set(0);
    PARAMS = new TransformParameters(INIT, NULLS, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartValues() {
    new TransformParameters(null, NULLS, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTransforms() {
    new TransformParameters(INIT, null, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTransforms() {
    new TransformParameters(INIT, new ParameterLimitsTransform[0], FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBitSet() {
    new TransformParameters(INIT, NULLS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAllFixed() {
    final BitSet allFixed = new BitSet();
    allFixed.set(0);
    allFixed.set(1);
    allFixed.set(2);
    allFixed.set(3);
    new TransformParameters(INIT, NULLS, allFixed);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTransformNullParameters() {
    PARAMS.transform(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTransformWrongParameters() {
    PARAMS.transform(new DoubleMatrix1D(new double[] {1, 2}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInverseTransformNullParameters() {
    PARAMS.inverseTransform(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInverseTransformWrongParameters() {
    PARAMS.inverseTransform(new DoubleMatrix1D(new double[] {1, 2}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJacobianNullParameters() {
    PARAMS.jacobian(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJacobianWrongParameters() {
    PARAMS.jacobian(new DoubleMatrix1D(new double[] {1, 2}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInverseJacobianNullParameters() {
    PARAMS.inverseJacobian(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInverseJacobianWrongParameters() {
    PARAMS.inverseJacobian(new DoubleMatrix1D(new double[] {1, 2}));
  }

  @Test
  public void test() {
    assertEquals(PARAMS.getNumberOfFunctionParameters(), 4);
    assertEquals(PARAMS.getNumberOfFittingParameters(), 3);
    TransformParameters other = new TransformParameters(INIT, NULLS, FIXED);
    assertEquals(PARAMS, other);
    assertEquals(PARAMS.hashCode(), other.hashCode());
    other = new TransformParameters(new DoubleMatrix1D(new double[] {1, 2, 4, 5}), NULLS, FIXED);
    assertFalse(other.equals(PARAMS));
    other = new TransformParameters(INIT, new ParameterLimitsTransform[] {new DoubleRangeLimitTransform(1, 2), new NullTransform(), new NullTransform(), new NullTransform()}, FIXED);
    assertFalse(other.equals(PARAMS));
    other = new TransformParameters(INIT, NULLS, new BitSet(4));
    assertFalse(other.equals(PARAMS));
  }

  @Test
  public void testTransformAndInverse() {
    final DoubleMatrix1D functionParameters = new DoubleMatrix1D(new double[] {1, 2, 6, 4});
    assertEquals(PARAMS.inverseTransform(PARAMS.transform(functionParameters)), functionParameters);
  }
}
