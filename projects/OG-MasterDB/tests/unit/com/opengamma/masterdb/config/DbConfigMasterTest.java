/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * Test DbConfigMaster.
 */
public class DbConfigMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigMasterTest.class);

  private DbConfigMaster _cfgMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbConfigMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext springContext = DbMasterTestUtils.getContext(getDatabaseType());
    _cfgMaster = springContext.getBean(getDatabaseType() + "DbConfigMaster", DbConfigMaster.class);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _cfgMaster = null;
    super.tearDown();
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_cfgMaster);
    assertEquals(true, _cfgMaster.getUniqueIdScheme().equals("DbCfg"));
    assertNotNull(_cfgMaster.getDbConnector());
    assertNotNull(_cfgMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_single_type() throws Exception {
    ConfigDocument<ExternalId> addDoc = new ConfigDocument<ExternalId>(ExternalId.class);
    addDoc.setName("Config test");
    addDoc.setValue(ExternalId.of("A", "B"));
    ConfigDocument<ExternalId> added = _cfgMaster.add(addDoc);
    
    ConfigDocument<ExternalId> loaded = _cfgMaster.get(added.getUniqueId(), ExternalId.class);
    assertEquals(added, loaded);
    
    ConfigDocument<ExternalId> loadedType = _cfgMaster.get(added.getUniqueId(), ExternalId.class);
    assertEquals(added, loadedType);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_multiple_types() throws Exception {
    ConfigDocument<ExternalId> identifierDoc = new ConfigDocument<ExternalId>(ExternalId.class);
    identifierDoc.setName("ExternalId test");
    identifierDoc.setValue(ExternalId.of("A", "B"));
    
    ConfigDocument<ExternalId> addedId = _cfgMaster.add(identifierDoc);
    
    ConfigDocument<ExternalIdBundle> bundleDoc = new ConfigDocument<ExternalIdBundle>(ExternalIdBundle.class);
    bundleDoc.setName("Bundle test");
    bundleDoc.setValue(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    ConfigDocument<ExternalIdBundle> addedBundle = _cfgMaster.add(bundleDoc);
    
    ConfigDocument<ExternalId> loadedId = _cfgMaster.get(addedId.getUniqueId(), ExternalId.class);
    assertEquals(addedId, loadedId);
    
    ConfigDocument<ExternalIdBundle> loadedBundle = _cfgMaster.get(addedBundle.getUniqueId(), ExternalIdBundle.class);
    assertEquals(addedBundle, loadedBundle);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_invalid_type() throws Exception {
    ConfigDocument<ExternalId> identifierDoc = new ConfigDocument<ExternalId>(ExternalId.class);
    identifierDoc.setName("ExternalId test");
    identifierDoc.setValue(ExternalId.of("A", "B"));
    
    _cfgMaster.add(identifierDoc);
    
    ConfigDocument<ExternalIdBundle> bundleDoc = new ConfigDocument<ExternalIdBundle>(ExternalIdBundle.class);
    bundleDoc.setName("Bundle test");
    bundleDoc.setValue(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    ConfigDocument<ExternalIdBundle> addedBundle = _cfgMaster.add(bundleDoc);
    
    _cfgMaster.get(addedBundle.getUniqueId(), ExternalId.class);    
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbConfigMaster[DbCfg]", _cfgMaster.toString());
  }

}
