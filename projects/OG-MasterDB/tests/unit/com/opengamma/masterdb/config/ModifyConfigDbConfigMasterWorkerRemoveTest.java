/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerRemoveTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyConfigDbConfigMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeConfig_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    _cfgMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    _cfgMaster.remove(uniqueId);
    ConfigDocument<Identifier> test = _cfgMaster.get(uniqueId, Identifier.class);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1aInstant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1aInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "B"), test.getValue());
    assertEquals("TestConfig101", test.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
