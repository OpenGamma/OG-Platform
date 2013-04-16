/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SnapshotDataBundleFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class SnapshotDataBundleFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    final SnapshotDataBundle in = new SnapshotDataBundle();
    final SnapshotDataBundle out = cycleObject(SnapshotDataBundle.class, in);
    assertEquals(out.size(), 0);
  }

  public void testBasic() {
    final SnapshotDataBundle in = new SnapshotDataBundle();
    in.setDataPoint(ExternalId.of("Foo", "1"), 1d);
    in.setDataPoint(ExternalIdBundle.of(ExternalId.of("Foo", "2"), ExternalId.of("Bar", "Cow")), 2d);
    final SnapshotDataBundle out = cycleObject(SnapshotDataBundle.class, in);
    assertEquals(out.size(), 2);
    assertEquals(out.getDataPoint(ExternalId.of("Foo", "1")), 1d);
    assertEquals(out.getDataPoint(ExternalId.of("Foo", "2")), 2d);
    assertEquals(out.getDataPoint(ExternalId.of("Bar", "Cow")), 2d);
  }

}
