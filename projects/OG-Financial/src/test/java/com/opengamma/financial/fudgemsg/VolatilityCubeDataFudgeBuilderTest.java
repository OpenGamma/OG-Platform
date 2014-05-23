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

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataFudgeBuilderTest extends FinancialTestBase {

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
    final Map<Triple<Tenor, Tenor, Double>, Double> values = new HashMap<>();
    for (final Tenor tenorX : x) {
      for (final Tenor tenorY : y) {
        for (final Double doubleZ : z) {
          values.put(Triple.of(tenorX, tenorY, doubleZ), Math.random());
        }
      }
    }
    VolatilityCubeData<Tenor, Tenor, Double> data = new VolatilityCubeData<>("def", "spec", "x1", "y1", "z1", values);
    VolatilityCubeData<Tenor, Tenor, Double> cycled = cycleObject(VolatilityCubeData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getDefinitionName(), cycled.getDefinitionName());
    assertEquals(data.getSpecificationName(), cycled.getSpecificationName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
    assertEquals(data.getZLabel(), cycled.getZLabel());
    data = new VolatilityCubeData<>("def", "spec", values);
    cycled = cycleObject(VolatilityCubeData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getDefinitionName(), cycled.getDefinitionName());
    assertEquals(data.getSpecificationName(), cycled.getSpecificationName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
    assertEquals(data.getZLabel(), cycled.getZLabel());
  }

  @Test
  public void testEmptyCycle() {
    final Map<Triple<Tenor, Tenor, Double>, Double> values = new HashMap<>();
    VolatilityCubeData<Tenor, Tenor, Double> data = new VolatilityCubeData<>("def", "spec", "x1", "y1", "z1", values);
    VolatilityCubeData<Tenor, Tenor, Double> cycled = cycleObject(VolatilityCubeData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getDefinitionName(), cycled.getDefinitionName());
    assertEquals(data.getSpecificationName(), cycled.getSpecificationName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
    assertEquals(data.getZLabel(), cycled.getZLabel());
    assertEquals(data, cycleObject(VolatilityCubeData.class, data));
    data = new VolatilityCubeData<>("def", "spec", values);
    cycled = cycleObject(VolatilityCubeData.class, data);
    assertEquals(data.asMap(), cycled.asMap());
    assertEquals(data.getDefinitionName(), cycled.getDefinitionName());
    assertEquals(data.getSpecificationName(), cycled.getSpecificationName());
    assertEquals(data.getXLabel(), cycled.getXLabel());
    assertEquals(data.getYLabel(), cycled.getYLabel());
    assertEquals(data.getZLabel(), cycled.getZLabel());
  }
}
