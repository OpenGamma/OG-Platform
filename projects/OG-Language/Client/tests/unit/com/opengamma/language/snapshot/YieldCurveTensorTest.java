/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.snapshot;

import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;

/**
 * Tests the yield curve tensor functions.
 */
@Test
public class YieldCurveTensorTest {

  @SuppressWarnings("unchecked")
  private ManageableYieldCurveSnapshot createSnapshot() {
    final ManageableYieldCurveSnapshot snapshot = new ManageableYieldCurveSnapshot();
    final ManageableUnstructuredMarketDataSnapshot values = new ManageableUnstructuredMarketDataSnapshot();
    values.setValues(new LinkedHashMap());
    UnstructuredMarketDataSnapshotUtil.setValue(values, "V", UniqueId.of("Test", "A"), null, 0.1);
    UnstructuredMarketDataSnapshotUtil.setValue(values, "V", UniqueId.of("Test", "B"), 0.25, 0.2);
    UnstructuredMarketDataSnapshotUtil.setValue(values, "V", UniqueId.of("Test", "C"), 0.35, null);
    snapshot.setValues(values);
    return snapshot;
  }

  public void testGetMarketValue() {
    final ManageableYieldCurveSnapshot snapshot = createSnapshot();
    final Value[] values = GetYieldCurveTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.FALSE);
    assertEquals(values[0].getDoubleValue(), 0.1);
    assertEquals(values[1].getDoubleValue(), 0.2);
    assertEquals(values[2].getDoubleValue(), null);
  }

  public void testGetOverrideValue() {
    final ManageableYieldCurveSnapshot snapshot = createSnapshot();
    final Value[] values = GetYieldCurveTensorFunction.invoke(snapshot, Boolean.FALSE, Boolean.TRUE);
    assertEquals(values[0].getDoubleValue(), null);
    assertEquals(values[1].getDoubleValue(), 0.25);
    assertEquals(values[2].getDoubleValue(), 0.35);
  }

  public void testGetMixedValue() {
    final ManageableYieldCurveSnapshot snapshot = createSnapshot();
    final Value[] values = GetYieldCurveTensorFunction.invoke(snapshot, Boolean.TRUE, Boolean.TRUE);
    assertEquals(values[0].getDoubleValue(), 0.1);
    assertEquals(values[1].getDoubleValue(), 0.25);
    assertEquals(values[2].getDoubleValue(), 0.35);
  }

  private void assertValue(final ManageableYieldCurveSnapshot snapshot, final String identifier, final Double expectedOverride, final Double expectedMarket) {
    final List<Double> value = UnstructuredMarketDataSnapshotUtil.getValue(snapshot.getValues(), "V", UniqueId.of("Test", identifier));
    assertEquals(value.get(0), expectedOverride);
    assertEquals(value.get(1), expectedMarket);
  }

  public void testUpdateMarketValue() {
    ManageableYieldCurveSnapshot snapshot = createSnapshot();
    snapshot = SetYieldCurveTensorFunction.invoke(snapshot, null, new Value[] {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) });
    assertValue(snapshot, "A", null, 0.5);
    assertValue(snapshot, "B", 0.25, 0.6);
    assertValue(snapshot, "C", 0.35, 0.7);
  }

  public void testUpdateOverrideValue() {
    ManageableYieldCurveSnapshot snapshot = createSnapshot();
    snapshot = SetYieldCurveTensorFunction.invoke(snapshot, new Value[] {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) }, null);
    assertValue(snapshot, "A", 0.5, 0.1);
    assertValue(snapshot, "B", 0.6, 0.2);
    assertValue(snapshot, "C", 0.7, null);
  }

  public void testUpdateMixedValue() {
    ManageableYieldCurveSnapshot snapshot = createSnapshot();
    snapshot = SetYieldCurveTensorFunction.invoke(snapshot, new Value[] {ValueUtils.of(0.3), ValueUtils.of(0.4), ValueUtils.of(0.5) },
        new Value[] {ValueUtils.of(0.5), ValueUtils.of(0.6), ValueUtils.of(0.7) });
    assertValue(snapshot, "A", 0.3, 0.5);
    assertValue(snapshot, "B", 0.4, 0.6);
    assertValue(snapshot, "C", 0.5, 0.7);
  }

}
