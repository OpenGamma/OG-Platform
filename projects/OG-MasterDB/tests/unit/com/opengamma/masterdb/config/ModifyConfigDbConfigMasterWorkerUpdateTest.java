/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.extsql.ExtSqlConfig;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerUpdateTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateConfig_nullDocument() {
    _cfgMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noConfigId() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("Name");
    doc.setValue(ExternalId.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setUniqueId(uniqueId);
    doc.setName("Name");
    doc.setValue(ExternalId.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "201", "0");
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setUniqueId(uniqueId);
    doc.setName("Name");
    doc.setValue(ExternalId.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument<ExternalId> base = _cfgMaster.get(uniqueId, ExternalId.class);
    ConfigDocument<ExternalId> input = new ConfigDocument<ExternalId>(ExternalId.class);
    input.setUniqueId(uniqueId);
    input.setName("NewName");
    input.setValue(ExternalId.of("A", "B"));
    
    ConfigDocument<ExternalId> updated = _cfgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals("NewName", updated.getName());
    assertEquals(ExternalId.of("A", "B"), updated.getValue());
    
    ConfigDocument<ExternalId> old = _cfgMaster.get(uniqueId, ExternalId.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getName(), old.getName());
    assertEquals(base.getValue(), old.getValue());
    
    ConfigHistoryRequest<ExternalId> search = new ConfigHistoryRequest<ExternalId>(base.getUniqueId(), null, now);
    search.setType(ExternalId.class);
    
    ConfigHistoryResult<ExternalId> searchResult = _cfgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_nameChangeNullValue() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument<ExternalId> base = _cfgMaster.get(uniqueId, ExternalId.class);
    ConfigDocument<ExternalId> input = new ConfigDocument<ExternalId>(ExternalId.class);
    input.setUniqueId(uniqueId);
    input.setName("NewName");
    input.setValue(null);  // name change only
    
    ConfigDocument<ExternalId> updated = _cfgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals("NewName", updated.getName());  // name changed
    assertEquals(base.getValue(), updated.getValue());  // value unchanged
    
    ConfigDocument<ExternalId> old = _cfgMaster.get(uniqueId, ExternalId.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getName(), old.getName());
    assertEquals(base.getValue(), old.getValue());
  }

  @Test
  public void test_update_rollback() {
    DbConfigWorker w = new DbConfigWorker(_cfgMaster.getDbConnector(), _cfgMaster.getUniqueIdScheme());
    w.setExtSqlBundle(ExtSqlBundle.of(new ExtSqlConfig("Invalid"), DbConfigMaster.class));
    final ConfigDocument<ExternalId> base = _cfgMaster.get(UniqueId.of("DbCfg", "101", "0"), ExternalId.class);
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument<ExternalId> input = new ConfigDocument<ExternalId>(ExternalId.class);
    input.setUniqueId(uniqueId);
    input.setName("Name");
    input.setValue(ExternalId.of("A", "B"));
    try {
      w.update(input);
      Assert.fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final ConfigDocument<ExternalId> test = _cfgMaster.get(UniqueId.of("DbCfg", "101", "0"), ExternalId.class);
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
