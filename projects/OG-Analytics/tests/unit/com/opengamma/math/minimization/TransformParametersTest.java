/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.BitSet;

import org.junit.Test;

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

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartValues() {
    new TransformParameters(null, NULLS, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTransforms() {
    new TransformParameters(INIT, null, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTransforms() {
    new TransformParameters(INIT, new ParameterLimitsTransform[0], FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBitSet() {
    new TransformParameters(INIT, NULLS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllFixed() {
    final BitSet allFixed = new BitSet();
    allFixed.set(0);
    allFixed.set(1);
    allFixed.set(2);
    allFixed.set(3);
    new TransformParameters(INIT, NULLS, allFixed);
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
}
