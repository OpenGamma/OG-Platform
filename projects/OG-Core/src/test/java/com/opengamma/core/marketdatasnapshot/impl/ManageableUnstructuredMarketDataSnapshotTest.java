/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ManageableUnstructuredMarketDataSnapshot} class.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableUnstructuredMarketDataSnapshotTest {

  private final ExternalId _eid1 = ExternalId.of("Foo", "1");
  private final ExternalId _eid2 = ExternalId.of("Foo", "2");
  private final ExternalId _eid3 = ExternalId.of("Foo", "3");

  public void testPutGetRemove_externalId() {

    final ManageableUnstructuredMarketDataSnapshot object = new ManageableUnstructuredMarketDataSnapshot();
    assertTrue(object.isEmpty());
    object.putValue(_eid1, "V1", ValueSnapshot.of(11d));
    object.putValue(_eid1, "V2", ValueSnapshot.of(12d));
    object.putValue(_eid2, "V1", ValueSnapshot.of(21d));
    object.putValue(_eid2, "V2", ValueSnapshot.of(22d));
    assertFalse(object.isEmpty());
    assertEquals(object.getTargets(), ImmutableSet.of(_eid1.toBundle(), _eid2.toBundle()));
    assertEquals(object.getValue(_eid1, "V1"), ValueSnapshot.of(11d));
    assertEquals(object.getValue(_eid1, "V2"), ValueSnapshot.of(12d));
    assertNull(object.getValue(_eid1, "V3"));
    assertEquals(object.getValue(_eid2, "V1"), ValueSnapshot.of(21d));
    assertEquals(object.getValue(_eid2, "V2"), ValueSnapshot.of(22d));
    assertNull(object.getValue(_eid2, "V3"));
    assertNull(object.getValue(_eid3, "V1"));
    assertNull(object.getValue(_eid3, "V2"));
    assertEquals(object.getValue(ExternalIdBundle.of(_eid1, _eid3), "V1"), ValueSnapshot.of(11d));
    final ManageableUnstructuredMarketDataSnapshot cloned = new ManageableUnstructuredMarketDataSnapshot(object);
    object.removeValue(_eid1, "V1");
    object.removeValue(_eid2, "V1");
    assertEquals(object.getTargets(), ImmutableSet.of(_eid1.toBundle(), _eid2.toBundle()));
    assertNull(object.getValue(_eid1, "V1"));
    assertEquals(object.getValue(_eid1, "V2"), ValueSnapshot.of(12d));
    assertNull(object.getValue(_eid2, "V1"));
    assertEquals(object.getValue(_eid2, "V2"), ValueSnapshot.of(22d));
    object.removeValue(_eid1, "V2");
    assertEquals(object.getTargets(), ImmutableSet.of(_eid2.toBundle()));
    object.removeValue(_eid2, "V2");
    assertEquals(object.getTargets(), Collections.emptySet());
    assertTrue(object.isEmpty());
    assertEquals(cloned.getValue(_eid1, "V1"), ValueSnapshot.of(11d));
    assertEquals(cloned.getValue(_eid1, "V2"), ValueSnapshot.of(12d));
    assertEquals(cloned.getValue(_eid2, "V1"), ValueSnapshot.of(21d));
    assertEquals(cloned.getValue(_eid2, "V2"), ValueSnapshot.of(22d));
  }

  public void testPutGetRemove_externalIdBundle() {
    final ManageableUnstructuredMarketDataSnapshot object = new ManageableUnstructuredMarketDataSnapshot();
    assertTrue(object.isEmpty());
    object.putValue(ExternalIdBundle.of(_eid1, _eid2), "V1", ValueSnapshot.of(1d));
    object.putValue(ExternalIdBundle.of(_eid2, _eid3), "V2", ValueSnapshot.of(2d));
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(_eid1, _eid2), ExternalIdBundle.of(_eid2, _eid3)));
    assertFalse(object.isEmpty());
    assertEquals(object.getValue(ExternalIdBundle.of(_eid1, _eid2), "V1"), ValueSnapshot.of(1d));
    assertEquals(object.getValue(ExternalIdBundle.of(_eid1, _eid2), "V2"), ValueSnapshot.of(2d));
    assertEquals(object.getValue(_eid1, "V1"), ValueSnapshot.of(1d));
    assertNull(object.getValue(_eid1, "V2"));
    assertEquals(object.getValue(_eid2, "V1"), ValueSnapshot.of(1d));
    assertEquals(object.getValue(_eid2, "V2"), ValueSnapshot.of(2d));
    object.putValue(ExternalIdBundle.of(_eid2, _eid3), "V1", ValueSnapshot.of(3d));
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(_eid2, _eid3)));
    assertNull(object.getValue(_eid1, "V1"));
    assertNull(object.getValue(_eid1, "V2"));
    assertEquals(object.getValue(_eid2, "V1"), ValueSnapshot.of(3d));
    assertEquals(object.getValue(_eid2, "V2"), ValueSnapshot.of(2d));
    assertEquals(object.getValue(_eid3, "V1"), ValueSnapshot.of(3d));
    assertEquals(object.getValue(_eid3, "V2"), ValueSnapshot.of(2d));
    final ManageableUnstructuredMarketDataSnapshot cloned = new ManageableUnstructuredMarketDataSnapshot(object);
    object.removeValue(ExternalIdBundle.of(_eid2, _eid3), "V1");
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(_eid2, _eid3)));
    assertNull(object.getValue(_eid2, "V1"));
    assertEquals(object.getValue(_eid2, "V2"), ValueSnapshot.of(2d));
    assertNull(object.getValue(_eid3, "V1"));
    assertEquals(object.getValue(_eid3, "V2"), ValueSnapshot.of(2d));
    object.removeValue(ExternalIdBundle.of(_eid1, _eid2), "V2");
    assertEquals(object.getTargets(), Collections.emptySet());
    assertTrue(object.isEmpty());
    assertEquals(cloned.getValue(_eid2, "V1"), ValueSnapshot.of(3d));
    assertEquals(cloned.getValue(_eid2, "V2"), ValueSnapshot.of(2d));
    assertEquals(cloned.getValue(_eid3, "V1"), ValueSnapshot.of(3d));
  }

}
