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
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerCorrectTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctConfig_nullDocument() {
    _cfgMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noConfigId() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("Name");
    doc.setValue(ExternalId.of("A", "B"));
    _cfgMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setUniqueId(uniqueId);
    doc.setName("Name");
    doc.setValue(ExternalId.of("A", "B"));
    _cfgMaster.correct(doc);
  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument<ExternalId> base = _cfgMaster.get(uniqueId, ExternalId.class);
    ConfigDocument<ExternalId> input = new ConfigDocument<ExternalId>(ExternalId.class);
    input.setUniqueId(uniqueId);
    input.setName("NewName");
    input.setValue(ExternalId.of("A", "B"));
    
    ConfigDocument<ExternalId> corrected = _cfgMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals("NewName", corrected.getName());
    assertEquals(ExternalId.of("A", "B"), corrected.getValue());
    
    ConfigDocument<ExternalId> old = _cfgMaster.get(uniqueId, ExternalId.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getValue(), old.getValue());
    
    ConfigHistoryRequest<ExternalId> search = new ConfigHistoryRequest<ExternalId>(base.getUniqueId(), now, null);
    search.setType(ExternalId.class);
    
    ConfigHistoryResult<ExternalId> searchResult = _cfgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_correct_nameChangeNullValue() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    ConfigDocument<ExternalId> base = _cfgMaster.get(uniqueId, ExternalId.class);
    ConfigDocument<ExternalId> input = new ConfigDocument<ExternalId>(ExternalId.class);
    input.setUniqueId(uniqueId);
    input.setName("NewName");
    input.setValue(null);  // name change only
    
    ConfigDocument<ExternalId> corrected = _cfgMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals("NewName", corrected.getName());  // name changed
    assertEquals(base.getValue(), corrected.getValue());  // value unchanged
    
    ConfigDocument<ExternalId> old = _cfgMaster.get(uniqueId, ExternalId.class);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getName(), old.getName());
    assertEquals(base.getValue(), old.getValue());
  }

}
