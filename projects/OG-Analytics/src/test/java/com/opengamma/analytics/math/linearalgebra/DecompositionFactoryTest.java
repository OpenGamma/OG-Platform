/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DecompositionFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
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
