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
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerUpdateTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public ModifyConfigDbConfigMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateConfig_nullDocument() {
    _cfgMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noConfigId() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>(Identifier.class);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0", "0");
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>(Identifier.class);
    doc.setUniqueId(uid);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>(Identifier.class);
    doc.setUniqueId(uid);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _cfgMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> base = _cfgMaster.get(uid, Identifier.class);
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>(Identifier.class);
    input.setUniqueId(uid);
    input.setName("NewName");
    input.setValue(Identifier.of("A", "B"));
    
    ConfigDocument<Identifier> updated = _cfgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals("NewName", updated.getName());
    assertEquals(Identifier.of("A", "B"), updated.getValue());
    
    ConfigDocument<Identifier> old = _cfgMaster.get(uid, Identifier.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getName(), old.getName());
    assertEquals(base.getValue(), old.getValue());
    
    ConfigHistoryRequest<Identifier> search = new ConfigHistoryRequest<Identifier>(base.getUniqueId(), null, now);
    search.setType(Identifier.class);
    
    ConfigHistoryResult<Identifier> searchResult = _cfgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_nameChangeNullValue() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> base = _cfgMaster.get(uid, Identifier.class);
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>(Identifier.class);
    input.setUniqueId(uid);
    input.setName("NewName");
    input.setValue(null);  // name change only
    
    ConfigDocument<Identifier> updated = _cfgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals("NewName", updated.getName());  // name changed
    assertEquals(base.getValue(), updated.getValue());  // value unchanged
    
    ConfigDocument<Identifier> old = _cfgMaster.get(uid, Identifier.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getName(), old.getName());
    assertEquals(base.getValue(), old.getValue());
  }

  @Test
  public void test_update_rollback() {
    DbConfigWorker w = new DbConfigWorker(_cfgMaster.getDbSource(), _cfgMaster.getIdentifierScheme()) {
      @Override
      protected String sqlInsertConfig() {
        return "INSERT";  // bad sql
      }
    };
    final ConfigDocument<Identifier> base = _cfgMaster.get(UniqueIdentifier.of("DbCfg", "101", "0"), Identifier.class);
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>(Identifier.class);
    input.setUniqueId(uid);
    input.setName("Name");
    input.setValue(Identifier.of("A", "B"));
    try {
      w.update(input);
      Assert.fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final ConfigDocument<Identifier> test = _cfgMaster.get(UniqueIdentifier.of("DbCfg", "101", "0"), Identifier.class);
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
