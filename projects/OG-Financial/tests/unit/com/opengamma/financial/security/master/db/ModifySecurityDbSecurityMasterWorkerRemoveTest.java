/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerRemoveTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerRemoveTest.class);

  private ModifySecurityDbSecurityMasterWorker _worker;
  private DbSecurityMasterWorker _queryWorker;

  public ModifySecurityDbSecurityMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
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
  @Test(expected = DataNotFoundException.class)
  public void test_removeSecurity_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0", "0");
    _worker.remove(uid);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_secMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    _worker.remove(uid);
    SecurityDocument test = _queryWorker.get(uid);
    
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")), security.getIdentifiers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
