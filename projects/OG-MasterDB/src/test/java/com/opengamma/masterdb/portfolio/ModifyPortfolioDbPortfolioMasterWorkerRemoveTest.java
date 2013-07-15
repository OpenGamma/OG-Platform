/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyPortfolioDbPortfolioMasterWorkerRemoveTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPortfolioDbPortfolioMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbPrt", "0", "0");
    _prtMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_prtMaster.getClock());
    
    UniqueId uniqueId = UniqueId.of("DbPrt", "201", "1");
    _prtMaster.remove(uniqueId);
    PortfolioDocument test = _prtMaster.get(uniqueId);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uniqueId, portfolio.getUniqueId());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
  }

}
