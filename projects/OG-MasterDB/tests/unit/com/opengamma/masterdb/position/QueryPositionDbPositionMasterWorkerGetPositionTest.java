/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.masterdb.position.DbPositionMasterWorker;
import com.opengamma.masterdb.position.QueryPositionDbPositionMasterWorker;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetPositionTest.class);

  private DbPositionMasterWorker _worker;

  public QueryPositionDbPositionMasterWorkerGetPositionTest(String databaseType, String databaseVersion) {
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
  public void test_getPosition_nullUID() {
    _worker.getPosition(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getPosition_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _worker.getPosition(uid);
  }

  @Test
  public void test_getPosition_versioned_oneSecurityKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122", "0");
    PositionDocument test = _worker.getPosition(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), test.getParentNodeId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), secKey.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getPosition_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121", "0");
    PositionDocument test = _worker.getPosition(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), test.getParentNodeId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
  }

  @Test
  public void test_getPosition_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "0");
    PositionDocument test = _worker.getPosition(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), test.getParentNodeId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(221.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getPosition_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "1");
    PositionDocument test = _worker.getPosition(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), test.getParentNodeId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(222.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getPosition_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _worker.getPosition(uid);
  }

  @Test
  public void test_getPosition_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionDocument test = _worker.getPosition(oid);
    
    assertNotNull(test);
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "1");
    assertEquals(uid, test.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), test.getParentNodeId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(222.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
