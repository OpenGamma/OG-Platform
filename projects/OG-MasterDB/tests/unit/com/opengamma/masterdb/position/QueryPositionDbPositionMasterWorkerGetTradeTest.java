/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageableTrade;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetTradeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTradeTest.class);

  private DbPositionMasterWorker _worker;

  public QueryPositionDbPositionMasterWorkerGetTradeTest(String databaseType, String databaseVersion) {
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
  public void test_getTrade_nullUID() {
    _worker.get(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getTrade_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getTradePosition_versioned_404() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "404", "0");
    ManageableTrade test = _worker.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("NASDAQ", "ORCL135"), Identifier.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(100.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(404), Identifier.of("CPARTY", "C104"));
    expected.setPositionId(UniqueIdentifier.of("DbPos", "123", "0"));
    expected.setUniqueIdentifier(uid);
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_405() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "405", "0");
    ManageableTrade test = _worker.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("NASDAQ", "ORCL135"), Identifier.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(200.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(405), Identifier.of("CPARTY", "C105"));
    expected.setPositionId(UniqueIdentifier.of("DbPos", "123", "0"));
    expected.setUniqueIdentifier(uid);
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "407", "0");
    ManageableTrade test = _worker.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("TICKER", "IBMC"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(221.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(407), Identifier.of("CPARTY", "C221"));
    expected.setPositionId(UniqueIdentifier.of("DbPos", "221", "0"));
    expected.setUniqueIdentifier(uid);
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "407", "1");
    ManageableTrade test = _worker.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("TICKER", "IBMC"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), Identifier.of("CPARTY", "C222"));
    expected.setPositionId(UniqueIdentifier.of("DbPos", "221", "1"));
    expected.setUniqueIdentifier(uid);
    assertEquals(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getTradePosition_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getTradePosition_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "407");
    ManageableTrade test = _worker.getTrade(oid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("TICKER", "IBMC"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), Identifier.of("CPARTY", "C222"));
    expected.setPositionId(UniqueIdentifier.of("DbPos", "221", "1"));
    expected.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "407", "1"));
    assertEquals(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
