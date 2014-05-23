/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.masterdb.convention.DbConventionBeanMaster;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbConventionBeanMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbConventionBeanMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbConventionBeanMasterTest.class);

  private static final ExternalIdBundle BUNDLE_201 = ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
  private static final ExternalIdBundle BUNDLE_102 = ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
  private static final ExternalIdBundle BUNDLE_101 = ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));

  protected DbConventionBeanMaster _cnvMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalSecurities;

  public AbstractDbConventionBeanMasterTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    init();
  }

  @Override
  protected void doTearDown() {
    _cnvMaster = null;
  }

  @Override
  protected void doTearDownClass() {
    _cnvMaster = null;
  }

  //-------------------------------------------------------------------------
  private void init() {
    _cnvMaster = new DbConventionBeanMaster(getDbConnector());
    
//    id bigint NOT NULL,
//    oid bigint NOT NULL,
//    ver_from_instant timestamp without time zone NOT NULL,
//    ver_to_instant timestamp without time zone NOT NULL,
//    corr_from_instant timestamp without time zone NOT NULL,
//    corr_to_instant timestamp without time zone NOT NULL,
//    name varchar(255) NOT NULL,
//    main_type char NOT NULL,
//    sub_type varchar(255) NOT NULL,
//    java_type varchar(255) NOT NULL,
//    packed_data blob NOT NULL,
    Instant now = Instant.now();
    _cnvMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final JdbcOperations template = _cnvMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO cnv_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestConvention101", "S", "MOCK", "MockConvention", blob("TestConvention101", BUNDLE_101));
    template.update("INSERT INTO cnv_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestConvention102", "S", "MOCK", "MockConvention", blob("TestConvention102", BUNDLE_102));
    template.update("INSERT INTO cnv_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestConvention201", "S", "MOCK", "MockConvention", blob("TestConvention201", BUNDLE_201));
    template.update("INSERT INTO cnv_document VALUES (?,?,?,?,?, ?,?,?,?,?, ?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP,
        "TestConvention202", "S", "MOCK", "MockConvention", blob("TestConvention202", BUNDLE_201));
    _totalSecurities = 3;
//  id bigint not null,
//  key_scheme varchar(255) not null,
//  key_value varchar(255) not null,
    template.update("INSERT INTO cnv_idkey VALUES (?,?,?)",
        1, "A", "B");
    template.update("INSERT INTO cnv_idkey VALUES (?,?,?)",
        2, "C", "D");
    template.update("INSERT INTO cnv_idkey VALUES (?,?,?)",
        3, "E", "F");
    template.update("INSERT INTO cnv_idkey VALUES (?,?,?)",
        4, "GH", "HI");
//  doc_id bigint not null,
//  idkey_id bigint not null,
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        101, 1);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        101, 2);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        101, 3);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        102, 1);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        102, 2);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        102, 4);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        201, 2);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        201, 3);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        202, 2);
    template.update("INSERT INTO cnv_doc2idkey VALUES (?,?)",
        202, 3);
  }

  private Object blob(String name, ExternalIdBundle bundle) {
    MockConvention value = new MockConvention(name, bundle, Currency.GBP);
    String xml = JodaBeanSerialization.serializer(false).xmlWriter().write(value);
    byte[] bytes = ZipUtils.deflateString(xml);
    SqlLobValue lob = new SqlLobValue(bytes, getDbConnector().getDialect().getLobHandler());
    return new SqlParameterValue(Types.BLOB, lob);
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ConventionDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    MockConvention convention = (MockConvention) test.getConvention();
    assertNotNull(convention);
    assertEquals(uniqueId, convention.getUniqueId());
    assertEquals("TestConvention101", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    assertEquals(BUNDLE_101, convention.getExternalIdBundle());
  }

  protected void assert102(final ConventionDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCnv", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    MockConvention convention = (MockConvention) test.getConvention();
    assertNotNull(convention);
    assertEquals(uniqueId, convention.getUniqueId());
    assertEquals("TestConvention102", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    assertEquals(BUNDLE_102, convention.getExternalIdBundle());
  }

  protected void assert201(final ConventionDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCnv", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    MockConvention convention = (MockConvention) test.getConvention();
    assertNotNull(convention);
    assertEquals(uniqueId, convention.getUniqueId());
    assertEquals("TestConvention201", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    assertEquals(BUNDLE_201, convention.getExternalIdBundle());
  }

  protected void assert202(final ConventionDocument test) {
    UniqueId uniqueId = UniqueId.of("DbCnv", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    MockConvention convention = (MockConvention) test.getConvention();
    assertNotNull(convention);
    assertEquals(uniqueId, convention.getUniqueId());
    assertEquals("TestConvention202", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    assertEquals(BUNDLE_201, convention.getExternalIdBundle());
  }

}
