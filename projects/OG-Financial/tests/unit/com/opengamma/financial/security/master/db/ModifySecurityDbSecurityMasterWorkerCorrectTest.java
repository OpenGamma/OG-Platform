/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityHistoryRequest;
import com.opengamma.financial.security.master.SecurityHistoryResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerCorrectTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerCorrectTest.class);

  private ModifySecurityDbSecurityMasterWorker _worker;
  private DbSecurityMasterWorker _queryWorker;

  public ModifySecurityDbSecurityMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifySecurityDbSecurityMasterWorker();
    _worker.init(_secMaster);
    _queryWorker = new QuerySecurityDbSecurityMasterWorker();
    _queryWorker.init(_secMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_correctSecurity_nullDocument() {
    _worker.correct(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noSecurityId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _worker.correct(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noSecurity() {
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurityId(UniqueIdentifier.of("DbSec", "101", "0"));
    _worker.correct(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0", "0");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument(security);
    _worker.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
//    DefaultSecurity security = new DefaultSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
//    SecurityDocument doc = new SecurityDocument(security);
//    _worker.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_secMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    SecurityDocument base = _queryWorker.get(uid);
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument input = new SecurityDocument(security);
    
    SecurityDocument corrected = _worker.correct(input);
    assertEquals(false, base.getSecurityId().equals(corrected.getSecurityId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getSecurity(), corrected.getSecurity());
    
    SecurityDocument old = _queryWorker.get(UniqueIdentifier.of("DbSec", "101", "0"));
    assertEquals(base.getSecurityId(), old.getSecurityId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getSecurity(), old.getSecurity());
    
    SecurityHistoryRequest search = new SecurityHistoryRequest(base.getSecurityId(), now, null);
    SecurityHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
