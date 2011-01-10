/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Types;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.TimeZone;

import org.fudgemsg.FudgeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbExchangeMasterWorker via DbExchangeMaster.
 */
@Ignore
public abstract class AbstractDbExchangeMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbExchangeMasterWorkerTest.class);

  protected DbExchangeMaster _exgMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalExchanges;

  public AbstractDbExchangeMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _exgMaster = (DbExchangeMaster) context.getBean(getDatabaseType() + "DbExchangeMaster");
    
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
    _exgMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _exgMaster.getDbSource().getJdbcTemplate();
    ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueId(UniqueIdentifier.of("DbExg", "101", "0"));
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")));
    exchange.setName("TestExchange101");
    exchange.setTimeZone(TimeZone.of("Europe/London"));
    byte[] bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange101", "Europe/London", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueIdentifier.of("DbExg", "102", "0"));
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H")));
    exchange.setName("TestExchange102");
    exchange.setTimeZone(TimeZone.of("Europe/Paris"));
    bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange102", "Europe/Paris", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueIdentifier.of("DbExg", "201", "0"));
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")));
    exchange.setName("TestExchange201");
    exchange.setTimeZone(TimeZone.of("Asia/Tokyo"));
    bytes = fudgeContext.toByteArray(fudgeContext.toFudgeMsg(exchange).getMessage());
    template.update("INSERT INTO exg_exchange VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP,
        "TestExchange201", "Asia/Tokyo", new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    exchange.setUniqueId(UniqueIdentifier.of("DbExg", "201", "1"));
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")));
    exchange.setName("TestExchange202");
    exchange.setTimeZone(TimeZone.of("Asia/Tokyo"));
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

  @After
  public void tearDown() throws Exception {
    _exgMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ExchangeDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uid, exchange.getUniqueId());
    assertEquals("TestExchange101", test.getName());
    assertEquals(TimeZone.of("Europe/London"), exchange.getTimeZone());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")), exchange.getIdentifiers());
  }

  protected void assert102(final ExchangeDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uid, exchange.getUniqueId());
    assertEquals("TestExchange102", test.getName());
    assertEquals(TimeZone.of("Europe/Paris"), exchange.getTimeZone());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H")), exchange.getIdentifiers());
  }

  protected void assert201(final ExchangeDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uid, exchange.getUniqueId());
    assertEquals("TestExchange201", test.getName());
    assertEquals(TimeZone.of("Asia/Tokyo"), exchange.getTimeZone());
    assertEquals(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")), exchange.getIdentifiers());
  }

  protected void assert202(final ExchangeDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uid, exchange.getUniqueId());
    assertEquals("TestExchange202", test.getName());
    assertEquals(TimeZone.of("Asia/Tokyo"), exchange.getTimeZone());
    assertEquals(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")), exchange.getIdentifiers());
  }

}
