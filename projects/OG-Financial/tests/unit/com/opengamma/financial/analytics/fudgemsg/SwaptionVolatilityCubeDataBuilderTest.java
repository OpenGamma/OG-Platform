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

import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeData;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SwaptionVolatilityCubeDataBuilderTest extends AnalyticsTestBase {
  private static final Tenor[] SWAP_TENORS = new Tenor[] {Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.FIVE_YEARS };
  private static final Tenor[] SWAPTION_EXPIRY = new Tenor[] {Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SEVEN_MONTHS };
  private static final Double[] RELATIVE_STRIKES = new Double[] {-100., -50., 0., 25., 75. };
  private static final SwaptionVolatilityCubeData DATA;

  static {
    final Map<VolatilityPoint, Double> data = new HashMap<VolatilityPoint, Double>();
    for (final Tenor swapTenor : SWAP_TENORS) {
      for (final Tenor swaptionExpiry : SWAPTION_EXPIRY) {
        for (final Double relativeStrike : RELATIVE_STRIKES) {
          final VolatilityPoint coordinate = new VolatilityPoint(swapTenor, swaptionExpiry, relativeStrike);
          final double vol = Math.random();
          data.put(coordinate, vol);
        }
      }
    }
    DATA = new SwaptionVolatilityCubeData(data);
  }

  @Test
  public void test() {
    final SwaptionVolatilityCubeData cycled = cycleObject(SwaptionVolatilityCubeData.class, DATA);
    assertEquals(cycled, DATA);
  }
}
