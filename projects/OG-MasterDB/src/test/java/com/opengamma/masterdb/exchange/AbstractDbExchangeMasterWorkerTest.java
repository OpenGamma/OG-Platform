/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.threeten.bp.temporal.ChronoUnit.HOURS;
import static org.threeten.bp.temporal.ChronoUnit.MINUTES;

import java.sql.Types;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbExchangeMasterWorker via DbExchangeMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbExchangeMasterWorkerTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbExchangeMasterWorkerTest.class);

  protected DbExchangeMaster _exgMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalExchanges;

  public AbstractDbExchangeMasterWorkerTest(String databaseType, String databaseVersion, boolean readOnly) {
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
    _exgMaster = null;
  }

  @Override
  protected void doTearDownClass() {
    _exgMaster = null;
  }

  //-------------------------------------------------------------------------
  protected ObjectId setupTestData(Instant now) {
    Clock origClock = _exgMaster.getClock();
    try {
      _exgMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      ManageableExchange exchange = new ManageableExchange(bundle, "initial", region, null);
      ExchangeDocument initialDoc = new ExchangeDocument(exchange);

      _exgMaster.add(initialDoc);

      ObjectId baseOid = initialDoc.getObjectId();

      List<ExchangeDocument> firstReplacement = newArrayList();
      for (int i = 0; i < 5; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "setup_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(i, MINUTES));
        firstReplacement.add(doc);
      }
      _exgMaster.setClock(Clock.fixed(now.plus(1, HOURS), ZoneOffset.UTC));
      _exgMaster.replaceVersions(baseOid, firstReplacement);
      return baseOid;
      
    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  private void init() {
    _exgMaster = new DbExchangeMaster(getDbConnector());
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    name varchar(255) not null,
//    time_zone varchar(255),
//    detail blob not null,
    Instant now = Instant.now();
    _exgMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    LobHandler lobHandler = new DefaultLobHandler();
    final JdbcOperations template = _exgMaster.getDbConnector().getJdbcOperations();
    ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueId(UniqueId.of("DbExg", "101", "0"));
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    exchange.setName("TestExchange101");
    exchange.setTimeZone(ZoneId.of("Europe/London"));
    byte[] bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange101", "Europe/London", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueId.of("DbExg", "102", "0"));
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H")));
    exchange.setName("TestExchange102");
    exchange.setTimeZone(ZoneId.of("Europe/Paris"));
    bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange102", "Europe/Paris", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueId.of("DbExg", "201", "0"));
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    exchange.setName("TestExchange201");
    exchange.setTimeZone(ZoneId.of("Asia/Tokyo"));
    bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange201", "Asia/Tokyo", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueId.of("DbExg", "201", "1"));
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    exchange.setName("TestExchange202");
    exchange.setTimeZone(ZoneId.of("Asia/Tokyo"));
    bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP,
        "TestExchange202", "Asia/Tokyo", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalExchanges = 3;
//  id bigint not null,
//  key_scheme varchar(255) not null,
//  key_value varchar(255) not null,
    template.update("INSERT INTO exg_idkey VALUES (?,?,?)",
        1, "A", "B");
    template.update("INSERT INTO exg_idkey VALUES (?,?,?)",
        2, "C", "D");
    template.update("INSERT INTO exg_idkey VALUES (?,?,?)",
        3, "E", "F");
    template.update("INSERT INTO exg_idkey VALUES (?,?,?)",
        4, "G", "H");
//  exchange_id bigint not null,
//  idkey_id bigint not null,
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        101, 1);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        101, 2);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        101, 3);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        102, 1);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        102, 2);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        102, 4);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        201, 2);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        201, 3);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        202, 2);
    template.update("INSERT INTO exg_exchange2idkey VALUES (?,?)",
        202, 3);
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ExchangeDocument test) {
    UniqueId uniqueId = UniqueId.of("DbExg", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uniqueId, exchange.getUniqueId());
    assertEquals("TestExchange101", test.getName());
    assertEquals(ZoneId.of("Europe/London"), exchange.getTimeZone());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), exchange.getExternalIdBundle());
  }

  protected void assert102(final ExchangeDocument test) {
    UniqueId uniqueId = UniqueId.of("DbExg", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uniqueId, exchange.getUniqueId());
    assertEquals("TestExchange102", test.getName());
    assertEquals(ZoneId.of("Europe/Paris"), exchange.getTimeZone());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H")), exchange.getExternalIdBundle());
  }

  protected void assert201(final ExchangeDocument test) {
    UniqueId uniqueId = UniqueId.of("DbExg", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uniqueId, exchange.getUniqueId());
    assertEquals("TestExchange201", test.getName());
    assertEquals(ZoneId.of("Asia/Tokyo"), exchange.getTimeZone());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), exchange.getExternalIdBundle());
  }

  protected void assert202(final ExchangeDocument test) {
    UniqueId uniqueId = UniqueId.of("DbExg", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uniqueId, exchange.getUniqueId());
    assertEquals("TestExchange202", test.getName());
    assertEquals(ZoneId.of("Asia/Tokyo"), exchange.getTimeZone());
    assertEquals(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")), exchange.getExternalIdBundle());
  }

}
