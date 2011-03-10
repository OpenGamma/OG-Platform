/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerAddTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerAddTest.class);

  public ModifyConfigDbConfigMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_addConfig_nullDocument() {
    _cfgMaster.add(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_add_noConfig() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    _cfgMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("TestConfig");
    doc.setValue(Identifier.of("A", "B"));
    ConfigDocument<Identifier> test = _cfgMaster.add(doc);
    
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
    ConfigDocument<Identifier> added = _cfgMaster.add(doc);
    
    ConfigDocument<Identifier> test = _cfgMaster.get(added.getUniqueId(), Identifier.class);
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
