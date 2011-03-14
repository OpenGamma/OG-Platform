package com.opengamma.masterdb.marketdatasnapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.core.marketdatasnapshot.FXVolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

public class DbMarketDataSnapshotMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMasterTest.class);

  private DbMarketDataSnapshotMaster _snpMaster;

  public DbMarketDataSnapshotMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _snpMaster = (DbMarketDataSnapshotMaster) context.getBean(getDatabaseType() + "DbMarketDataSnapshotMaster");
  }

  @After
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
    
    HashMap<UniqueIdentifier,ValueSnapshot> values = new HashMap<UniqueIdentifier,ValueSnapshot>();
    HashMap<Triple<String, Currency,Currency>,FXVolatilitySurfaceSnapshot> fxVolSurfaces = new HashMap<Triple<String, Currency,Currency>,FXVolatilitySurfaceSnapshot>();
    HashMap<Pair<String,Currency>,YieldCurveSnapshot> yieldCurves = new HashMap<Pair<String,Currency>,YieldCurveSnapshot>();
    
    marketDataSnapshot.setValues(values);
    marketDataSnapshot.setFxVolatilitySurfaces(fxVolSurfaces);
    marketDataSnapshot.setYieldCurves(yieldCurves);
        
    MarketDataSnapshotDocument addDoc = new MarketDataSnapshotDocument(marketDataSnapshot);
    MarketDataSnapshotDocument added = _snpMaster.add(addDoc);
    
    MarketDataSnapshotDocument loaded = _snpMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_complex_example() throws Exception {
    ManageableMarketDataSnapshot marketDataSnapshot = new ManageableMarketDataSnapshot();
    marketDataSnapshot.setName("Test");
    
    HashMap<UniqueIdentifier,ValueSnapshot> values = new HashMap<UniqueIdentifier,ValueSnapshot>();
    HashMap<Triple<String, Currency,Currency>,FXVolatilitySurfaceSnapshot> fxVolSurfaces = new HashMap<Triple<String, Currency,Currency>,FXVolatilitySurfaceSnapshot>();
    HashMap<Pair<String,Currency>,YieldCurveSnapshot> yieldCurves = new HashMap<Pair<String,Currency>,YieldCurveSnapshot>();
    
    
    UniqueIdentifier identA = UniqueIdentifier.parse("XXX::AAA");
    UniqueIdentifier identB = UniqueIdentifier.parse("XXX::BBB");
    
    values.put(identA, new ValueSnapshot(12,null,identA));
    values.put(identB, new ValueSnapshot(12,Double.valueOf(11),identB));
    
    //TODO vol surface and yield curve
    
    marketDataSnapshot.setValues(values);
    marketDataSnapshot.setFxVolatilitySurfaces(fxVolSurfaces);
    marketDataSnapshot.setYieldCurves(yieldCurves);
    
    MarketDataSnapshotDocument addDoc = new MarketDataSnapshotDocument(marketDataSnapshot);
    MarketDataSnapshotDocument added = _snpMaster.add(addDoc);
    
    MarketDataSnapshotDocument loaded = _snpMaster.get(added.getUniqueId());
    
    assertEquals(added.getSnapshot().getValues().keySet(), loaded.getSnapshot().getValues().keySet());
    for (UniqueIdentifier id : added.getSnapshot().getValues().keySet()) {
        ValueSnapshot addedValue = added.getSnapshot().getValues().get(id);
        ValueSnapshot loadedValue = loaded.getSnapshot().getValues().get(id);
        
        assertEquals(addedValue.getMarketValue(), loadedValue.getMarketValue(), 0.0);
        assertEquals(addedValue.getOverrideValue(), loadedValue.getOverrideValue());
        assertEquals(addedValue.getSecurity(), loadedValue.getSecurity());
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbMarketDataSnapshotMaster[DbSnp]", _snpMaster.toString());
  }

}
