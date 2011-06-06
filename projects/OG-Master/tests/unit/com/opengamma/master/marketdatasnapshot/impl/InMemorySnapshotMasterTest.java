/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.ObjectIdentifierSupplier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * Test InMemorySnapshotMaster.
 */
@Test
public class InMemorySnapshotMasterTest {

  // TODO Move the logical tests from here to the generic SnapshotMasterTestCase then we can just extend from that

  private static final UniqueIdentifier OTHER_UID = UniqueIdentifier.of("U", "1");
  private static final ManageableMarketDataSnapshot SNAP1 = new ManageableMarketDataSnapshot("Test 1", new ManageableUnstructuredMarketDataSnapshot(),new HashMap<YieldCurveKey, YieldCurveSnapshot>(12));
  private static final ManageableMarketDataSnapshot SNAP2 = new ManageableMarketDataSnapshot("Test 2", new ManageableUnstructuredMarketDataSnapshot(),new HashMap<YieldCurveKey, YieldCurveSnapshot>(12));
  

  private InMemorySnapshotMaster testEmpty;
  private InMemorySnapshotMaster testPopulated;
  private MarketDataSnapshotDocument doc1;
  private MarketDataSnapshotDocument doc2;

  @BeforeMethod
  public void setUp() {
    testEmpty = new InMemorySnapshotMaster(new ObjectIdentifierSupplier("Test"));
    testPopulated = new InMemorySnapshotMaster(new ObjectIdentifierSupplier("Test"));
    doc1 = new MarketDataSnapshotDocument();
    doc1.setSnapshot(SNAP1);
    doc1 = testPopulated.add(doc1);
    doc2 = new MarketDataSnapshotDocument();
    doc2.setSnapshot(SNAP2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemorySnapshotMaster((Supplier<ObjectIdentifier>) null);
  }

  public void test_defaultSupplier() {
    InMemorySnapshotMaster master = new InMemorySnapshotMaster();
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    MarketDataSnapshotDocument added = master.add(doc);
    assertEquals("MemSnap", added.getUniqueId().getScheme());
  }

  public void test_alternateSupplier() {
    InMemorySnapshotMaster master = new InMemorySnapshotMaster(new ObjectIdentifierSupplier("Hello"));
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    MarketDataSnapshotDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    MarketDataSnapshotSearchResult result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_populatedMaster_all() {
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    MarketDataSnapshotSearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByName() {
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName("*est 2");
    MarketDataSnapshotSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_history_emptyMaster() {
    MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest();
    request.setObjectId(doc1.getUniqueId().getObjectId());
    MarketDataSnapshotHistoryResult result = testEmpty.history(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  public void test_history_populatedMaster() {
    MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest();
    request.setObjectId(doc1.getUniqueId().getObjectId());
    MarketDataSnapshotHistoryResult result = testPopulated.history(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  public void test_get_populatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueId()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueId()));
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    MarketDataSnapshotDocument added = testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(SNAP1, added.getSnapshot());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    doc.setUniqueId(OTHER_UID);
    testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
    doc.setSnapshot(SNAP1);
    doc.setUniqueId(doc1.getUniqueId());
    MarketDataSnapshotDocument updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    MarketDataSnapshotSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<MarketDataSnapshotDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

}
