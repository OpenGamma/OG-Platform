/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerUpdateTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerUpdateTest.class);

  private ModifySecurityDbSecurityMasterWorker _worker;
  private DbSecurityMasterWorker _queryWorker;

  public ModifySecurityDbSecurityMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
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
  public void test_updateSecurity_nullDocument() {
    _worker.update(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noSecurityId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _worker.update(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noSecurity() {
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurityId(UniqueIdentifier.of("DbSec", "101", "0"));
    _worker.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0", "0");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument(security);
    _worker.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument(security);
    _worker.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_secMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    SecurityDocument base = _queryWorker.get(uid);
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument input = new SecurityDocument(security);
    
    SecurityDocument updated = _worker.update(input);
    assertEquals(false, base.getSecurityId().equals(updated.getSecurityId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getSecurity(), updated.getSecurity());
    
    SecurityDocument old = _queryWorker.get(uid);
    assertEquals(base.getSecurityId(), old.getSecurityId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getSecurity(), old.getSecurity());
    
    SecurityHistoryRequest search = new SecurityHistoryRequest(base.getSecurityId(), null, now);
    SecurityHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    ModifySecurityDbSecurityMasterWorker w = new ModifySecurityDbSecurityMasterWorker() {
      @Override
      protected String sqlInsertSecurityIdKey() {
        return "INSERT";  // bad sql
      };
    };
    w.init(_secMaster);
    final SecurityDocument base = _queryWorker.get(UniqueIdentifier.of("DbSec", "101", "0"));
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    ManageableSecurity security = new ManageableSecurity(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument input = new SecurityDocument(security);
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final SecurityDocument test = _queryWorker.get(UniqueIdentifier.of("DbSec", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
