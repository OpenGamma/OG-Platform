/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.financial.master.db.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbPositionMasterWorker via DbPositionMaster.
 */
@Ignore
public abstract class AbstractDbPositionMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbPositionMasterWorkerTest.class);

  protected DbPositionMaster _posMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalPortfolios;
  protected int _totalPositions;

  public AbstractDbPositionMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _posMaster = (DbPositionMaster) context.getBean(getDatabaseType() + "DbPositionMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    name varchar(255) not null,
    Instant now = Instant.nowSystemClock();
    _posMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _posMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio101");
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio201");
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestPortfolio202");
    _totalPortfolios = 2;
//    id bigint not null,
//    oid bigint not null,
//    portfolio_id bigint not null,
//    portfolio_oid bigint not null,
//    parent_node_id bigint,
//    depth int,
//    tree_left bigint not null,
//    tree_right bigint not null,
//    name varchar(255),
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?,?)",
        111, 111, 101, 101, null, 0, 1, 6, "TestNode111");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?,?)",
        112, 112, 101, 101, 111, 1, 2, 5, "TestNode112");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?,?)",
        113, 113, 101, 101, 112, 2, 3, 4, "TestNode113");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?,?)",
        211, 211, 201, 201, null, 0, 1, 2, "TestNode211");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?,?)",
        212, 211, 202, 201, null, 0, 1, 2, "TestNode212");
//    id bigint not null,
//    oid bigint not null,
//    portfolio_oid bigint not null,
//    parent_node_oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    quantity decimal(31,8) not null,
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        100, 100, 101, 112, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(100.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        120, 120, 101, 112, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(120.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        121, 121, 101, 112, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(121.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        122, 122, 101, 112, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(122.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        123, 123, 101, 112, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(123.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        221, 221, 201, 211, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(221.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        222, 221, 201, 211, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, BigDecimal.valueOf(222.987));
    _totalPositions = 6;
//    id bigint not null,
//    position_id bigint not null,
//    id_scheme varchar(255) not null,
//    id_value varchar(255) not null,
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        120, 100, "TICKER", "S100");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        130, 120, "TICKER", "T130");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        131, 121, "TICKER", "MSFT");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        132, 121, "NASDAQ", "Micro");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        133, 122, "TICKER", "ORCL");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        134, 123, "TICKER", "ORCL134");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        135, 123, "NASDAQ", "ORCL135");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        231, 221, "TICKER", "IBMC");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        232, 222, "TICKER", "IBMC");
    
    
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        400, 400, 120,  BigDecimal.valueOf(120.987), toSqlTimestamp(_version1Instant.minusSeconds(120)), "CPARTY", "C100");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        401, 401, 121,  BigDecimal.valueOf(121.987), toSqlTimestamp(_version1Instant.minusSeconds(121)), "CPARTY", "C101");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        402, 402, 122,  BigDecimal.valueOf(100.987), toSqlTimestamp(_version1Instant.minusSeconds(122)), "CPARTY", "JMP");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        403, 403, 122,  BigDecimal.valueOf(22.987), toSqlTimestamp(_version1Instant.minusSeconds(122)), "CPARTY", "CISC");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        404, 404, 123,  BigDecimal.valueOf(100.987), toSqlTimestamp(_version1Instant.minusSeconds(123)), "CPARTY", "C104");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        405, 405, 123,  BigDecimal.valueOf(200.987), toSqlTimestamp(_version1Instant.minusSeconds(123)), "CPARTY", "C105");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        406, 406, 123,  BigDecimal.valueOf(300.987), toSqlTimestamp(_version1Instant.minusSeconds(123)), "CPARTY", "C106");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        407, 407, 221,  BigDecimal.valueOf(221.987), toSqlTimestamp(_version1Instant.minusSeconds(100)), "CPARTY", "C221");
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?)", 
        408, 407, 222,  BigDecimal.valueOf(222.987), toSqlTimestamp(_version2Instant.minusSeconds(100)), "CPARTY", "C222");
    
    
    
  }

  @After
  public void tearDown() throws Exception {
    _posMaster = null;
    super.tearDown();
  }

}
