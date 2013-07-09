package com.opengamma.masterdb.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbMarketDataSnapshotMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMasterTest.class);

  private DbMarketDataSnapshotMaster _snpMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbMarketDataSnapshotMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _snpMaster = new DbMarketDataSnapshotMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _snpMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_snpMaster);
    assertEquals(true, _snpMaster.getUniqueIdScheme().equals("DbSnp"));
    assertNotNull(_snpMaster.getDbConnector());
    assertNotNull(_snpMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    final ManageableMarketDataSnapshot marketDataSnapshot = new ManageableMarketDataSnapshot();
    marketDataSnapshot.setName("Test");

    final HashMap<YieldCurveKey, YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey, YieldCurveSnapshot>();

    final ManageableUnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    marketDataSnapshot.setGlobalValues(globalValues);
    marketDataSnapshot.setYieldCurves(yieldCurves);

    final MarketDataSnapshotDocument addDoc = new MarketDataSnapshotDocument(marketDataSnapshot);
    final MarketDataSnapshotDocument added = _snpMaster.add(addDoc);

    final MarketDataSnapshotDocument loaded = _snpMaster.get(added.getUniqueId());
    assertEquivalent(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_complex_example() throws Exception {
    final ManageableMarketDataSnapshot snapshot1 = new ManageableMarketDataSnapshot();
    snapshot1.setName("Test");
    final ManageableUnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    snapshot1.setGlobalValues(globalValues);

    final HashMap<YieldCurveKey, YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey, YieldCurveSnapshot>();

    final ExternalIdBundle specA = ExternalId.of("XXX", "AAA").toBundle();
    final ExternalIdBundle specB = ExternalIdBundle.of(ExternalId.of("XXX", "B1"), ExternalId.of("XXX", "B2"));

    globalValues.putValue(specA, "X", new ValueSnapshot(Double.valueOf(12), null));
    globalValues.putValue(specA, "Y", new ValueSnapshot(Double.valueOf(1), null));
    globalValues.putValue(specA, "Z", new ValueSnapshot(null, null));
    globalValues.putValue(specB, "X", new ValueSnapshot(Double.valueOf(12), Double.valueOf(11)));

    final ManageableYieldCurveSnapshot manageableYieldCurveSnapshot = new ManageableYieldCurveSnapshot();
    manageableYieldCurveSnapshot.setValuationTime(Instant.now());
    manageableYieldCurveSnapshot.setValues(globalValues);
    yieldCurves.put(new YieldCurveKey(Currency.GBP, "Default"), manageableYieldCurveSnapshot);

    snapshot1.setYieldCurves(yieldCurves);

    final HashMap<Pair<Tenor, Tenor>, ValueSnapshot> strikes = new HashMap<Pair<Tenor, Tenor>, ValueSnapshot>();
    strikes.put(Pair.of(Tenor.DAY, Tenor.WORKING_WEEK), new ValueSnapshot(12.0, 12.0));
    strikes.put(Pair.of(Tenor.DAY, Tenor.WORKING_WEEK), null);

    final HashMap<VolatilityCubeKey, VolatilityCubeSnapshot> volCubes = new HashMap<VolatilityCubeKey, VolatilityCubeSnapshot>();
    final ManageableVolatilityCubeSnapshot volCube = new ManageableVolatilityCubeSnapshot();

    volCube.setOtherValues(globalValues);
    volCube.setValues(new HashMap<VolatilityPoint, ValueSnapshot>());
    volCube.setStrikes(strikes);
    volCube.getValues().put(new VolatilityPoint(Tenor.DAY, Tenor.YEAR, -1), new ValueSnapshot(null, null));

    volCubes.put(new VolatilityCubeKey(Currency.USD, "Default"), volCube);
    snapshot1.setVolatilityCubes(volCubes);

    MarketDataSnapshotDocument doc1 = new MarketDataSnapshotDocument(snapshot1);
    doc1 = _snpMaster.add(doc1);

    final ManageableMarketDataSnapshot snapshot2 = new ManageableMarketDataSnapshot();
    snapshot2.setName("SS 2");
    MarketDataSnapshotDocument doc2 = new MarketDataSnapshotDocument(snapshot2);
    doc2 = _snpMaster.add(doc2);

    final MarketDataSnapshotDocument doc1Loaded = _snpMaster.get(doc1.getUniqueId());
    assertEquivalent(doc1, doc1Loaded);

    final MarketDataSnapshotDocument doc2Loaded = _snpMaster.get(doc2.getUniqueId());
    assertEquivalent(doc2, doc2Loaded);

    // Search by name with data
    final MarketDataSnapshotSearchRequest request1 = new MarketDataSnapshotSearchRequest();
    request1.setIncludeData(true);
    request1.setName(doc1.getName());
    final MarketDataSnapshotSearchResult result1 = _snpMaster.search(request1);
    assertTrue(result1.getDocuments().size() > 0);

    // Search by name without data
    final MarketDataSnapshotSearchRequest request2 = new MarketDataSnapshotSearchRequest();
    request2.setIncludeData(false);
    request2.setName(doc1.getName());
    final MarketDataSnapshotSearchResult result2 = _snpMaster.search(request2);
    assertTrue(result2.getDocuments().size() > 0);
    assertEquals(result1.getDocuments().size(), result2.getDocuments().size());

    // Search by ID
    final MarketDataSnapshotSearchRequest request3 = new MarketDataSnapshotSearchRequest();
    request3.setSnapshotIds(ImmutableSet.of(doc1.getUniqueId().getObjectId()));
    final MarketDataSnapshotSearchResult result3 = _snpMaster.search(request3);
    assertEquals(1, result3.getDocuments().size());
    assertEquals(doc1.getUniqueId(), result3.getFirstDocument().getUniqueId());
  }

  private void assertEquivalent(final MarketDataSnapshotDocument added, final MarketDataSnapshotDocument loaded) {
    final ManageableMarketDataSnapshot addedSnapshot = added.getSnapshot();
    final ManageableMarketDataSnapshot loadedSnapshot = loaded.getSnapshot();
    assertEquivalent(addedSnapshot, loadedSnapshot);
  }

  private void assertEquivalent(final ManageableMarketDataSnapshot addedSnapshot, final ManageableMarketDataSnapshot loadedSnapshot) {
    final UnstructuredMarketDataSnapshot addedGlobalValues = addedSnapshot.getGlobalValues();
    final UnstructuredMarketDataSnapshot loadedGlobalValues = loadedSnapshot.getGlobalValues();
    assertEquivalent(addedGlobalValues, loadedGlobalValues);
    // TODO check yield curves and vol cubes
  }

  private void assertEquivalent(final UnstructuredMarketDataSnapshot addedGlobalValues, final UnstructuredMarketDataSnapshot loadedGlobalValues) {
    if (addedGlobalValues == null && loadedGlobalValues == null) {
      return;
    }
    if (addedGlobalValues == null && loadedGlobalValues != null) {
      throw new AssertionError(null);
    }
    assertEquals(addedGlobalValues.getTargets(), loadedGlobalValues.getTargets());
    for (final ExternalIdBundle target : addedGlobalValues.getTargets()) {
      assertEquals(addedGlobalValues.getTargetValues(target), loadedGlobalValues.getTargetValues(target));
    }
  }

  @Test
  public void test_toString() {
    assertEquals("DbMarketDataSnapshotMaster[DbSnp]", _snpMaster.toString());
  }

}
