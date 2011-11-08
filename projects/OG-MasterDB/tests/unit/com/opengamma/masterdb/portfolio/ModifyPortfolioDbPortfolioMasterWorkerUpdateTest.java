/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
public class ModifyPortfolioDbPortfolioMasterWorkerUpdateTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPortfolioDbPortfolioMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullDocument() {
    _prtMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noPortfolioId() {
    ManageablePortfolio position = new ManageablePortfolio("Test");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(position);
    _prtMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noPortfolio() {
    PortfolioDocument doc = new PortfolioDocument();
    doc.setUniqueId(UniqueId.of("DbPrt", "101", "0"));
    _prtMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueId(UniqueId.of("DbPrt", "0", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioDocument doc = new PortfolioDocument(port);
    _prtMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueId(UniqueId.of("DbPrt", "201", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioDocument doc = new PortfolioDocument(port);
    _prtMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    UniqueId oldPortfolioId = UniqueId.of("DbPrt", "101", "0");
    PortfolioDocument base = _prtMaster.get(oldPortfolioId);
    Map<String, String> oldAttr = base.getPortfolio().getAttributes();
    assertNotNull(oldAttr);
    ManageablePortfolio port = new ManageablePortfolio("NewName");
    port.setUniqueId(oldPortfolioId);
    port.setRootNode(base.getPortfolio().getRootNode());
    Map<String, String> newAttr = getNewAttributes();
    port.setAttributes(newAttr);
    PortfolioDocument input = new PortfolioDocument(port);
    
    PortfolioDocument updated = _prtMaster.update(input);
    assertEquals(UniqueId.of("DbPrt", "101"), updated.getUniqueId().toLatest());
    assertEquals(false, base.getUniqueId().getVersion().equals(updated.getUniqueId().getVersion()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getPortfolio(), updated.getPortfolio());
    
    PortfolioDocument old = _prtMaster.get(oldPortfolioId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals("TestPortfolio101", old.getPortfolio().getName());
    assertEquals("TestNode111", old.getPortfolio().getRootNode().getName());
    assertEquals(base.getPortfolio().getAttributes(), old.getPortfolio().getAttributes());
    
    PortfolioDocument newer = _prtMaster.get(updated.getUniqueId());
    assertEquals(updated.getUniqueId(), newer.getUniqueId());
    assertEquals(now, newer.getVersionFromInstant());
    assertEquals(null, newer.getVersionToInstant());
    assertEquals(now, newer.getCorrectionFromInstant());
    assertEquals(null, newer.getCorrectionToInstant());
    assertEquals("NewName", newer.getPortfolio().getName());
    assertEquals("TestNode111", newer.getPortfolio().getRootNode().getName());
    assertEquals(old.getPortfolio().getRootNode().getUniqueId().toLatest(),
        newer.getPortfolio().getRootNode().getUniqueId().toLatest());
    assertEquals(false, old.getPortfolio().getRootNode().getUniqueId().getVersion().equals(
        newer.getPortfolio().getRootNode().getUniqueId().getVersion()));
    assertEquals(newAttr, newer.getPortfolio().getAttributes());
    
    PortfolioHistoryRequest search = new PortfolioHistoryRequest(base.getUniqueId(), null, now);
    PortfolioHistoryResult searchResult = _prtMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(updated.getUniqueId(), searchResult.getDocuments().get(0).getUniqueId());
    assertEquals(oldPortfolioId, searchResult.getDocuments().get(1).getUniqueId());
    
    assertEquals(newAttr, searchResult.getDocuments().get(0).getPortfolio().getAttributes());
    assertEquals(oldAttr, searchResult.getDocuments().get(1).getPortfolio().getAttributes());
  }

  private Map<String, String> getNewAttributes() {
    Map<String, String> newAttr = Maps.newHashMap();
    newAttr.put("NA1", "VA1");
    newAttr.put("NA2", "VA2");
    newAttr.put("NA3", "VA3");
    return newAttr;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_prtMaster.getClass().getSimpleName() + "[DbPrt]", _prtMaster.toString());
  }

}
