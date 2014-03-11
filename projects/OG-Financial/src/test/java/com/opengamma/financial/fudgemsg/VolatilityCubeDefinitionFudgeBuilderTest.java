/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDefinitionFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final Tenor[] x = new Tenor[10];
    final Tenor[] y = new Tenor[10];
    final Double[] z = new Double[10];
    for (int i = 1; i <= 10; i++) {
      x[i - 1] = Tenor.ofYears(i);
      y[i - 1] = Tenor.ofYears(i + 10);
      z[i - 1] = Double.valueOf(i);
    }
    final VolatilityCubeDefinition<Tenor, Tenor, Double> def = new VolatilityCubeDefinition<>("name", "type", x, y, z);
    assertEquals(def, cycleObject(VolatilityCubeDefinition.class, def));
  }
}
