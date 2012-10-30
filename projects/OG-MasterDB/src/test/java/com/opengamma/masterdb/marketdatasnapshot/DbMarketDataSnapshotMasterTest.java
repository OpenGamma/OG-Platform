package com.opengamma.masterdb.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
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
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

public class DbMarketDataSnapshotMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMasterTest.class);

  private DbMarketDataSnapshotMaster _snpMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbMarketDataSnapshotMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _snpMaster = (DbMarketDataSnapshotMaster) context.getBean(getDatabaseType() + "DbMarketDataSnapshotMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _snpMaster = null;
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_snpMaster);
    assertEquals(true, _snpMaster.getUniqueIdScheme().equals("DbSnp"));
    assertNotNull(_snpMaster.getDbConnector());
    assertNotNull(_snpMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableMarketDataSnapshot marketDataSnapshot = new ManageableMarketDataSnapshot();
    marketDataSnapshot.setName("Test");
    
    HashMap<YieldCurveKey,YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey,YieldCurveSnapshot>();
    
    ManageableUnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    globalValues.setValues(new HashMap<MarketDataValueSpecification, Map<String,ValueSnapshot>>());
    marketDataSnapshot.setGlobalValues(globalValues);
    marketDataSnapshot.setYieldCurves(yieldCurves);
    
    MarketDataSnapshotDocument addDoc = new MarketDataSnapshotDocument(marketDataSnapshot);
    MarketDataSnapshotDocument added = _snpMaster.add(addDoc);
    
    MarketDataSnapshotDocument loaded = _snpMaster.get(added.getUniqueId());
    assertEquivalent(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_complex_example() throws Exception {
    ManageableMarketDataSnapshot snapshot1 = new ManageableMarketDataSnapshot();
    snapshot1.setName("Test");
    ManageableUnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    snapshot1.setGlobalValues(globalValues);
    
    HashMap<MarketDataValueSpecification,Map<String, ValueSnapshot>> values = new HashMap<MarketDataValueSpecification,Map<String, ValueSnapshot>>();
    
    HashMap<YieldCurveKey,YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey,YieldCurveSnapshot>();
    
    MarketDataValueSpecification specA = new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, UniqueId.of("XXX", "AAA"));
    MarketDataValueSpecification specB = new MarketDataValueSpecification(MarketDataValueType.SECURITY, UniqueId.of("XXX", "AAA"));
    
    HashMap<String,ValueSnapshot> hashMapA = new HashMap<String,ValueSnapshot>();
    hashMapA.put("X", new ValueSnapshot(Double.valueOf(12),null));
    hashMapA.put("Y", new ValueSnapshot(Double.valueOf(1),null));
    hashMapA.put("Z", new ValueSnapshot(null,null));
    values.put(specA, hashMapA);
    HashMap<String,ValueSnapshot> hashMapB = new HashMap<String,ValueSnapshot>();
    hashMapB.put("X", new ValueSnapshot(Double.valueOf(12),Double.valueOf(11)));
    values.put(specB, hashMapB);
    
    ManageableYieldCurveSnapshot manageableYieldCurveSnapshot = new ManageableYieldCurveSnapshot();
    manageableYieldCurveSnapshot.setValuationTime(Instant.now());
    manageableYieldCurveSnapshot.setValues(globalValues);
    yieldCurves.put(new YieldCurveKey(Currency.GBP, "Default"), manageableYieldCurveSnapshot);
    
    globalValues.setValues(values);
    snapshot1.setYieldCurves(yieldCurves);
    
    HashMap<Pair<Tenor,Tenor>, ValueSnapshot> strikes = new HashMap<Pair<Tenor,Tenor>, ValueSnapshot>();
    strikes.put(Pair.of(Tenor.DAY, Tenor.WORKING_WEEK), new ValueSnapshot(12.0, 12.0));
    strikes.put(Pair.of(Tenor.DAY, Tenor.WORKING_WEEK), null);
    
    HashMap<VolatilityCubeKey, VolatilityCubeSnapshot> volCubes = new HashMap<VolatilityCubeKey, VolatilityCubeSnapshot>();
    ManageableVolatilityCubeSnapshot volCube = new ManageableVolatilityCubeSnapshot();
    
    volCube.setOtherValues(globalValues);
    volCube.setValues(new HashMap<VolatilityPoint, ValueSnapshot>());
    volCube.setStrikes(strikes);
    volCube.getValues().put(new VolatilityPoint(Tenor.DAY, Tenor.MONTH, -1), new ValueSnapshot(null,null));
    
    volCubes.put(new VolatilityCubeKey(Currency.USD, "Default"), volCube);
    snapshot1.setVolatilityCubes(volCubes);

    MarketDataSnapshotDocument doc1 = new MarketDataSnapshotDocument(snapshot1);
    doc1 = _snpMaster.add(doc1);
    
    ManageableMarketDataSnapshot snapshot2 = new ManageableMarketDataSnapshot();
    snapshot2.setName("SS 2");
    MarketDataSnapshotDocument doc2 = new MarketDataSnapshotDocument(snapshot2);
    doc2 = _snpMaster.add(doc2);
    
    MarketDataSnapshotDocument doc1Loaded = _snpMaster.get(doc1.getUniqueId());
    assertEquivalent(doc1, doc1Loaded);
    
    MarketDataSnapshotDocument doc2Loaded = _snpMaster.get(doc2.getUniqueId());
    assertEquivalent(doc2, doc2Loaded);
    
    // Search by name with data
    MarketDataSnapshotSearchRequest request1 = new MarketDataSnapshotSearchRequest();
    request1.setIncludeData(true);
    request1.setName(doc1.getName());
    MarketDataSnapshotSearchResult result1 = _snpMaster.search(request1);
    assertTrue(result1.getDocuments().size() > 0);

    // Search by name without data
    MarketDataSnapshotSearchRequest request2 = new MarketDataSnapshotSearchRequest();
    request2.setIncludeData(false);
    request2.setName(doc1.getName());
    MarketDataSnapshotSearchResult result2 = _snpMaster.search(request2);
    assertTrue(result2.getDocuments().size() > 0);
    assertEquals(result1.getDocuments().size(), result2.getDocuments().size());
    
    // Search by ID
    MarketDataSnapshotSearchRequest request3 = new MarketDataSnapshotSearchRequest();
    request3.setSnapshotIds(ImmutableSet.of(doc1.getUniqueId().getObjectId()));
    MarketDataSnapshotSearchResult result3 = _snpMaster.search(request3);
    assertEquals(1, result3.getDocuments().size());
    assertEquals(doc1.getUniqueId(), result3.getFirstDocument().getUniqueId());
  }

  private void assertEquivalent(MarketDataSnapshotDocument added, MarketDataSnapshotDocument loaded) {
    ManageableMarketDataSnapshot addedSnapshot = added.getSnapshot();
    ManageableMarketDataSnapshot loadedSnapshot = loaded.getSnapshot();
    assertEquivalent(addedSnapshot, loadedSnapshot);
  }

  private void assertEquivalent(ManageableMarketDataSnapshot addedSnapshot, ManageableMarketDataSnapshot loadedSnapshot) {
    UnstructuredMarketDataSnapshot addedGlobalValues = addedSnapshot.getGlobalValues();
    UnstructuredMarketDataSnapshot loadedGlobalValues = loadedSnapshot.getGlobalValues();
    assertEquivalent(addedGlobalValues, loadedGlobalValues);
    // TODO check yield curves and vol cubes
  }

  private void assertEquivalent(UnstructuredMarketDataSnapshot addedGlobalValues, UnstructuredMarketDataSnapshot loadedGlobalValues) {
    if (addedGlobalValues == null && loadedGlobalValues == null) {
      return;
    }
    if (addedGlobalValues == null && loadedGlobalValues != null) {
      throw new AssertionError(null);
    }
    assertEquivalent(addedGlobalValues.getValues(), loadedGlobalValues.getValues());
  }

  private void assertEquivalent(Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> added,
      Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> loaded) throws AssertionError {
    if (added == null && loaded == null) {
      return;
    }
    if (added == null || loaded == null) {
      throw new AssertionError(null);
    }
    assertEquals(added.keySet(), loaded.keySet());
    
    for (MarketDataValueSpecification spec : added.keySet()) {
        Map<String, ValueSnapshot> aMap = added.get(spec);
        Map<String, ValueSnapshot> loadMap = loaded.get(spec);
        
        assertEquals(aMap.keySet(), loadMap.keySet());
        //TODO
        //TODO assertEquals(addedValue.getMarketValue(), loadedValue.getMarketValue(), 0.0);
        //TODO assertEquals(addedValue.getOverrideValue(), loadedValue.getOverrideValue());
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbMarketDataSnapshotMaster[DbSnp]", _snpMaster.toString());
  }

}
