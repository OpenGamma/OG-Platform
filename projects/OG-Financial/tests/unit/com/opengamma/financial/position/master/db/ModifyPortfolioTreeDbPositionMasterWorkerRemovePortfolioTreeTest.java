/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests ModifyPortfolioTreeDbPortfolioTreeMasterWorker.
 */
public class ModifyPortfolioTreeDbPositionMasterWorkerRemovePortfolioTreeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioTreeDbPositionMasterWorkerRemovePortfolioTreeTest.class);

  private ModifyPortfolioTreeDbPositionMasterWorker _worker;
  private QueryPortfolioTreeDbPositionMasterWorker _queryWorker;

  public ModifyPortfolioTreeDbPositionMasterWorkerRemovePortfolioTreeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyPortfolioTreeDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPortfolioTreeDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_removePortfolioTree_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _worker.removePortfolioTree(uid);
  }

  @Test
  public void test_removePortfolioTree_removed() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "1");
    _worker.removePortfolioTree(uid);
    PortfolioTreeDocument test = _queryWorker.getPortfolioTree(uid);
    
    assertEquals(uid, test.getPortfolioId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
  }

  @Test
  public void test_removePortfolioTree_positionsRemoved() {
    Instant later = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    PositionSearchRequest search = new PositionSearchRequest();
    search.setPortfolioId(uid.toLatest());
    search.setVersionAsOfInstant(later);
    search.setCorrectedToInstant(later);
    PositionSearchResult oldPositions = _posMaster.searchPositions(search);
    assertEquals(5, oldPositions.getDocuments().size());
    
    _worker.removePortfolioTree(uid);
    
    PositionSearchResult newPositions = _posMaster.searchPositions(search);
    assertEquals(0, newPositions.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
