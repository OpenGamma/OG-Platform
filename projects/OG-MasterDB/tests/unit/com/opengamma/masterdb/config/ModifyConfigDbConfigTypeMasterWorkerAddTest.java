/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.config.DbConfigTypeMasterWorker;
import com.opengamma.masterdb.config.ModifyConfigDbConfigTypeMasterWorker;
import com.opengamma.masterdb.config.QueryConfigDbConfigTypeMasterWorker;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigTypeMasterWorkerAddTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigTypeMasterWorkerAddTest.class);

  private ModifyConfigDbConfigTypeMasterWorker<Identifier> _worker;
  private DbConfigTypeMasterWorker<Identifier> _queryWorker;

  public ModifyConfigDbConfigTypeMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyConfigDbConfigTypeMasterWorker<Identifier>();
    _worker.init(_cfgMaster);
    _queryWorker = new QueryConfigDbConfigTypeMasterWorker<Identifier>();
    _queryWorker.init(_cfgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_addConfig_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noConfig() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    _worker.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("TestConfig");
    doc.setValue(Identifier.of("A", "B"));
    ConfigDocument<Identifier> test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbCfg", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "B"), test.getValue());
    assertEquals("TestConfig", test.getName());
  }

  @Test
  public void test_add_addThenGet() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("TestConfig");
    doc.setValue(Identifier.of("A", "B"));
    ConfigDocument<Identifier> added = _worker.add(doc);
    
    ConfigDocument<Identifier> test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbCfg]", _worker.toString());
  }

}
