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
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryConfigDbConfigMasterWorkerGetTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryConfigDbConfigMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getConfig_nullUID() {
    _cfgMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConfig_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    _cfgMaster.get(uniqueId);
  }

  @Test
  public void test_getConfig_versioned_oneConfigKey() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument test = _cfgMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getConfig_versioned_twoConfigKeys() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "102", "0");
    ConfigDocument test = _cfgMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getConfig_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "201", "0");
    ConfigDocument test = _cfgMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getConfig_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "201", "1");
    ConfigDocument test = _cfgMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConfig_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0");
    _cfgMaster.get(uniqueId);
  }

  @Test
  public void test_getConfig_unversioned() {
    UniqueId uid = UniqueId.of("DbCfg", "201");
    ConfigDocument test = _cfgMaster.get(uid);
    assert202(test);
  }
  
  @Test
  public void test_get_noType() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument test = _cfgMaster.get(uniqueId);
    assertNotNull(test);
    if (test.getConfig().getValue() instanceof ExternalId) {
      assertEquals(test.getType(), ExternalId.class);
      assert101(test);
    } else {
      Assert.fail();
    }
  }

}
