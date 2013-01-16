/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Tests the {@link ComputationTargetReference} class
 */
@Test
public class ComputationTargetReferenceTest {

  public void testGetTypeDepth() {
    assertEquals(ComputationTargetReference.getTypeDepth(ComputationTargetType.NULL), 0);
    assertEquals(ComputationTargetReference.getTypeDepth(ComputationTargetType.SECURITY), 1);
    assertEquals(ComputationTargetReference.getTypeDepth(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)), 2);
    assertEquals(ComputationTargetReference.getTypeDepth(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE)), 1);
    assertEquals(
        ComputationTargetReference.getTypeDepth(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).or(
            ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE))), 2);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testGetTypeDepth_invalid() {
    ComputationTargetReference.getTypeDepth(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).or(ComputationTargetType.SECURITY));
  }

}
