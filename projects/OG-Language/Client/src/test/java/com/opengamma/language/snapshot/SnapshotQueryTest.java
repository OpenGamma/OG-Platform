/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.snapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.test.TestUtils;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.InMemorySnapshotMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the snapshot querying functions
 */
@Test(groups = TestGroup.UNIT)
public class SnapshotQueryTest {

  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotQueryTest.class);

  private static class TestRemoteClient extends RemoteClient {

    private final MarketDataSnapshotMaster _marketDataSnapshotMaster;

    public TestRemoteClient(final MarketDataSnapshotMaster marketDataSnapshotMaster) {
      super(null, null, null);
      _marketDataSnapshotMaster = marketDataSnapshotMaster;
    }

    @Override
    public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

  }

  private static class TestSnapshotMaster extends InMemorySnapshotMaster {

    private final String _scheme;

    public TestSnapshotMaster(final String scheme) {
      super(new ObjectIdSupplier(scheme));
      _scheme = scheme;
    }

    @Override
    protected void validateScheme(final String scheme) {
      if (!_scheme.equals(scheme)) {
        throw new IllegalArgumentException("Scheme " + scheme + " not from this master");
      }
    }

  }

  private static TestUtils testUtils() {
    final TestUtils testUtils = new TestUtils();
    InMemorySnapshotMaster master = new TestSnapshotMaster("USER");
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("U", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("US", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("UG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("USG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("Test 1", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("Test 1", null, null)));
    testUtils.setSessionClient(new TestRemoteClient(master));
    master = new TestSnapshotMaster("SESSION");
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("S", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("US", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("SG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("USG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("Test 2", null, null)));
    testUtils.setUserClient(new TestRemoteClient(master));
    master = new TestSnapshotMaster("GLOBAL");
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("G", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("UG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("SG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("USG", null, null)));
    master.add(new MarketDataSnapshotDocument(new ManageableMarketDataSnapshot("Test 3", null, null)));
    testUtils.setGlobalClient(new TestRemoteClient(master));
    return testUtils;
  }

  public void testGetSnapshotsFull() {
    final TestUtils testUtils = testUtils();
    final Map<UniqueId, String> result = SnapshotsFunction.invoke(testUtils.createSessionContext(), (String) null);
    assertNotNull(result);
    assertEquals(result.size(), 16);
    s_logger.info("testGetSnapshotsFull");
    for (Map.Entry<UniqueId, String> entry : result.entrySet()) {
      s_logger.debug("{} = {}", entry.getKey(), entry.getValue());
    }
  }

  public void testGetSnapshotsFiltered() {
    final TestUtils testUtils = testUtils();
    final Map<UniqueId, String> result = SnapshotsFunction.invoke(testUtils.createSessionContext(), "Test *");
    assertNotNull(result);
    assertEquals(result.size(), 4);
    s_logger.info("testGetSnapshotsFiltered");
    for (Map.Entry<UniqueId, String> entry : result.entrySet()) {
      s_logger.debug("{} = {}", entry.getKey(), entry.getValue());
    }
  }

  public void testGetSnapshotVersions() {
    final TestUtils testUtils = testUtils();
    final SessionContext context = testUtils.createSessionContext();
    final Map<UniqueId, String> result = SnapshotsFunction.invoke(context, "G");
    assertNotNull(result);
    assertEquals(result.size(), 1);
    final UniqueId id = result.keySet().iterator().next();
    s_logger.debug("testGetSnapshotVersions {}", id);
    final Object[][] data = SnapshotVersionsFunction.invoke(context, id, null);
    assertNotNull(data);
    assertEquals(data.length, 1);
    assertEquals(data[0].length, 4);
    s_logger.info("testGetSnapshotVersions");
    s_logger.debug("UID = {}, Time = {}, Name = {}, Basis View = {}", data[0]);
  }

}
