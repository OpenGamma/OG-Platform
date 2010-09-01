/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.DateUtil;

/**
 * Base tests for DbSecurityMasterWorker via DbSecurityMaster.
 */
@Ignore
public abstract class AbstractDbSecurityMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbSecurityMasterWorkerTest.class);

  private ConfigurableApplicationContext _springApplicationContext;
  protected DbSecurityMaster _secMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalSecurities;

  public AbstractDbSecurityMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    final String contextLocation =  "config/test-master-context.xml";
    _springApplicationContext = new FileSystemXmlApplicationContext(contextLocation);
    _secMaster = (DbSecurityMaster) _springApplicationContext.getBean(getDatabaseType() + "DbSecurityMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    name varchar(255) not null,
//    sec_type varchar(255) not null,
    Instant now = Instant.nowSystemClock();
    _secMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _secMaster.getJdbcTemplate();
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        101, 101, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestSecurity101", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        102, 102, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestSecurity102", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        201, 201, DateUtil.toSqlTimestamp(_version1Instant), DateUtil.toSqlTimestamp(_version2Instant), DateUtil.toSqlTimestamp(_version1Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestSecurity201", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        202, 201, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, DateUtil.toSqlTimestamp(_version2Instant), DateUtil.MAX_SQL_TIMESTAMP, "TestSecurity202", "EQUITY");
    _totalSecurities = 3;
//    id bigint not null,
//    security_id bigint not null,
//    id_scheme varchar(255) not null,
//    id_value varchar(255) not null,
    template.update("INSERT INTO sec_identitykey VALUES (?,?,?,?)",
        111, 101, "TICKER", "ORCL");
    template.update("INSERT INTO sec_identitykey VALUES (?,?,?,?)",
        112, 102, "TICKER", "MSFT");
    template.update("INSERT INTO sec_identitykey VALUES (?,?,?,?)",
        113, 102, "NASDAQ", "Micro");
    template.update("INSERT INTO sec_identitykey VALUES (?,?,?,?)",
        211, 201, "TICKER", "IBMC");
    template.update("INSERT INTO sec_identitykey VALUES (?,?,?,?)",
        212, 202, "TICKER", "IBMC");
  }

  @After
  public void tearDown() throws Exception {
    _springApplicationContext.close();
    _springApplicationContext = null;
    _secMaster = null;
    super.tearDown();
  }

}
