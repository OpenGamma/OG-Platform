/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PositionDocument;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTest.class);

  private DbPositionMasterWorker _worker;

  public QueryPositionDbPositionMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPositionDbPositionMasterWorker();
    _worker.init(_posMaster);
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
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getPosition_versioned_oneSecurityKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122", "0");
    PositionDocument test = _worker.get(uid);
    assert122(test);
  }

  @Test
  public void test_getPosition_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121", "0");
    PositionDocument test = _worker.get(uid);
    assert121(test);
  }

  @Test
  public void test_getPosition_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "0");
    PositionDocument test = _worker.get(uid);
    assert221(test);
  }

  @Test
  public void test_getPosition_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "1");
    PositionDocument test = _worker.get(uid);
    assert222(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getPosition_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getPosition_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionDocument test = _worker.get(oid);
    assert222(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
