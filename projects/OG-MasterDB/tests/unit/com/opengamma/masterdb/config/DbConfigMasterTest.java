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

import com.opengamma.core.config.impl.ConfigItem;
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
    super(databaseType, databaseVersion, databaseVersion);
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
    ConfigItem<ExternalId> item = ConfigItem.of(ExternalId.of("A", "B"));
    item.setName("Config test");

    ConfigDocument added = _cfgMaster.add(new ConfigDocument(item));
    
    ConfigDocument loaded = _cfgMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
    
    ConfigDocument loadedType = _cfgMaster.get(added.getUniqueId());
    assertEquals(added, loadedType);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_multiple_types() throws Exception {
    ConfigItem<ExternalId> identifierDoc = ConfigItem.of(ExternalId.of("A", "B"));
    identifierDoc.setName("ExternalId test");
    
    ConfigDocument addedId = _cfgMaster.add(new ConfigDocument(identifierDoc));
    
    ConfigItem<ExternalIdBundle> bundleDoc = ConfigItem.of(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    bundleDoc.setName("Bundle test");
    ConfigDocument addedBundle = _cfgMaster.add(new ConfigDocument(bundleDoc));
    
    ConfigDocument loadedId = _cfgMaster.get(addedId.getUniqueId());
    assertEquals(addedId, loadedId);
    
    ConfigDocument loadedBundle = _cfgMaster.get(addedBundle.getUniqueId());
    assertEquals(addedBundle, loadedBundle);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbConfigMaster[DbCfg]", _cfgMaster.toString());
  }

}
