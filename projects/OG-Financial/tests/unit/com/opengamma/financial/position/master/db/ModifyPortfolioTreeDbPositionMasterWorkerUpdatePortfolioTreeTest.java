/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PortfolioTreeHistoryRequest;
import com.opengamma.financial.position.master.PortfolioTreeHistoryResult;
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
    ManageablePortfolio position = new ManageablePortfolio("Test");
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(position);
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_updatePortfolioTree_noPortfolioTree() {
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolioId(UniqueIdentifier.of("DbPos", "101", "0"));
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_updatePortfolioTree_notFound() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "0", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioTreeDocument doc = new PortfolioTreeDocument(port);
    _worker.updatePortfolioTree(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_updatePortfolioTree_notLatestVersion() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "201", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioTreeDocument doc = new PortfolioTreeDocument(port);
    _worker.updatePortfolioTree(doc);
  }

  @Test
  public void test_updatePortfolioTree_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier oldPortfolioId = UniqueIdentifier.of("DbPos", "101", "0");
    PortfolioTreeDocument base = _queryWorker.getPortfolioTree(oldPortfolioId);
    ManageablePortfolio port = new ManageablePortfolio("NewName");
    port.setUniqueIdentifier(oldPortfolioId);
    port.setRootNode(base.getPortfolio().getRootNode());
    PortfolioTreeDocument input = new PortfolioTreeDocument(port);
    
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
    
    PortfolioTreeHistoryRequest search = new PortfolioTreeHistoryRequest(base.getPortfolioId(), null, now);
    PortfolioTreeHistoryResult searchResult = _queryWorker.historyPortfolioTree(search);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(updated.getPortfolioId(), searchResult.getDocuments().get(0).getPortfolioId());
    assertEquals(oldPortfolioId, searchResult.getDocuments().get(1).getPortfolioId());
  }

  @Test
  public void test_updatePortfolioTree_positionsRemoved() {
    Instant later = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    PositionSearchRequest search = new PositionSearchRequest();
    search.setPortfolioId(uid.toLatest());
    search.setVersionAsOfInstant(later);
    search.setCorrectedToInstant(later);
    PositionSearchResult oldPositions = _posMaster.searchPositions(search);
    assertEquals(5, oldPositions.getDocuments().size());
    
    PortfolioTreeDocument doc = _queryWorker.getPortfolioTree(uid);
    ManageablePortfolioNode rootNode = doc.getPortfolio().getRootNode();
    rootNode.removeNode(UniqueIdentifier.of("DbPos", "112"));
    _worker.updatePortfolioTree(doc);
    
    PositionSearchResult newPositions = _posMaster.searchPositions(search);
    assertEquals(0, newPositions.getDocuments().size());
  }

  @Test
  public void test_updatePortfolioTree_positionsRemoved_complex() {
    System.out.println("=======================================================================");
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    _posMaster.setTimeSource(TimeSource.fixed(now.minusSeconds(5)));
    PositionDocument addDoc = new PositionDocument(new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B")));
    addDoc.setParentNodeId(UniqueIdentifier.of("DbPos", "113", "0"));
    addDoc = _posMaster.addPosition(addDoc);
    _posMaster.setTimeSource(TimeSource.fixed(now));
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    PositionSearchRequest search = new PositionSearchRequest();
    search.setPortfolioId(uid.toLatest());
    search.setVersionAsOfInstant(now);
    search.setCorrectedToInstant(now);
    PositionSearchResult oldPositions = _posMaster.searchPositions(search);
    assertEquals(6, oldPositions.getDocuments().size());
    
    PortfolioTreeDocument doc = _queryWorker.getPortfolioTree(uid);
    assertTrue(doc.getPortfolio().getRootNode().removeNode(UniqueIdentifier.of("DbPos", "113")));
    _worker.updatePortfolioTree(doc);
    
    PositionSearchResult newPositions = _posMaster.searchPositions(search);
    assertEquals(5, newPositions.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
