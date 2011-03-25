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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerCorrectTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public ModifyConfigDbConfigMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctConfig_nullDocument() {
    _cfgMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noConfigId() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _cfgMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noConfig() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setUniqueId(UniqueIdentifier.of("DbCfg", "101", "0"));
    _cfgMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0", "0");
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setUniqueId(uid);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _cfgMaster.correct(doc);
  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> base = _cfgMaster.get(uid, Identifier.class);
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>();
    input.setUniqueId(uid);
    input.setName("Name");
    input.setValue(Identifier.of("A", "B"));
    
    ConfigDocument<Identifier> corrected = _cfgMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getValue(), corrected.getValue());
    
    ConfigDocument<Identifier> old = _cfgMaster.get(uid, Identifier.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getValue(), old.getValue());
    
    ConfigHistoryRequest<Identifier> search = new ConfigHistoryRequest<Identifier>(base.getUniqueId(), now, null);
    search.setType(Identifier.class);
    
    ConfigHistoryResult<Identifier> searchResult = _cfgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
