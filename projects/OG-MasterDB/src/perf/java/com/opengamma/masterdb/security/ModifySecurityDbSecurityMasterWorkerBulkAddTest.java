/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerBulkAddTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerBulkAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifySecurityDbSecurityMasterWorkerBulkAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addSecurity_nullDocument() {
    _secMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noSecurity() {
    SecurityDocument doc = new SecurityDocument();
    _secMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_secMaster.getClock());

    ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    SecurityDocument test = _secMaster.add(doc);

    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbSec", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity testSecurity = test.getSecurity();
    assertNotNull(testSecurity);
    assertEquals(uniqueId, testSecurity.getUniqueId());
    assertEquals("TestSecurity", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    ExternalIdBundle idKey = security.getExternalIdBundle();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(ExternalId.of("A", "B"), idKey.getExternalIds().iterator().next());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    SecurityDocument added = _secMaster.add(doc);

    SecurityDocument test = _secMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

}
