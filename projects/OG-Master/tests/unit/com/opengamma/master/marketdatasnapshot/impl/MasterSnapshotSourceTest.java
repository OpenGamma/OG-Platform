/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * Test {@link MasterSnapshotSource}.
 */
@Test
public class MasterSnapshotSourceTest {

  private static final UniqueId UID = UniqueId.of("A", "B");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterSnapshotSource(null);
  }

  //-------------------------------------------------------------------------
  public void test_getSnapshot_noOverride_found() throws Exception {
    MarketDataSnapshotMaster mock = mock(MarketDataSnapshotMaster.class);

    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterSnapshotSource test = new MasterSnapshotSource(mock);
    StructuredMarketDataSnapshot testResult = test.getSnapshot(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class, expectedExceptionsMessageRegExp = "Some message")
  public void test_getSnapshot_noOverride_notFound() throws Exception {
    MarketDataSnapshotMaster mock = mock(MarketDataSnapshotMaster.class);
    
    new MarketDataSnapshotDocument(example());
    when(mock.get(UID)).thenThrow(new DataNotFoundException("Some message"));
    MasterSnapshotSource test = new MasterSnapshotSource(mock);
    test.getSnapshot(UID);
  }

  //-------------------------------------------------------------------------
  protected ManageableMarketDataSnapshot example() {
    ManageableMarketDataSnapshot snapshotDocument = new ManageableMarketDataSnapshot();
    snapshotDocument.setUniqueId(UID);
    return snapshotDocument;
  }

}
