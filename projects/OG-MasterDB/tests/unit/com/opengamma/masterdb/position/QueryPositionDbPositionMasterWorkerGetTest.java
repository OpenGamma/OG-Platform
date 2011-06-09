/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public QueryPositionDbPositionMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _posMaster.get(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _posMaster.get(uid);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121", "1");
    _posMaster.get(uid);
  }

  @Test
  public void test_getPosition_versioned_oneSecurityKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122", "0");
    PositionDocument test = _posMaster.get(uid);
    assert122(test);
  }

  @Test
  public void test_getPosition_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121", "0");
    PositionDocument test = _posMaster.get(uid);
    assert121(test);
  }

  @Test
  public void test_getPosition_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "0");
    PositionDocument test = _posMaster.get(uid);
    assert221(test);
  }

  @Test
  public void test_getPosition_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "1");
    PositionDocument test = _posMaster.get(uid);
    assert222(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getPosition_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _posMaster.get(uid);
  }

  @Test
  public void test_getPosition_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionDocument test = _posMaster.get(oid);
    assert222(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
