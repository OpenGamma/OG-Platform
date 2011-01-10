/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
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
public class QueryConfigDbConfigTypeMasterWorkerGetTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigTypeMasterWorkerGetTest.class);

  private DbConfigTypeMasterWorker<Identifier> _worker;

  public QueryConfigDbConfigTypeMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryConfigDbConfigTypeMasterWorker<Identifier>();
    _worker.init(_cfgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getConfig_nullUID() {
    _worker.get(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getConfig_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getConfig_versioned_oneConfigKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> test = _worker.get(uid);
    assert101(test);
  }

  @Test
  public void test_getConfig_versioned_twoConfigKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "102", "0");
    ConfigDocument<Identifier> test = _worker.get(uid);
    assert102(test);
  }

  @Test
  public void test_getConfig_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    ConfigDocument<Identifier> test = _worker.get(uid);
    assert201(test);
  }

  @Test
  public void test_getConfig_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "1");
    ConfigDocument<Identifier> test = _worker.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getConfig_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getConfig_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigDocument<Identifier> test = _worker.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbCfg]", _worker.toString());
  }

}
