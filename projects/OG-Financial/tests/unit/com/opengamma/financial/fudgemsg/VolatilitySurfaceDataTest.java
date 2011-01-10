/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceData;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge serialization test for VolatilitySurfaceSpecification
 */
public class VolatilitySurfaceDataTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    Tenor[] oneToTenYears = new Tenor[10];
    for (int i=1; i<=10; i++) {
      oneToTenYears[i-1] = Tenor.ofYears(i);
    }
    Map<Pair<Tenor, Tenor>, Double> values = new HashMap<Pair<Tenor, Tenor>, Double>();
    for (Tenor tenorX : oneToTenYears) {
      for (Tenor tenorY : oneToTenYears) {
        values.put(Pair.of(tenorX, tenorY), Math.random());
      }
    }
    VolatilitySurfaceData<Tenor, Tenor> def = new VolatilitySurfaceData<Tenor, Tenor>("US", "US", Currency.getInstance("USD"), Interpolator1DFactory.NATURAL_CUBIC_SPLINE, oneToTenYears, oneToTenYears, values);
    assertEquals(def, cycleObject(VolatilitySurfaceData.class, def));
  }
}
