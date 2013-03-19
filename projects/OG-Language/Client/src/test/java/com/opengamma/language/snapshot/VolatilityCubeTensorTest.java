/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.snapshot;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests the volatility cube tensor functions.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeTensorTest {

  private ManageableVolatilityCubeSnapshot createSnapshot() {
    final ManageableVolatilityCubeSnapshot snapshot = new ManageableVolatilityCubeSnapshot();
    final Map<VolatilityPoint, ValueSnapshot> values = new HashMap<VolatilityPoint, ValueSnapshot>();
    values.put(new VolatilityPoint(Tenor.DAY, Tenor.DAY, 0), new ValueSnapshot(0.1, null));
    values.put(new VolatilityPoint(Tenor.DAY, Tenor.YEAR, 0), new ValueSnapshot(0.2, 0.25));
    values.put(new VolatilityPoint(Tenor.YEAR, Tenor.YEAR, 4.2), new ValueSnapshot(null, 0.35));
    snapshot.setValues(values);
    return snapshot;
  }

  public void testGetMarketValue() {
    final ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    final Value[][][] values = GetVolatilityCubeTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.FALSE);
    assertEquals(values[0][0][0].getDoubleValue(), 0.1);
    assertEquals(values[0][0][1].getDoubleValue(), null);
    assertEquals(values[0][1][0].getDoubleValue(), 0.2);
    assertEquals(values[0][1][1].getDoubleValue(), null);
    assertEquals(values[1][0][0].getDoubleValue(), null);
    assertEquals(values[1][0][1].getDoubleValue(), null);
    assertEquals(values[1][1][0].getDoubleValue(), null);
    assertEquals(values[1][1][1].getDoubleValue(), null);
  }

  public void testGetOverrideValue() {
    final ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    final Value[][][] values = GetVolatilityCubeTensorFunction.invoke(snapshot, Boolean.FALSE, Boolean.TRUE);
    assertEquals(values[0][0][0].getDoubleValue(), null);
    assertEquals(values[0][0][1].getDoubleValue(), null);
    assertEquals(values[0][1][0].getDoubleValue(), 0.25);
    assertEquals(values[0][1][1].getDoubleValue(), null);
    assertEquals(values[1][0][0].getDoubleValue(), null);
    assertEquals(values[1][0][1].getDoubleValue(), null);
    assertEquals(values[1][1][0].getDoubleValue(), null);
    assertEquals(values[1][1][1].getDoubleValue(), 0.35);
  }

  public void testGetMixedValue() {
    final ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    final Value[][][] values = GetVolatilityCubeTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.TRUE);
    assertEquals(values[0][0][0].getDoubleValue(), 0.1);
    assertEquals(values[0][0][1].getDoubleValue(), null);
    assertEquals(values[0][1][0].getDoubleValue(), 0.25);
    assertEquals(values[0][1][1].getDoubleValue(), null);
    assertEquals(values[1][0][0].getDoubleValue(), null);
    assertEquals(values[1][0][1].getDoubleValue(), null);
    assertEquals(values[1][1][0].getDoubleValue(), null);
    assertEquals(values[1][1][1].getDoubleValue(), 0.35);
  }

  private void assertValue(final ManageableVolatilityCubeSnapshot snapshot, final Tenor x, final Tenor y, double z, final Double expectedMarket, final Double expectedOverride) {
    final ValueSnapshot value = snapshot.getValues().get(new VolatilityPoint(x, y, z));
    assertEquals(value.getOverrideValue(), expectedOverride);
    assertEquals(value.getMarketValue(), expectedMarket);
  }

  public void testUpdateMarketValue() {
    ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilityCubeTensorFunction.invoke(snapshot, null, new Value[][][] { { {ValueUtils.of(0.5), ValueUtils.of(0.6) }, {ValueUtils.of(0.7), ValueUtils.of(0.8) } },
        { {ValueUtils.of(0.9), ValueUtils.of(1.0) }, {ValueUtils.of(1.1), ValueUtils.of(1.2) } } });
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 0, 0.5, null);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 0, 0.6, null);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 0, 0.7, 0.25);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 0, 0.8, null);
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 4.2, 0.9, null);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 4.2, 1.0, null);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 4.2, 1.1, null);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 4.2, 1.2, 0.35);
  }

  public void testUpdateOverrideValue() {
    ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilityCubeTensorFunction.invoke(snapshot, new Value[][][] { { {ValueUtils.of(0.5), ValueUtils.of(0.6) }, {ValueUtils.of(0.7), ValueUtils.of(0.8) } },
        { {ValueUtils.of(0.9), ValueUtils.of(1.0) }, {ValueUtils.of(1.1), ValueUtils.of(1.2) } } }, null);
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 0, 0.1, 0.5);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 0, null, 0.6);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 0, 0.2, 0.7);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 0, null, 0.8);
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 4.2, null, 0.9);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 4.2, null, 1.0);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 4.2, null, 1.1);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 4.2, null, 1.2);
  }

  public void testUpdateMixedValue() {
    ManageableVolatilityCubeSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilityCubeTensorFunction
        .invoke(snapshot, new Value[][][] { { {ValueUtils.of(0.5), ValueUtils.of(0.6) }, {ValueUtils.of(0.7), ValueUtils.of(0.8) } },
            { {ValueUtils.of(0.9), ValueUtils.of(1.0) }, {ValueUtils.of(1.1), ValueUtils.of(1.2) } } },
            new Value[][][] { { {ValueUtils.of(1.3), ValueUtils.of(1.4) }, {ValueUtils.of(1.5), ValueUtils.of(1.6) } }, { {ValueUtils.of(1.7), ValueUtils.of(1.8) },
                {ValueUtils.of(1.9), ValueUtils.of(2.0) } } });
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 0, 1.3, 0.5);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 0, 1.4, 0.6);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 0, 1.5, 0.7);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 0, 1.6, 0.8);
    assertValue(snapshot, Tenor.DAY, Tenor.DAY, 4.2, 1.7, 0.9);
    assertValue(snapshot, Tenor.YEAR, Tenor.DAY, 4.2, 1.8, 1.0);
    assertValue(snapshot, Tenor.DAY, Tenor.YEAR, 4.2, 1.9, 1.1);
    assertValue(snapshot, Tenor.YEAR, Tenor.YEAR, 4.2, 2.0, 1.2);
  }

}
