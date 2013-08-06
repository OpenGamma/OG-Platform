/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SnapshotDataBundle} class.
 */
@Test(groups = TestGroup.UNIT)
public class SnapshotDataBundleTest {

  private SnapshotDataBundle createObject() {
    final SnapshotDataBundle object = new SnapshotDataBundle();
    object.setDataPoint(ExternalId.of("Foo", "1"), 1d);
    object.setDataPoint(ExternalId.of("Foo", "2"), 2d);
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")), 3d);
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "4"), ExternalId.of("Bar", "Dog")), 4d);
    assertEquals(object.size(), 4);
    return object;
  }

  public void testGetBundle_exactMatch() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "1"))), 1d);
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow"))), 3d);
  }

  public void testGetBundle_partialMatch() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "2"), ExternalId.of("Missing", "1"))), 2d);
  }

  public void testGetBundle_noMatch() {
    final SnapshotDataBundle object = createObject();
    assertNull(object.getDataPoint(ExternalIdBundle.of(ExternalId.of("Missing", "2"), ExternalId.of("Missing", "1"))));
  }

  public void testGetSingle_match() {
    final SnapshotDataBundle object = createObject();
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "1")), 1d);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "4")), 4d);
  }

  public void testGetSingle_noMatch() {
    final SnapshotDataBundle object = createObject();
    assertNull(object.getDataPoint(ExternalId.of("Missing", "1")));
  }

  public void testSetBundle_erasing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "2"), ExternalId.of("Bar", "Cow")), 42d);
    assertEquals(object.size(), 3);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "1")), 1d);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "2")), 42d);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "4")), 4d);
  }

  public void testSetBundle_replacing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")), 42d);
    assertEquals(object.size(), 4);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "3")), 42d);
  }

  public void testSetSingle_replacing() {
    final SnapshotDataBundle object = createObject();
    object.setDataPoint(ExternalId.of("Foo", "3"), 42d);
    assertEquals(object.size(), 4);
    assertEquals(object.getDataPoint(ExternalId.of("Foo", "3")), 42d);
    assertEquals(object.getDataPoint(ExternalId.of("Bar", "Cow")), 42d);
  }

  public void testRemoveBundle_exact() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Cow")));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  public void testRemoveBundle_partial() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Missing", "1")));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  public void testRemoveBundle_multiple() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoints(ExternalIdBundle.of(ExternalId.of("Foo", "3"), ExternalId.of("Bar", "Dog")));
    assertEquals(object.size(), 2);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
    assertNull(object.getDataPoint(ExternalId.of("Foo", "4")));
  }

  public void testRemoveSingle_direct() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoint(ExternalId.of("Foo", "2"));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Foo", "2")));
  }

  public void testRemoveSingle_cascade() {
    final SnapshotDataBundle object = createObject();
    object.removeDataPoint(ExternalId.of("Bar", "Cow"));
    assertEquals(object.size(), 3);
    assertNull(object.getDataPoint(ExternalId.of("Bar", "Cow")));
    assertNull(object.getDataPoint(ExternalId.of("Foo", "3")));
  }

  public void testGetDataPointSet() {
    final SnapshotDataBundle object = new SnapshotDataBundle();
    assertTrue(object.getDataPointSet().isEmpty());
    object.setDataPoint(ExternalId.of("Foo", "Bar"), 42d);
    assertEquals(object.getDataPointSet().size(), 1);
    final Map.Entry<ExternalIdBundle, Double> e = object.getDataPointSet().iterator().next();
    assertEquals(e.getKey(), ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
    assertEquals(e.getValue(), 42d);
  }

  public void testEquals() {
    SnapshotDataBundle snap = new SnapshotDataBundle();
    snap.setDataPoint(ExternalId.parse("Snap~Test"), 1234.56);
    SnapshotDataBundle snap2 = new SnapshotDataBundle();
    snap2.setDataPoint(ExternalId.parse("Snap~Test"), 1234.56);
    SnapshotDataBundle snap3 = new SnapshotDataBundle();
    snap3.setDataPoint(ExternalId.parse("Snap~Test"), 1234);
    assertEquals(snap, snap2);
    assertEquals(snap.hashCode(), snap2.hashCode());
    assertNotEquals(snap, snap3);
    assertNotEquals(snap.hashCode(), snap3.hashCode());
  }

}
