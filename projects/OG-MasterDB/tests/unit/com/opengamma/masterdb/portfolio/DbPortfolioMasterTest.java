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
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * Test DbPortfolioMaster.
 */
public class DbPortfolioMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbPortfolioMasterTest.class);

  private DbPortfolioMaster _prtMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbPortfolioMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _prtMaster = (DbPortfolioMaster) context.getBean(getDatabaseType() + "DbPortfolioMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _prtMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_prtMaster);
    assertEquals(true, _prtMaster.getUniqueIdScheme().equals("DbPrt"));
    assertNotNull(_prtMaster.getDbConnector());
    assertNotNull(_prtMaster.getTimeSource());
  }
  
  @Test(description = "[PLAT-1723]") 
  public void test_duplicate_names() throws Exception {
    PortfolioDocument a = new PortfolioDocument();
    a.setPortfolio( new ManageablePortfolio("Name"));
    _prtMaster.add(a);
    
    PortfolioDocument b = new PortfolioDocument();
    b.setPortfolio( new ManageablePortfolio("Name"));
    _prtMaster.add(b);
    
    PortfolioSearchResult search = _prtMaster.search(new PortfolioSearchRequest());
    assertEquals(2, search.getPortfolios().size());
  }

  @Test(description = "[PLAT-1723]") 
  public void test_duplicate_names_complex() throws Exception {
    
    //Try to make the table big enough that database looses presumed order guarantees
    for (int i=0;i<10;i++)
    {
      String portfolioName = "Portfolio";
      PortfolioDocument a = new PortfolioDocument();
      a.setPortfolio( new ManageablePortfolio(portfolioName));
      _prtMaster.add(a);
      for (int j = 0;j<10;j++){
        ManageablePortfolioNode child = new ManageablePortfolioNode("X");
        child.addChildNode(new ManageablePortfolioNode("Y"));
        a.getPortfolio().getRootNode().addChildNode(child);
        _prtMaster.update(a);
      }
      
      PortfolioDocument b = new PortfolioDocument();
      b.setPortfolio( new ManageablePortfolio(portfolioName));
      for (int j = 0;j<10;j++){
        ManageablePortfolioNode childB = new ManageablePortfolioNode("X");
        childB.addChildNode(new ManageablePortfolioNode("Y"));
        b.getPortfolio().getRootNode().addChildNode(childB);
      }
      _prtMaster.add(b);
  
      for (int j = 0;j<10;j++){
        ManageablePortfolioNode child = new ManageablePortfolioNode("X");
        child.addChildNode(new ManageablePortfolioNode("Y"));
        a.getPortfolio().getRootNode().addChildNode(child);
        _prtMaster.update(a);
        
        PortfolioSearchRequest request = new PortfolioSearchRequest();
        request.setName(portfolioName);
        PortfolioSearchResult search = _prtMaster.search(request);
        assertEquals(2 * (i+1), search.getPortfolios().size());
      }
      
      PortfolioSearchRequest request = new PortfolioSearchRequest();
      request.setName(portfolioName);
      PortfolioSearchResult search = _prtMaster.search(request);
      assertEquals(2 * (i+1), search.getPortfolios().size());
    }
  }

  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbPortfolioMaster[DbPrt]", _prtMaster.toString());
  }

}
