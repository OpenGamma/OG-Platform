/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tool;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.examples.simulated.DBTestUtils;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.simulated.loader.ExampleMultiAssetPortfolioLoader;
import com.opengamma.examples.simulated.tool.ExampleDatabasePopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test example database population
 */
@Test(groups = TestGroup.INTEGRATION)
public class ExampleDatabasePopulatorTest {

  private static final String CONFIG_RESOURCE_LOCATION = "classpath:/toolcontext/toolcontext-examplessimulated.properties";

//  @BeforeMethod
//  public void setUp() throws IOException {
//    DBTestUtils.createHsqlDB(CONFIG_RESOURCE_LOCATION);
//  }
//  
//  @AfterMethod
//  public void runAfter() throws IOException {
//    DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
//  }

  @Test(enabled=false)
  public void testPortfolioAndDataLoaded() throws Exception {
    DBTestUtils.createTestHsqlDB(CONFIG_RESOURCE_LOCATION);
    
    if (!(new ExampleDatabasePopulator().run(CONFIG_RESOURCE_LOCATION, ToolContext.class))) {
      fail();
    }
    
    ToolContext toolContext = getToolContext();
    try {
      assertMultiAssetPortfolio(toolContext);
      assertEquityPortfolio(toolContext);
      assertMultiCurrencySwapPortfolio(toolContext);
      
    } finally {
      if (toolContext != null) {
        toolContext.close();
      }
    }
    DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
  }

  private void assertMultiAssetPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleMultiAssetPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertEquityPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleEquityPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertMultiCurrencySwapPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME);
  }

  private void assertPortfolio(PortfolioMaster portfolioMaster, String portfolioName) {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(portfolioName);
    PortfolioSearchResult portfolioSearchResult = portfolioMaster.search(request);
    assertNotNull(portfolioSearchResult);
    assertEquals(1, portfolioSearchResult.getDocuments().size());
  }

  private ToolContext getToolContext() {
    return ToolContextUtils.getToolContext(CONFIG_RESOURCE_LOCATION, ToolContext.class);
  }
}
