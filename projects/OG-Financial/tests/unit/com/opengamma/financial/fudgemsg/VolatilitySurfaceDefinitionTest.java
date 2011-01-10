/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.Tenor;

/**
 * Fudge serialization test for VolatilitySurfaceSpecification
 */
public class VolatilitySurfaceDefinitionTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    Tenor[] oneToTenYears = new Tenor[10];
    for (int i=1; i<=10; i++) {
      oneToTenYears[i-1] = Tenor.ofYears(i);
    }
    VolatilitySurfaceDefinition<Tenor, Tenor> def = new VolatilitySurfaceDefinition<Tenor, Tenor>("US", Currency.getInstance("USD"), Interpolator1DFactory.NATURAL_CUBIC_SPLINE, oneToTenYears, oneToTenYears);
    assertEquals(def, cycleObject(VolatilitySurfaceDefinition.class, def));
  }
}
