package com.opengamma.masterdb.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.ManageableYieldCurveSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

public class DbMarketDataSnapshotMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMasterTest.class);

  private DbMarketDataSnapshotMaster _snpMaster;

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public DbMarketDataSnapshotMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
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

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_snpMaster);
    assertEquals(true, _snpMaster.getIdentifierScheme().equals("DbSnp"));
    assertNotNull(_snpMaster.getDbSource());
    assertNotNull(_snpMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableMarketDataSnapshot marketDataSnapshot = new ManageableMarketDataSnapshot();
    marketDataSnapshot.setName("Test");
    
    HashMap<YieldCurveKey,YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey,YieldCurveSnapshot>();
    
    UnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    
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
    ManageableMarketDataSnapshot marketDataSnapshot = new ManageableMarketDataSnapshot();
    marketDataSnapshot.setName("Test");
    ManageableUnstructuredMarketDataSnapshot globalValues = new ManageableUnstructuredMarketDataSnapshot();
    marketDataSnapshot.setGlobalValues(globalValues);
    
    HashMap<MarketDataValueSpecification,ValueSnapshot> values = new HashMap<MarketDataValueSpecification,ValueSnapshot>();
    
    HashMap<YieldCurveKey,YieldCurveSnapshot> yieldCurves = new HashMap<YieldCurveKey,YieldCurveSnapshot>();
    
    
    MarketDataValueSpecification specA = new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, UniqueIdentifier.parse("XXX::AAA"));
    MarketDataValueSpecification specB = new MarketDataValueSpecification(MarketDataValueType.SECURITY, UniqueIdentifier.parse("XXX::AAA"));
    
    values.put(specA, new ValueSnapshot(12,null));
    values.put(specB, new ValueSnapshot(12,Double.valueOf(11)));
    
    
    ManageableYieldCurveSnapshot manageableYieldCurveSnapshot = new ManageableYieldCurveSnapshot();
    manageableYieldCurveSnapshot.setValuationTime(Instant.now());
    manageableYieldCurveSnapshot.setValues(globalValues);
    yieldCurves.put(new YieldCurveKey(Currency.GBP, "Default"), manageableYieldCurveSnapshot);
    
    globalValues.setValues(values);
    marketDataSnapshot.setYieldCurves(yieldCurves);
    
    MarketDataSnapshotDocument addDoc = new MarketDataSnapshotDocument(marketDataSnapshot);
    MarketDataSnapshotDocument added = _snpMaster.add(addDoc);
    
    MarketDataSnapshotDocument loaded = _snpMaster.get(added.getUniqueId());
    
    assertEquivalent(added, loaded);
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
  }

  private void assertEquivalent(UnstructuredMarketDataSnapshot addedGlobalValues,       UnstructuredMarketDataSnapshot loadedGlobalValues) {
    
    if (addedGlobalValues == null && loadedGlobalValues == null)
    {
      return;
    }
    if (addedGlobalValues == null && loadedGlobalValues != null)
    {
      throw new AssertionError(null);
    }
    
    assertEquivalent(addedGlobalValues.getValues(), loadedGlobalValues.getValues());
  }

  private void assertEquivalent(Map<MarketDataValueSpecification, ValueSnapshot> added,
      Map<MarketDataValueSpecification, ValueSnapshot> loaded) throws AssertionError {
    if (added == null && loaded == null) {
      return;
    }
    if (added == null || loaded == null) {
      throw new AssertionError(null);
    }
    assertEquals(added.keySet(), loaded.keySet());
    
    for (MarketDataValueSpecification spec : added.keySet()) {
        ValueSnapshot addedValue = added.get(spec);
        ValueSnapshot loadedValue = loaded.get(spec);
        
        assertEquals(addedValue.getMarketValue(), loadedValue.getMarketValue(), 0.0);
        assertEquals(addedValue.getOverrideValue(), loadedValue.getOverrideValue());
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbMarketDataSnapshotMaster[DbSnp]", _snpMaster.toString());
  }

}
