/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
public class QueryConfigDbConfigMasterWorkerGetTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerGetTest.class);

  public QueryConfigDbConfigMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_getConfig_nullUID() {
    _cfgMaster.get(null, Object.class);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getConfig_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0", "0");
    _cfgMaster.get(uid, Object.class);
  }

  @Test
  public void test_getConfig_versioned_oneConfigKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> test = _cfgMaster.get(uid, Identifier.class);
    assert101(test);
  }

  @Test
  public void test_getConfig_versioned_twoConfigKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "102", "0");
    ConfigDocument<Identifier> test = _cfgMaster.get(uid, Identifier.class);
    assert102(test);
  }

  @Test
  public void test_getConfig_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    ConfigDocument<Identifier> test = _cfgMaster.get(uid, Identifier.class);
    assert201(test);
  }

  @Test
  public void test_getConfig_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "1");
    ConfigDocument<Identifier> test = _cfgMaster.get(uid, Identifier.class);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getConfig_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0");
    _cfgMaster.get(uid, Object.class);
  }

  @Test
  public void test_getConfig_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigDocument<Identifier> test = _cfgMaster.get(oid, Identifier.class);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
