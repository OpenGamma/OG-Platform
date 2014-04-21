/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.surface.SurfaceDefinition;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SurfaceDefinitionFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final Tenor[] x = new Tenor[10];
    final Tenor[] y = new Tenor[10];
    for (int i = 1; i <= 10; i++) {
      x[i - 1] = Tenor.ofYears(i);
      y[i - 1] = Tenor.ofYears(i + 10);
    }
    final SurfaceDefinition<Tenor, Tenor> def = new SurfaceDefinition<>("US", x, y);
    assertEquals(def, cycleObject(SurfaceDefinition.class, def));
  }
}
