/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Types;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbConfigMasterWorker via DbConfigMaster.
 */
@Ignore
public abstract class AbstractDbConfigTypeMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbConfigTypeMasterWorkerTest.class);

  protected DbConfigTypeMaster<Identifier> _cfgMaster;
  protected Instant _version1aInstant;
  protected Instant _version1bInstant;
  protected Instant _version1cInstant;
  protected Instant _version2Instant;
  protected int _totalConfigs;

  public AbstractDbConfigTypeMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    DbConfigMaster master = (DbConfigMaster) context.getBean(getDatabaseType() + "DbConfigMaster");
    _cfgMaster = (DbConfigTypeMaster<Identifier>) master.typed(Identifier.class);
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    name varchar(255) not null,
//    config_type varchar(255) not null,
//    config blob not null,
    Instant now = Instant.now();
    _cfgMaster.setTimeSource(TimeSource.fixed(now));
    _version1aInstant = now.minusSeconds(102);
    _version1bInstant = now.minusSeconds(101);
    _version1cInstant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    FudgeMsgEnvelope env = fudgeContext.toFudgeMsg(Identifier.of("A", "B"));
    byte[] bytes = fudgeContext.toByteArray(env.getMessage());
    String cls = Identifier.class.getName();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _cfgMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?)",
        101, 101, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig101", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?)",
        102, 102, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig102", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?)",
        201, 201, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), "TestConfig201", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig202", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalConfigs = 3;
  }

  @After
  public void tearDown() throws Exception {
    _cfgMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getConfigId());
    assertEquals(_version1aInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals("TestConfig101", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert102(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getConfigId());
    assertEquals(_version1bInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals("TestConfig102", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert201(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getConfigId());
    assertEquals(_version1cInstant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals("TestConfig201", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert202(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getConfigId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

}
