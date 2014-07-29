/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.BitSet;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TransformParametersTest {
  private static final DoubleMatrix1D INIT = new DoubleMatrix1D(new double[] {1, 2, 3, 4});
  private static final ParameterLimitsTransform[] NULLS = new ParameterLimitsTransform[] {new NullTransform(), new NullTransform(), new NullTransform(), new NullTransform()};
  private static final BitSet FIXED = new BitSet(4);
  private static final UncoupledParameterTransforms PARAMS;

  static {
    FIXED.set(0);
    PARAMS = new UncoupledParameterTransforms(INIT, NULLS, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartValues() {
    new UncoupledParameterTransforms(null, NULLS, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTransforms() {
    new UncoupledParameterTransforms(INIT, null, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTransforms() {
    new UncoupledParameterTransforms(INIT, new ParameterLimitsTransform[0], FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBitSet() {
    new UncoupledParameterTransforms(INIT, NULLS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAllFixed() {
    final BitSet allFixed = new BitSet();
    allFixed.set(0);
    allFixed.set(1);
    allFixed.set(2);
    allFixed.set(3);
    new UncoupledParameterTransforms(INIT, NULLS, allFixed);
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
    assertEquals(PARAMS.getNumberOfModelParameters(), 4);
    assertEquals(PARAMS.getNumberOfFittingParameters(), 3);
    UncoupledParameterTransforms other = new UncoupledParameterTransforms(INIT, NULLS, FIXED);
    assertEquals(PARAMS, other);
    assertEquals(PARAMS.hashCode(), other.hashCode());
    other = new UncoupledParameterTransforms(new DoubleMatrix1D(new double[] {1, 2, 4, 5}), NULLS, FIXED);
    assertFalse(other.equals(PARAMS));
    other = new UncoupledParameterTransforms(INIT, new ParameterLimitsTransform[] {new DoubleRangeLimitTransform(1, 2), new NullTransform(), new NullTransform(), new NullTransform()}, FIXED);
    assertFalse(other.equals(PARAMS));
    other = new UncoupledParameterTransforms(INIT, NULLS, new BitSet(4));
    assertFalse(other.equals(PARAMS));
  }

  @Test
  public void testTransformAndInverse() {
    final DoubleMatrix1D functionParameters = new DoubleMatrix1D(new double[] {1, 2, 6, 4});
    assertEquals(PARAMS.inverseTransform(PARAMS.transform(functionParameters)), functionParameters);
  }
}
