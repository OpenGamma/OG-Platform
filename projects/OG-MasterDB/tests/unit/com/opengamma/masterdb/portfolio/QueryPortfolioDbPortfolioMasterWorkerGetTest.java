/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
public class QueryPortfolioDbPortfolioMasterWorkerGetTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerGetTest.class);

  private QueryPortfolioDbPortfolioMasterWorker _worker;

  public QueryPortfolioDbPortfolioMasterWorkerGetTest(String databaseType, String databaseVersion) {
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
  public void test_get_nullUID() {
    _worker.get(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_get_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_get_versioned() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "101", "0");
    PortfolioDocument test = _worker.get(uid);
    assert101(test, 999);
  }

  @Test
  public void test_get_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "201", "0");
    PortfolioDocument test = _worker.get(uid);
    assert201(test);
  }

  @Test
  public void test_get_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "201", "1");
    PortfolioDocument test = _worker.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "0");
    _worker.get(uid);
  }

  @Test
  public void test_get_unversioned_latest() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPrt", "201");
    PortfolioDocument test = _worker.get(oid);
    assert202(test);
  }

  @Test
  public void test_get_unversioned_nodesLoaded() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPrt", "101");
    PortfolioDocument test = _worker.get(oid);
    assert101(test, 999);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPrt]", _worker.toString());
  }

}
