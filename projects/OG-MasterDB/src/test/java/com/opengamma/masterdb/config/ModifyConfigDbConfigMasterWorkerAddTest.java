/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyConfigDbConfigMasterWorkerAddTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addConfig_nullDocument() {
    _cfgMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noConfig() {
    ConfigItem<ExternalId> item = ConfigItem.of(null);
    _cfgMaster.add(new ConfigDocument(item));
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_cfgMaster.getClock());
    
    ConfigItem<ExternalId> item = ConfigItem.of(ExternalId.of("A", "B"));
    item.setName("TestConfig");
    ConfigDocument test = _cfgMaster.add(new ConfigDocument(item));
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbCfg", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
    assertEquals("TestConfig", test.getName());
  }
  
  @Test
  public void test_add_addThenGet() {
    ConfigItem<ExternalId> item = ConfigItem.of(ExternalId.of("A", "B"));
    item.setName("TestConfig");
    ConfigDocument added = _cfgMaster.add(new ConfigDocument(item));
    
    ConfigDocument test = _cfgMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

}
