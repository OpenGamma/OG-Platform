/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.tool;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.examples.bloomberg.DBTestUtils;
import com.opengamma.examples.bloomberg.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.bloomberg.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.examples.bloomberg.tool.ExampleDatabasePopulator;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test example database population
 */
@Test(groups = TestGroup.INTEGRATION)
public class ExampleDatabasePopulatorTest {

  private static final String CONFIG_RESOURCE_LOCATION = "classpath:/toolcontext/toolcontext-examplesbloomberg.properties";

//  @BeforeMethod
//  public void setUp() throws IOException {
//    DBTestUtils.createHsqlDB(CONFIG_RESOURCE_LOCATION);
//  }
//  
//  @AfterMethod
//  public void runAfter() throws IOException {
//    DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
//  }
  
  @Test
  public void testPortfolioAndDataLoaded() throws Exception {
    for (int i = 0; i < 2; i++) {
      DBTestUtils.createHsqlDB(CONFIG_RESOURCE_LOCATION);
      
      new ExampleDatabasePopulator().run(CONFIG_RESOURCE_LOCATION, IntegrationToolContext.class);
      
      IntegrationToolContext toolContext = getToolContext();
      try {
        assertEquityPortfolio(toolContext);
        assertMultiCurrencySwapPortfolio(toolContext);
        
      } finally {
        if (toolContext != null) {
          toolContext.close();
        }
      }
      DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
    }
  }

  private void assertMultiCurrencySwapPortfolio(IntegrationToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertEquityPortfolio(IntegrationToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleEquityPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertPortfolio(PortfolioMaster portfolioMaster, String portfolioName) {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(portfolioName);
    PortfolioSearchResult portfolioSearchResult = portfolioMaster.search(request);
    assertNotNull(portfolioSearchResult);
    assertEquals(1, portfolioSearchResult.getDocuments().size());
  }

  private IntegrationToolContext getToolContext() {
    return (IntegrationToolContext) ToolContextUtils.getToolContext(CONFIG_RESOURCE_LOCATION, IntegrationToolContext.class);
  }
}
