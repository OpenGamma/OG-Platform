/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbSecurityMasterWorker via DbSecurityMaster.
 */
@Test(groups = TestGroup.UNIT)
public abstract class AbstractDbSecurityMasterWorkerBulkTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbSecurityMasterWorkerBulkTest.class);

  protected DbSecurityMaster _secMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalSecurities;
  protected boolean _readOnly;  // attempt to speed up tests

  public AbstractDbSecurityMasterWorkerBulkTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion);
    _readOnly = readOnly;
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeClass
  public void setUpClass() throws Exception {
    if (_readOnly) {
      init();
    }
  }

  @BeforeMethod
  public void setUp() throws Exception {
    if (_readOnly == false) {
      init();
    }
  }

  private void init() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    name varchar(255) not null,
//    sec_type varchar(255) not null,
    Instant now = Instant.now();
    _secMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _secMaster.getDbConnector().getJdbcTemplate();
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity101", "EQUITY", "D");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity102", "EQUITY", "D");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity201", "EQUITY", "D");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestSecurity202", "EQUITY", "D");
    _totalSecurities = 3;
//  id bigint not null,
//  key_scheme varchar(255) not null,
//  key_value varchar(255) not null,
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        1, "A", "B");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        2, "C", "D");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        3, "E", "F");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        4, "GH", "HI");
//  security_id bigint not null,
//  idkey_id bigint not null,
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 1);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 3);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 1);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 4);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        201, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        201, 3);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        202, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        202, 3);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_readOnly == false) {
      _secMaster = null;
      super.tearDown();
    }
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    if (_readOnly) {
      _secMaster = null;
      super.tearDown();
    }
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final SecurityDocument test) {
    UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), security.getExternalIdBundle());
  }

  protected void assert102(final SecurityDocument test) {
    UniqueId uniqueId = UniqueId.of("DbSec", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity102", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI")), security.getExternalIdBundle());
  }

  protected void assert201(final SecurityDocument test) {
    UniqueId uniqueId = UniqueId.of("DbSec", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity201", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), security.getExternalIdBundle());
  }

  protected void assert202(final SecurityDocument test) {
    UniqueId uniqueId = UniqueId.of("DbSec", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity202", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), security.getExternalIdBundle());
  }

}
