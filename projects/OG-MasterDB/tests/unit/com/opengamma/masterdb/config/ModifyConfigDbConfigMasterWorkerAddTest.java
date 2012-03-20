/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
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
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    _cfgMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("TestConfig");
    doc.setValue(ExternalId.of("A", "B"));
    ConfigDocument<ExternalId> test = _cfgMaster.add(doc);
    
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
    assertEquals(ExternalId.of("A", "B"), test.getValue());
    assertEquals("TestConfig", test.getName());
  }
  
  @Test
  public void test_add_addThenGet() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("TestConfig");
    doc.setValue(ExternalId.of("A", "B"));
    ConfigDocument<ExternalId> added = _cfgMaster.add(doc);
    
    ConfigDocument<ExternalId> test = _cfgMaster.get(added.getUniqueId(), ExternalId.class);
    assertEquals(added, test);
  }

}
