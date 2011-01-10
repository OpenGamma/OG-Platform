/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class DecompositionFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadName() {
    DecompositionFactory.getDecomposition("X");
  }

  @Test
  public void testNullDecomposition() {
    DecompositionFactory.getDecompositionName(null);
  }

  @Test
  public void test() {
    assertEquals(DecompositionFactory.LU_COMMONS_NAME, DecompositionFactory.getDecompositionName(DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME)));
    assertEquals(DecompositionFactory.QR_COMMONS_NAME, DecompositionFactory.getDecompositionName(DecompositionFactory.getDecomposition(DecompositionFactory.QR_COMMONS_NAME)));
    assertEquals(DecompositionFactory.SV_COMMONS_NAME, DecompositionFactory.getDecompositionName(DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME)));
    assertEquals(DecompositionFactory.SV_COLT_NAME, DecompositionFactory.getDecompositionName(DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME)));
  }
}
