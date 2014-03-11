/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.SurfaceData;
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
    final Tenor[] x = new Tenor[10];
    final Tenor[] y = new Tenor[10];
    for (int i = 1; i <= 10; i++) {
      x[i - 1] = Tenor.ofYears(i);
      y[i - 1] = Tenor.ofYears(i + 10);
    }
    final Map<Pair<Tenor, Tenor>, Double> values = new HashMap<>();
    for (final Tenor tenorX : x) {
      for (final Tenor tenorY : y) {
        values.put(Pairs.of(tenorX, tenorY), Math.random());
      }
    }
    SurfaceData<Tenor, Tenor> data = new SurfaceData<>("US", values);
    SurfaceData<Tenor, Tenor> cycled = cycleObject(SurfaceData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getName(), cycled.getName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
    data = new SurfaceData<>("def", values);
    cycled = cycleObject(SurfaceData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getName(), cycled.getName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
  }

  @Test
  public void testEmptyCycle() {
    final Map<Pair<Tenor, Tenor>, Double> values = new HashMap<>();
    final SurfaceData<Tenor, Tenor> data = new SurfaceData<>("US", values);
    assertEquals(Collections.emptyMap(), data.asMap());
    final SurfaceData<Tenor, Tenor> dataWithName = new SurfaceData<>("US", "time", "strike", values);
    assertEquals(dataWithName, cycleObject(SurfaceData.class, dataWithName));
    assertEquals(Collections.emptyMap(), dataWithName.asMap());
  }
}
