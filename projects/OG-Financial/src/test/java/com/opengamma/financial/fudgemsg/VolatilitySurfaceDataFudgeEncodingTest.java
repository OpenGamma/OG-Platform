/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceDataFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final Tenor[] oneToTenYears = new Tenor[10];
    for (int i = 1; i <= 10; i++) {
      oneToTenYears[i - 1] = Tenor.ofYears(i);
    }
    final Map<Pair<Tenor, Tenor>, Double> values = new HashMap<Pair<Tenor, Tenor>, Double>();
    for (final Tenor tenorX : oneToTenYears) {
      for (final Tenor tenorY : oneToTenYears) {
        values.put(Pairs.of(tenorX, tenorY), Math.random());
      }
    }
    final VolatilitySurfaceData<Tenor, Tenor> data = new VolatilitySurfaceData<Tenor, Tenor>("US", "US", Currency.USD, oneToTenYears, oneToTenYears, values);
    assertEquals(data, cycleObject(VolatilitySurfaceData.class, data));
    final VolatilitySurfaceData<Tenor, Tenor> dataWithName = new VolatilitySurfaceData<Tenor, Tenor>("US", "US", Currency.USD, oneToTenYears, "time", oneToTenYears, "strike", values);
    assertEquals(dataWithName, cycleObject(VolatilitySurfaceData.class, dataWithName));
  }
}
