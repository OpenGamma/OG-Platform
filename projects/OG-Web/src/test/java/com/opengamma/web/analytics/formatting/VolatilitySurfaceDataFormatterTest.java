/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceDataFormatterTest {

  @SuppressWarnings("unchecked")
  @Test
  public void expandedRectangular() {
    Tenor[] xVals = new Tenor[]{Tenor.DAY, Tenor.ONE_WEEK, Tenor.TWO_WEEKS};
    Tenor[] yVals = new Tenor[]{Tenor.ONE_YEAR, Tenor.TWO_YEARS};
    double[] vols = new double[] {10, 11, 12, 13, 14, 15};
    Map<Pair<Tenor, Tenor>, Double> vol = Maps.newHashMap();
    for (int y = 0; y < yVals.length; y++) {
      for (int x = 0; x < xVals.length; x++) {
        vol.put(Pairs.of(xVals[x], yVals[y]), vols[x + (y * xVals.length)]);
      }
    }
    String name = "test";
    UniqueIdentifiable target = Currency.USD;
    VolatilitySurfaceData<Tenor, Tenor> data = new VolatilitySurfaceData<>(name, name, target, xVals, yVals, vol);

    Map<String, Object> map =
        (Map<String, Object>) new VolatilitySurfaceDataFormatter().format(data, null, TypeFormatter.Format.EXPANDED, null);
    assertEquals(Lists.newArrayList("1D", "7D", "14D"), map.get(SurfaceFormatterUtils.X_LABELS));
    assertEquals(Lists.newArrayList("1Y", "2Y"), map.get(SurfaceFormatterUtils.Y_LABELS));
    assertEquals(Lists.newArrayList(10d, 11d, 12d, 13d, 14d, 15d), map.get(SurfaceFormatterUtils.VOL));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void expandedRagged() {
    Tenor[] xs = new Tenor[]{Tenor.DAY,      Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.TWO_WEEKS, Tenor.ONE_MONTH};
    Tenor[] ys = new Tenor[]{Tenor.ONE_YEAR, Tenor.ONE_YEAR, Tenor.ONE_YEAR,  Tenor.TWO_YEARS, Tenor.TWO_YEARS};
    double[] vols = new double[] {10, 11, 12, 13, 14};
    Map<Pair<Tenor, Tenor>, Double> values = Maps.newHashMap();
    for (int i = 0; i < xs.length; i++) {
      values.put(Pairs.of(xs[i], ys[i]), vols[i]);
    }
    String name = "test";
    UniqueIdentifiable target = Currency.USD;
    VolatilitySurfaceData<Tenor, Tenor> data = new VolatilitySurfaceData<>(name, name, target, xs, ys, values);

    Map<String, Object> map =
        (Map<String, Object>) new VolatilitySurfaceDataFormatter().format(data, null, TypeFormatter.Format.EXPANDED, null);
    assertEquals(Lists.newArrayList("1D", "7D", "14D", "1M"), map.get(SurfaceFormatterUtils.X_LABELS));
    assertEquals(Lists.newArrayList("1Y", "2Y"), map.get(SurfaceFormatterUtils.Y_LABELS));
    assertEquals(Lists.newArrayList(10d, 11d, 12d, null, null, null, 13d, 14d), map.get(SurfaceFormatterUtils.VOL));
  }
}
