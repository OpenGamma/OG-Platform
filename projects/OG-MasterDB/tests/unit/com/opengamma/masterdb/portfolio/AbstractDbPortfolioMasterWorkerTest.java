/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * Base tests for DbPortfolioMasterWorker via DbPortfolioMaster.
 */
public abstract class AbstractDbPortfolioMasterWorkerTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbPortfolioMasterWorkerTest.class);

  protected DbPortfolioMaster _prtMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalPortfolios;
  protected int _visiblePortfolios;
  protected int _totalPositions;
  protected OffsetDateTime _now;

  public AbstractDbPortfolioMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _prtMaster = (DbPortfolioMaster) context.getBean(getDatabaseType() + "DbPortfolioMaster");
    
    _now = OffsetDateTime.now();
    _prtMaster.setTimeSource(TimeSource.fixed(_now.toInstant()));
    _version1Instant = _now.toInstant().minusSeconds(100);
    _version2Instant = _now.toInstant().minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _prtMaster.getDbConnector().getJdbcTemplate();
    template.update("INSERT INTO prt_portfolio VALUES (?,?,?,?,?, ?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio101", 25);
    template.update("INSERT INTO prt_portfolio VALUES (?,?,?,?,?, ?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio102", 25);
    template.update("INSERT INTO prt_portfolio VALUES (?,?,?,?,?, ?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio201", 25);
    template.update("INSERT INTO prt_portfolio VALUES (?,?,?,?,?, ?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestPortfolio202", 25);
    template.update("INSERT INTO prt_portfolio VALUES (?,?,?,?,?, ?,?,?)",
        301, 301, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestPortfolio301", 75);
    _visiblePortfolios = 3;
    _totalPortfolios = 4;
    
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        111, 111, 101, 101, null, null, 0, 1, 6, "TestNode111");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        112, 112, 101, 101, 111, 111, 1, 2, 5, "TestNode112");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        113, 113, 101, 101, 112, 112, 2, 3, 4, "TestNode113");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        121, 121, 102, 102, null, null, 0, 1, 2, "TestNode121");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        211, 211, 201, 201, null, null, 0, 1, 2, "TestNode211");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        212, 211, 202, 201, null, null, 0, 1, 2, "TestNode212");
    template.update("INSERT INTO prt_node VALUES (?,?,?,?,?, ?,?,?,?,?)",
        311, 311, 301, 301, null, null, 0, 1, 2, "TestNode311");
    
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        112, "DbPos", "500");
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        113, "DbPos", "501");
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        113, "DbPos", "502");
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        211, "DbPos", "500");
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        212, "DbPos", "500");
    template.update("INSERT INTO prt_position VALUES (?,?,?)",
        311, "DbPos", "500");
    
    template.update("INSERT INTO prt_portfolio_attribute VALUES (?,?,?,?,?)",
        10, 101, 101, "K101a", "V101a");
    template.update("INSERT INTO prt_portfolio_attribute VALUES (?,?,?,?,?)",
        11, 101, 101, "K101b", "V101b");
    template.update("INSERT INTO prt_portfolio_attribute VALUES (?,?,?,?,?)",
        12, 102, 102,  "K102a", "V102a");
    template.update("INSERT INTO prt_portfolio_attribute VALUES (?,?,?,?,?)",
        13, 102, 102, "K102b", "V102b");
    
    _totalPositions = 6;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _prtMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final PortfolioDocument test, final int depth) {
    UniqueId uniqueId = UniqueId.of("DbPrt", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestPortfolio101", portfolio.getName());
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertNode111(rootNode, depth, uniqueId);
    
    assertNotNull(portfolio.getAttributes());
    assertEquals(2, portfolio.getAttributes().size());
    assertEquals("V101a", portfolio.getAttributes().get("K101a"));
    assertEquals("V101b", portfolio.getAttributes().get("K101b"));
  }

  protected void assertNode111(final ManageablePortfolioNode node, final int depth, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "111", "0"), node.getUniqueId());
    assertEquals("TestNode111", node.getName());
    assertEquals(null, node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(0, node.getPositionIds().size());
    if (depth == 0) {
      assertEquals(0, node.getChildNodes().size());
      return;
    }
    assertEquals(1, node.getChildNodes().size());
    ManageablePortfolioNode child112 = node.getChildNodes().get(0);
    assertNode112(child112, depth, portfolioId);
  }

  protected void assertNode112(final ManageablePortfolioNode node, final int depth, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "112", "0"), node.getUniqueId());
    assertEquals("TestNode112", node.getName());
    assertEquals(UniqueId.of("DbPrt", "111", "0"), node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(node.getPositionIds().toString(), 1, node.getPositionIds().size());
    assertEquals(ObjectId.of("DbPos", "500"), node.getPositionIds().get(0));
    if (depth == 1) {
      assertEquals(0, node.getChildNodes().size());
      return;
    }
    assertEquals(1, node.getChildNodes().size());
    ManageablePortfolioNode child113 = node.getChildNodes().get(0);
    assertNode113(child113, portfolioId);
  }

  protected void assertNode113(final ManageablePortfolioNode node, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "113", "0"), node.getUniqueId());
    assertEquals("TestNode113", node.getName());
    assertEquals(UniqueId.of("DbPrt", "112", "0"), node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(0, node.getChildNodes().size());
    assertEquals(2, node.getPositionIds().size());
    assertEquals(true, node.getPositionIds().contains(ObjectId.of("DbPos", "501")));
    assertEquals(true, node.getPositionIds().contains(ObjectId.of("DbPos", "502")));
  }

  protected void assert102(final PortfolioDocument test) {
    UniqueId uniqueId = UniqueId.of("DbPrt", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestPortfolio102", portfolio.getName());
    
    assertNotNull(portfolio.getAttributes());
    assertEquals(2, portfolio.getAttributes().size());
    assertEquals("V102a", portfolio.getAttributes().get("K102a"));
    assertEquals("V102b", portfolio.getAttributes().get("K102b"));
  }

  protected void assert201(final PortfolioDocument test) {
    UniqueId uniqueId = UniqueId.of("DbPrt", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestPortfolio201", portfolio.getName());
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertNode211(rootNode, uniqueId);
    
    assertNotNull(portfolio.getAttributes());
    assertTrue(portfolio.getAttributes().isEmpty());
  }

  protected void assertNode211(final ManageablePortfolioNode node, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "211", "0"), node.getUniqueId());
    assertEquals("TestNode211", node.getName());
    assertEquals(null, node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(0, node.getChildNodes().size());
    assertEquals(1, node.getPositionIds().size());
    assertEquals(ObjectId.of("DbPos", "500"), node.getPositionIds().get(0));
  }

  protected void assert202(final PortfolioDocument test) {
    UniqueId uniqueId = UniqueId.of("DbPrt", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestPortfolio202", portfolio.getName());
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertNode212(rootNode, uniqueId);
    
    assertNotNull(portfolio.getAttributes());
    assertTrue(portfolio.getAttributes().isEmpty());
  }

  protected void assertNode212(final ManageablePortfolioNode node, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "211", "1"), node.getUniqueId());
    assertEquals("TestNode212", node.getName());
    assertEquals(null, node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(0, node.getChildNodes().size());
    assertEquals(1, node.getPositionIds().size());
    assertEquals(ObjectId.of("DbPos", "500"), node.getPositionIds().get(0));
  }
  
  protected void assert301(final PortfolioDocument test) {
    UniqueId uniqueId = UniqueId.of("DbPrt", "301", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestPortfolio301", portfolio.getName());
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertNode311(rootNode, uniqueId);
    
    assertNotNull(portfolio.getAttributes());
    assertTrue(portfolio.getAttributes().isEmpty());
  }
  
  protected void assertNode311(final ManageablePortfolioNode node, final UniqueId portfolioId) {
    assertEquals(UniqueId.of("DbPrt", "311", "0"), node.getUniqueId());
    assertEquals("TestNode311", node.getName());
    assertEquals(null, node.getParentNodeId());
    assertEquals(portfolioId, node.getPortfolioId());
    assertEquals(0, node.getChildNodes().size());
    assertEquals(1, node.getPositionIds().size());
    assertEquals(ObjectId.of("DbPos", "500"), node.getPositionIds().get(0));
  }

}
