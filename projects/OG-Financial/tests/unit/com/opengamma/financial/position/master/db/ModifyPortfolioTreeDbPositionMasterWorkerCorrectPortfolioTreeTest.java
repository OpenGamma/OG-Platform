/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PortfolioTreeHistoryRequest;
import com.opengamma.master.position.PortfolioTreeHistoryResult;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPortfolioTreeDbPositionMasterWorkerCorrectPortfolioTreeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioTreeDbPositionMasterWorkerCorrectPortfolioTreeTest.class);

  private ModifyPortfolioTreeDbPositionMasterWorker _worker;
  private QueryPortfolioTreeDbPositionMasterWorker _queryWorker;

  public ModifyPortfolioTreeDbPositionMasterWorkerCorrectPortfolioTreeTest(String databaseType, String databaseVersion) {
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
  public void test_correctPortfolioTree_nullDocument() {
    _worker.correctPortfolioTree(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_correctPortfolioTree_noPortfolioTreeId() {
    ManageablePortfolio position = new ManageablePortfolio("Test");
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(position);
    _worker.correctPortfolioTree(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_correctPortfolioTree_noPortfolioTree() {
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolioId(UniqueIdentifier.of("DbPos", "201", "0"));
    _worker.correctPortfolioTree(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_correctPortfolioTree_notFound() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "0", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioTreeDocument doc = new PortfolioTreeDocument(port);
    _worker.correctPortfolioTree(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_correctPortfolioTree_notLatestCorrection() {
    PortfolioTreeDocument base = _queryWorker.getPortfolioTree(UniqueIdentifier.of("DbPos", "201", "0"));
    _worker.correctPortfolioTree(base);  // correction
    base = _queryWorker.getPortfolioTree(UniqueIdentifier.of("DbPos", "201", "0"));  // get old version
    _worker.correctPortfolioTree(base);  // cannot update old correction
  }

  @Test
  public void test_correctPortfolioTree_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier oldPortfolioId = UniqueIdentifier.of("DbPos", "201", "0");
    PortfolioTreeDocument base = _queryWorker.getPortfolioTree(oldPortfolioId);
    ManageablePortfolio port = new ManageablePortfolio("NewName");
    port.setUniqueIdentifier(oldPortfolioId);
    port.setRootNode(base.getPortfolio().getRootNode());
    PortfolioTreeDocument input = new PortfolioTreeDocument(port);
    
    PortfolioTreeDocument corrected = _worker.correctPortfolioTree(input);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), corrected.getPortfolioId().toLatest());
    assertEquals(false, base.getPortfolioId().getVersion().equals(corrected.getPortfolioId().getVersion()));
    assertEquals(_version1Instant, corrected.getVersionFromInstant());
    assertEquals(_version2Instant, corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getPortfolio(), corrected.getPortfolio());
    
    PortfolioTreeDocument old = _queryWorker.getPortfolioTree(oldPortfolioId);
    assertEquals(base.getPortfolioId(), old.getPortfolioId());
    assertEquals(_version1Instant, old.getVersionFromInstant());
    assertEquals(_version2Instant, old.getVersionToInstant());  // old version ended
    assertEquals(_version1Instant, old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());
    assertEquals("TestPortfolio201", old.getPortfolio().getName());
    assertEquals("TestNode211", old.getPortfolio().getRootNode().getName());
    
    PortfolioTreeDocument newer = _queryWorker.getPortfolioTree(corrected.getPortfolioId());
    assertEquals(corrected.getPortfolioId(), newer.getPortfolioId());
    assertEquals(_version1Instant, newer.getVersionFromInstant());
    assertEquals(_version2Instant, newer.getVersionToInstant());
    assertEquals(now, newer.getCorrectionFromInstant());
    assertEquals(null, newer.getCorrectionToInstant());
    assertEquals("NewName", newer.getPortfolio().getName());
    assertEquals("TestNode211", newer.getPortfolio().getRootNode().getName());
    assertEquals(old.getPortfolio().getRootNode().getUniqueIdentifier().toLatest(),
        newer.getPortfolio().getRootNode().getUniqueIdentifier().toLatest());
    assertEquals(false, old.getPortfolio().getRootNode().getUniqueIdentifier().getVersion().equals(
        newer.getPortfolio().getRootNode().getUniqueIdentifier().getVersion()));
    
    PortfolioTreeHistoryRequest search = new PortfolioTreeHistoryRequest(base.getPortfolioId(), _version1Instant.plusSeconds(5), null);
    PortfolioTreeHistoryResult searchResult = _queryWorker.historyPortfolioTree(search);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(corrected.getPortfolioId(), searchResult.getDocuments().get(0).getPortfolioId());
    assertEquals(oldPortfolioId, searchResult.getDocuments().get(1).getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
