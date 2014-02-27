/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;

/**
 * Tests {@link VolatilityCubeDefinitionFudgeBuilder}.
 */
public class VolatilityCubeDefinitionFudgeBuilderTest extends FinancialTestBase {

  /**
   * Tests a cycle.
   */
  @Test
  public void test() {
    final String name = "definition";
    final Double[] xs = new Double[] {1., 2., 3., 4., 5. };
    final Double[] ys = new Double[] {11., 12., 13., 14., 15. };
    final Double[] zs = new Double[] {21., 22., 23., 24., 25. };
    final VolatilityCubeDefinition<Double, Double, Double> definition = new VolatilityCubeDefinition<>(name, xs, ys, zs);
    assertEquals(definition, cycleObject(VolatilityCubeDefinition.class, definition));
  }
}
