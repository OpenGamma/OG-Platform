/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
public class ModifyPortfolioDbPortfolioMasterWorkerRemoveTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerRemoveTest.class);

  public ModifyPortfolioDbPortfolioMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_remove_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "0", "0");
    _prtMaster.remove(uid);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPrt", "201", "1");
    _prtMaster.remove(uid);
    PortfolioDocument test = _prtMaster.get(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueId());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_prtMaster.getClass().getSimpleName() + "[DbPrt]", _prtMaster.toString());
  }

}
