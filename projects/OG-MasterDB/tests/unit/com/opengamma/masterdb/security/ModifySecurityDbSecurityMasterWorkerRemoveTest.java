/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
public class ModifySecurityDbSecurityMasterWorkerRemoveTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifySecurityDbSecurityMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeSecurity_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    _secMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_secMaster.getTimeSource());
    
    UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    _secMaster.remove(uniqueId);
    SecurityDocument test = _secMaster.get(uniqueId);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")), security.getIdentifiers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_secMaster.getClass().getSimpleName() + "[DbSec]", _secMaster.toString());
  }

}
