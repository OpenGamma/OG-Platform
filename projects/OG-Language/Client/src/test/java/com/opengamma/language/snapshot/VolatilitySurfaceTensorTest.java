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
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the volatility surface tensor functions.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceTensorTest {

  @SuppressWarnings("unchecked")
  private ManageableVolatilitySurfaceSnapshot createSnapshot() {
    final ManageableVolatilitySurfaceSnapshot snapshot = new ManageableVolatilitySurfaceSnapshot();
    final Map<Pair<? extends Object, ? extends Object>, ValueSnapshot> values = new HashMap<Pair<? extends Object, ? extends Object>, ValueSnapshot>();
    values.put(Pair.of("A", "I"), new ValueSnapshot(0.1, null));
    values.put(Pair.of("B", "I"), new ValueSnapshot(0.2, 0.25));
    values.put(Pair.of("C", "J"), new ValueSnapshot(null, 0.35));
    snapshot.setValues((Map<Pair<Object, Object>, ValueSnapshot>) (Map<?, ?>) values);
    return snapshot;
  }

  public void testGetMarketValue() {
    final ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    final Value[][] values = GetVolatilitySurfaceTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.FALSE);
    assertEquals(values[0][0].getDoubleValue(), 0.1);
    assertEquals(values[0][1].getDoubleValue(), 0.2);
    assertEquals(values[0][2].getDoubleValue(), null);
    assertEquals(values[1][0].getDoubleValue(), null);
    assertEquals(values[1][1].getDoubleValue(), null);
    assertEquals(values[1][2].getDoubleValue(), null);
  }

  public void testGetOverrideValue() {
    final ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    final Value[][] values = GetVolatilitySurfaceTensorFunction.invoke(snapshot, Boolean.FALSE, Boolean.TRUE);
    assertEquals(values[0][0].getDoubleValue(), null);
    assertEquals(values[0][1].getDoubleValue(), 0.25);
    assertEquals(values[0][2].getDoubleValue(), null);
    assertEquals(values[1][0].getDoubleValue(), null);
    assertEquals(values[1][1].getDoubleValue(), null);
    assertEquals(values[1][2].getDoubleValue(), 0.35);
  }

  public void testGetMixedValue() {
    final ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    final Value[][] values = GetVolatilitySurfaceTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.TRUE);
    assertEquals(values[0][0].getDoubleValue(), 0.1);
    assertEquals(values[0][1].getDoubleValue(), 0.25);
    assertEquals(values[0][2].getDoubleValue(), null);
    assertEquals(values[1][0].getDoubleValue(), null);
    assertEquals(values[1][1].getDoubleValue(), null);
    assertEquals(values[1][2].getDoubleValue(), 0.35);
  }

  private void assertValue(final ManageableVolatilitySurfaceSnapshot snapshot, final Object x, final Object y, final Double expectedMarket, final Double expectedOverride) {
    final ValueSnapshot value = snapshot.getValues().get(Pair.of(x, y));
    assertEquals(value.getOverrideValue(), expectedOverride);
    assertEquals(value.getMarketValue(), expectedMarket);
  }

  public void testUpdateMarketValue() {
    ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilitySurfaceTensorFunction.invoke(snapshot, null, new Value[][] { {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) },
        {ValueUtils.of(0.8), ValueUtils.of(0.9), ValueUtils.of(1.0) } });
    assertValue(snapshot, "A", "I", 0.5, null);
    assertValue(snapshot, "B", "I", 0.6, 0.25);
    assertValue(snapshot, "C", "I", 0.7, null);
    assertValue(snapshot, "A", "J", 0.8, null);
    assertValue(snapshot, "B", "J", 0.9, null);
    assertValue(snapshot, "C", "J", 1.0, 0.35);
  }

  public void testUpdateOverrideValue() {
    ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilitySurfaceTensorFunction.invoke(snapshot,
        new Value[][] { {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) }, {ValueUtils.of(0.8), ValueUtils.of(0.9), ValueUtils.of(1.0) } }, null);
    assertValue(snapshot, "A", "I", 0.1, 0.5);
    assertValue(snapshot, "B", "I", 0.2, 0.6);
    assertValue(snapshot, "C", "I", null, 0.7);
    assertValue(snapshot, "A", "J", null, 0.8);
    assertValue(snapshot, "B", "J", null, 0.9);
    assertValue(snapshot, "C", "J", null, 1.0);
  }

  public void testUpdateMixedValue() {
    ManageableVolatilitySurfaceSnapshot snapshot = createSnapshot();
    snapshot = SetVolatilitySurfaceTensorFunction.invoke(snapshot,
        new Value[][] { {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) }, {ValueUtils.of(0.8), ValueUtils.of(0.9), ValueUtils.of(1.0) } },
        new Value[][] { {ValueUtils.of(1.1), ValueUtils.of(1.2), ValueUtils.of(1.3) }, {ValueUtils.of(1.4), ValueUtils.of(1.5), ValueUtils.of(1.6) } });
    assertValue(snapshot, "A", "I", 1.1, 0.5);
    assertValue(snapshot, "B", "I", 1.2, 0.6);
    assertValue(snapshot, "C", "I", 1.3, 0.7);
    assertValue(snapshot, "A", "J", 1.4, 0.8);
    assertValue(snapshot, "B", "J", 1.5, 0.9);
    assertValue(snapshot, "C", "J", 1.6, 1.0);
  }

}
