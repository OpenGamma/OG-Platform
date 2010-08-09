/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPortfolioTreeDbPositionMasterWorkerUpdatePortfolioTreeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioTreeDbPositionMasterWorkerUpdatePortfolioTreeTest.class);

  private ModifyPortfolioTreeDbPositionMasterWorker _worker;
  private QueryPortfolioTreeDbPositionMasterWorker _queryWorker;

  public ModifyPortfolioTreeDbPositionMasterWorkerUpdatePortfolioTreeTest(String databaseType, String databaseVersion) {
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
  @Test(expected = NullPointerException.class)
  public void test_updatePortfolioTree_nullDocument() {
    _worker.updatePortfolioTree(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_updatePortfolioTree_noPortfolioTreeId() {
    PortfolioImpl position = new PortfolioImpl("Test");
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(position);
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_updatePortfolioTree_noPortfolioTree() {
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolioId(UniqueIdentifier.of("DbPos", "101", "101"));
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_updatePortfolioTree_notFound() {
    PortfolioImpl pos = new PortfolioImpl(UniqueIdentifier.of("DbPos", "0", "0"), "Test", new PortfolioNodeImpl("Root"));
    PortfolioTreeDocument doc = new PortfolioTreeDocument(pos);
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_updatePortfolioTree_notLatestVersion() {
    PortfolioImpl pos = new PortfolioImpl(UniqueIdentifier.of("DbPos", "201", "201"), "Test", new PortfolioNodeImpl("Root"));
    PortfolioTreeDocument doc = new PortfolioTreeDocument(pos);
    _worker.updatePortfolioTree(doc);
  }

  @Test
  public void test_updatePortfolioTree_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier oldPortfolioId = UniqueIdentifier.of("DbPos", "101", "101");
    PortfolioTreeDocument base = _queryWorker.getPortfolioTree(oldPortfolioId);
    PortfolioImpl pos = new PortfolioImpl(oldPortfolioId, "NewName", (PortfolioNodeImpl) base.getPortfolio().getRootNode());
    PortfolioTreeDocument input = new PortfolioTreeDocument(pos);
    
    PortfolioTreeDocument updated = _worker.updatePortfolioTree(input);
    assertEquals(UniqueIdentifier.of("DbPos", "101"), updated.getPortfolioId().toLatest());
    assertEquals(false, base.getPortfolioId().getVersion().equals(updated.getPortfolioId().getVersion()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getPortfolio(), updated.getPortfolio());
    
    PortfolioTreeDocument old = _queryWorker.getPortfolioTree(oldPortfolioId);
    assertEquals(base.getPortfolioId(), old.getPortfolioId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals("TestPortfolio101", old.getPortfolio().getName());
    assertEquals("TestNode111", old.getPortfolio().getRootNode().getName());
    
    PortfolioTreeDocument newer = _queryWorker.getPortfolioTree(updated.getPortfolioId());
    assertEquals(updated.getPortfolioId(), newer.getPortfolioId());
    assertEquals(now, newer.getVersionFromInstant());
    assertEquals(null, newer.getVersionToInstant());
    assertEquals(now, newer.getCorrectionFromInstant());
    assertEquals(null, newer.getCorrectionToInstant());
    assertEquals("NewName", newer.getPortfolio().getName());
    assertEquals("TestNode111", newer.getPortfolio().getRootNode().getName());
    assertEquals(old.getPortfolio().getRootNode().getUniqueIdentifier().toLatest(),
        newer.getPortfolio().getRootNode().getUniqueIdentifier().toLatest());
    assertEquals(false, old.getPortfolio().getRootNode().getUniqueIdentifier().getVersion().equals(
        newer.getPortfolio().getRootNode().getUniqueIdentifier().getVersion()));
    
    PortfolioTreeSearchHistoricRequest search = new PortfolioTreeSearchHistoricRequest(base.getPortfolioId(), null, now);
    PortfolioTreeSearchHistoricResult searchResult = _queryWorker.searchPortfolioTreeHistoric(search);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(updated.getPortfolioId(), searchResult.getDocuments().get(0).getPortfolioId());
    assertEquals(oldPortfolioId, searchResult.getDocuments().get(1).getPortfolioId());
  }

  @Test
  public void test_updatePortfolioTree_positionsRemoved() {
    Instant later = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "101");
    PositionSearchRequest search = new PositionSearchRequest();
    search.setPortfolioId(uid.toLatest());
    search.setVersionAsOfInstant(later);
    search.setCorrectedToInstant(later);
    PositionSearchResult oldPositions = _posMaster.searchPositions(search);
    assertEquals(2, oldPositions.getDocuments().size());
    
    PortfolioTreeDocument doc = _queryWorker.getPortfolioTree(uid);
    PortfolioNodeImpl rootNode = (PortfolioNodeImpl) doc.getPortfolio().getRootNode();
    rootNode.removeChildNode(rootNode.getChildNodes().get(0));
    _worker.updatePortfolioTree(doc);
    
    PositionSearchResult newPositions = _posMaster.searchPositions(search);
    assertEquals(0, newPositions.getDocuments().size());
  }

  @Test
  public void test_updatePortfolioTree_positionsRemoved_complex() {
    System.out.println("=======================================================================");
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    _posMaster.setTimeSource(TimeSource.fixed(now.minusSeconds(5)));
    PositionDocument addDoc = new PositionDocument(new PositionImpl(BigDecimal.TEN, Identifier.of("A", "B")));
    addDoc.setParentNodeId(UniqueIdentifier.of("DbPos", "113", "113"));
    addDoc = _posMaster.addPosition(addDoc);
    _posMaster.setTimeSource(TimeSource.fixed(now));
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "101");
    PositionSearchRequest search = new PositionSearchRequest();
    search.setPortfolioId(uid.toLatest());
    search.setVersionAsOfInstant(now);
    search.setCorrectedToInstant(now);
    PositionSearchResult oldPositions = _posMaster.searchPositions(search);
    assertEquals(3, oldPositions.getDocuments().size());
    
    PortfolioTreeDocument doc = _queryWorker.getPortfolioTree(uid);
    PortfolioNodeImpl node112 = (PortfolioNodeImpl) doc.getPortfolio().getRootNode().getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "112", "112"), node112.getUniqueIdentifier());
    PortfolioNode node113 = node112.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113", "113"), node113.getUniqueIdentifier());
    node112.removeChildNode(node113);
    _worker.updatePortfolioTree(doc);
    
    PositionSearchResult newPositions = _posMaster.searchPositions(search);
    assertEquals(2, newPositions.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
