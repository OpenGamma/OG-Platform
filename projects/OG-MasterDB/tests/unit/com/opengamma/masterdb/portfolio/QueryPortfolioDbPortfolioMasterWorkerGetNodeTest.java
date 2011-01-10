/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
public class QueryPortfolioDbPortfolioMasterWorkerGetNodeTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerGetNodeTest.class);

  private QueryPortfolioDbPortfolioMasterWorker _worker;

  public QueryPortfolioDbPortfolioMasterWorkerGetNodeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPortfolioDbPortfolioMasterWorker();
    _worker.init(_prtMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getNode_nullUID() {
    _worker.getNode(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getNode_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "0", "0");
    _worker.getNode(uid);
  }

  @Test
  public void test_getNode_versioned() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "111", "0");
    ManageablePortfolioNode test = _worker.getNode(uid);
    assertNode111(test, 999, UniqueIdentifier.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_112() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "112", "0");
    ManageablePortfolioNode test = _worker.getNode(uid);
    assertNode112(test, 999, UniqueIdentifier.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_113() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "113", "0");
    ManageablePortfolioNode test = _worker.getNode(uid);
    assertNode113(test, UniqueIdentifier.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "211", "0");
    ManageablePortfolioNode test = _worker.getNode(uid);
    assertNode211(test, UniqueIdentifier.of("DbPrt", "201", "0"));
  }

  @Test
  public void test_getNode_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "211", "1");
    ManageablePortfolioNode test = _worker.getNode(uid);
    assertNode212(test, UniqueIdentifier.of("DbPrt", "201", "1"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getNode_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "0");
    _worker.getNode(uid);
  }

  @Test
  public void test_getNode_unversioned_latest() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPrt", "211");
    ManageablePortfolioNode test = _worker.getNode(oid);
    assertNode212(test, UniqueIdentifier.of("DbPrt", "201", "1"));
  }

  @Test
  public void test_getNode_unversioned_nodesLoaded() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPrt", "111");
    ManageablePortfolioNode test = _worker.getNode(oid);
    assertNode111(test, 999, UniqueIdentifier.of("DbPrt", "101", "0"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPrt]", _worker.toString());
  }

}
