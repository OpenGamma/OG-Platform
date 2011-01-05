/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.security.DbSecurityMasterWorker;
import com.opengamma.masterdb.security.ModifySecurityDbSecurityMasterWorker;
import com.opengamma.masterdb.security.QuerySecurityDbSecurityMasterWorker;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerAddTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerAddTest.class);

  private ModifySecurityDbSecurityMasterWorker _worker;
  private DbSecurityMasterWorker _queryWorker;

  public ModifySecurityDbSecurityMasterWorkerAddTest(String databaseType, String databaseVersion) {
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
  public void test_addSecurity_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noSecurity() {
    SecurityDocument doc = new SecurityDocument();
    _worker.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_secMaster.getTimeSource());
    
    ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    SecurityDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbSec", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity testSecurity = test.getSecurity();
    assertNotNull(testSecurity);
    assertEquals(uid, testSecurity.getUniqueId());
    assertEquals("TestSecurity", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(Identifier.of("A", "B"), idKey.getIdentifiers().iterator().next());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", IdentifierBundle.of(Identifier.of("A", "B")));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    SecurityDocument added = _worker.add(doc);
    
    SecurityDocument test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
