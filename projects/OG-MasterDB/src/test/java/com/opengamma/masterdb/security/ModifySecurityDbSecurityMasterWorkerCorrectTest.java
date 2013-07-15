/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifySecurityDbSecurityMasterWorkerCorrectTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifySecurityDbSecurityMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctSecurity_nullDocument() {
    _secMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noSecurityId() {
    UniqueId uniqueId = UniqueId.of("DbSec", "101");
    ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _secMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noSecurity() {
    SecurityDocument doc = new SecurityDocument();
    doc.setUniqueId(UniqueId.of("DbSec", "101", "0"));
    _secMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    SecurityDocument doc = new SecurityDocument(security);
    _secMaster.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueId uniqueId = UniqueId("DbSec", "201", "0");
//    DefaultSecurity security = new DefaultSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
//    SecurityDocument doc = new SecurityDocument(security);
//    _worker.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_secMaster.getClock());
    
    UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    SecurityDocument base = _secMaster.get(uniqueId);
    ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    SecurityDocument input = new SecurityDocument(security);
    
    SecurityDocument corrected = _secMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getSecurity(), corrected.getSecurity());
    
    SecurityDocument old = _secMaster.get(UniqueId.of("DbSec", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getSecurity(), old.getSecurity());
    
    SecurityHistoryRequest search = new SecurityHistoryRequest(base.getUniqueId(), now, null);
    search.setFullDetail(false);
    SecurityHistoryResult searchResult = _secMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

}
