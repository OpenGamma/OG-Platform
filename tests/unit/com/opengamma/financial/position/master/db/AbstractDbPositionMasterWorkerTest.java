/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;

/**
 * Base tests for DbPositionMasterWorker via DbPositionMaster.
 */
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
    final String contextLocation =  "config/test-position-master-context.xml";
    final ApplicationContext context = new FileSystemXmlApplicationContext(contextLocation);
    _posMaster = (DbPositionMaster) context.getBean(getDatabaseType()+"DbPositionMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    valid_from_instant timestamp not null,
//    valid_to_instant timestamp not null,
//    effective_instant timestamp not null,
//    name varchar(255) not null,
    Instant now = Instant.nowSystemClock();
    _posMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _posMaster.getTemplate();
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        101, 101, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestPortfolio101");
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        201, 201, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.toSqlTimestamp(_version2Instant), DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestPortfolio201");
    template.update("INSERT INTO pos_portfolio VALUES (?,?,?,?,?, ?,?)",
        202, 201, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestPortfolio202");
    _totalPortfolios = 2;
//    id bigint not null,
//    oid bigint not null,
//    portfolio_id bigint not null,
//    parent_node_id bigint,
//    tree_left bigint not null,
//    tree_right bigint not null,
//    name varchar(255),
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?)",
        111, 111, 101, null, 0, 1, 6, "TestNode111");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?)",
        112, 112, 101, 111, 1, 2, 5, "TestNode112");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?)",
        113, 113, 101, 112, 2, 3, 4, "TestNode113");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?)",
        211, 211, 201, null, 0, 1, 2, "TestNode211");
    template.update("INSERT INTO pos_node VALUES (?,?,?,?,?, ?,?,?)",
        212, 211, 202, null, 0, 1, 2, "TestNode212");
//    id bigint not null,
//    oid bigint not null,
//    portfolio_oid bigint not null,
//    parent_node_oid bigint not null,
//    valid_from_instant timestamp not null,
//    valid_to_instant timestamp not null,
//    effective_instant timestamp not null,
//    quantity decimal not null,
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        121, 121, 101, 112, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, BigDecimal.valueOf(121.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        122, 122, 101, 112, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, BigDecimal.valueOf(122.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        221, 221, 201, 211, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.toSqlTimestamp(_version2Instant), DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, BigDecimal.valueOf(221.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        222, 221, 201, 211, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, BigDecimal.valueOf(222.987));
    _totalPositions = 3;
//    id bigint not null,
//    position_id bigint not null,
//    id_scheme varchar(255) not null,
//    id_value varchar(255) not null,
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        131, 121, "TICKER", "MSFT");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        132, 121, "NASDAQ", "Micro");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        133, 122, "TICKER", "ORCL");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        231, 221, "TICKER", "IBMC");
    template.update("INSERT INTO pos_securitykey VALUES (?,?,?,?)",
        232, 222, "TICKER", "IBMC");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _posMaster = null;
  }

}
