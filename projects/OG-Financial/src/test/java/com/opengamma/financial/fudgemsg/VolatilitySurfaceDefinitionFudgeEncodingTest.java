/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceDefinitionFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final Tenor[] oneToTenYears = new Tenor[10];
    for (int i = 1; i <= 10; i++) {
      oneToTenYears[i - 1] = Tenor.ofYears(i);
    }
    final VolatilitySurfaceDefinition<Tenor, Tenor> def = new VolatilitySurfaceDefinition<>("US", Currency.USD, oneToTenYears, oneToTenYears);
    assertEquals(def, cycleObject(VolatilitySurfaceDefinition.class, def));
  }
}
