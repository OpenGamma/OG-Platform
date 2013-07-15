/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryExchangeDbExchangeMasterWorkerGetTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryExchangeDbExchangeMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getExchange_nullUID() {
    _exgMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getExchange_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbExg", "0", "0");
    _exgMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getExchange_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbExg", "101", "1");
    _exgMaster.get(uniqueId);
  }

  @Test
  public void test_getExchange_versioned_oneExchangeDate() {
    UniqueId uniqueId = UniqueId.of("DbExg", "101", "0");
    ExchangeDocument test = _exgMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getExchange_versioned_twoExchangeDates() {
    UniqueId uniqueId = UniqueId.of("DbExg", "102", "0");
    ExchangeDocument test = _exgMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getExchange_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbExg", "201", "0");
    ExchangeDocument test = _exgMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getExchange_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbExg", "201", "1");
    ExchangeDocument test = _exgMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getExchange_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbExg", "0");
    _exgMaster.get(uniqueId);
  }

  @Test
  public void test_getExchange_unversioned() {
    UniqueId oid = UniqueId.of("DbExg", "201");
    ExchangeDocument test = _exgMaster.get(oid);
    assert202(test);
  }

}
