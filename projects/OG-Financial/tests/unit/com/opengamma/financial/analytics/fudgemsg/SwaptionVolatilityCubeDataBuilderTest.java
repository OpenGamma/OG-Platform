/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeData;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class SwaptionVolatilityCubeDataBuilderTest extends AnalyticsTestBase {
  private static final Object[] SWAP_TENORS = new Tenor[] {Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.FIVE_YEARS };
  private static final Object[] SWAPTION_EXPIRY = new Tenor[] {Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SEVEN_MONTHS };
  private static final Object[] DELTAS = new Double[] {0.15, 0.2, 0.5, 0.75, 0.9 };
  private static final SwaptionVolatilityCubeData<Object, Object, Object> DATA;

  static {
    final Map<Triple<Object, Object, Object>, Double> data = new HashMap<Triple<Object, Object, Object>, Double>();
    for (final Object element : SWAP_TENORS) {
      for (final Object element2 : SWAPTION_EXPIRY) {
        for (final Object element3 : DELTAS) {
          final Triple<Object, Object, Object> coordinate = Triple.of(element, element2, element3);
          final double vol = Math.random();
          data.put(coordinate, vol);
        }
      }
    }
    DATA = new SwaptionVolatilityCubeData<Object, Object, Object>(data);
  }

  @Test
  public void test() {
    @SuppressWarnings("unchecked")
    final SwaptionVolatilityCubeData<Object, Object, Object> cycled = cycleObject(SwaptionVolatilityCubeData.class, DATA);
    assertEquals(cycled, DATA);
  }
}
