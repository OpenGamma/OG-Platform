/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.examples.DBTestUtils;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleMixedPortfolioLoader;
import com.opengamma.examples.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleSwapPortfolioLoader;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * Test example database population
 */
public class ExampleDatabasePopulaterTest {

  private static final String CONFIG_RESOURCE_LOCATION = "classpath:toolcontext/toolcontext-example.properties";
  
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
      
      new ExampleDatabasePopulater().run(AbstractExampleTool.TOOLCONTEXT_EXAMPLE_PROPERTIES);
      
      ToolContext toolContext = getToolContext();
      try {
        assertMixedPortfolio(toolContext);
        assertEquityPortfolio(toolContext);
        assertSwapPortfolio(toolContext);
        assertMultiCurrencySwapPortfolio(toolContext);
        
      } finally {
        if (toolContext != null) {
          toolContext.close();
        }
      }
      DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
    }
  }

  private void assertMultiCurrencySwapPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertSwapPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleSwapPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertEquityPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleEquityPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertMixedPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleMixedPortfolioLoader.PORTFOLIO_NAME);
  }

  private void assertPortfolio(PortfolioMaster portfolioMaster, String portfolioName) {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(portfolioName);
    PortfolioSearchResult portfolioSearchResult = portfolioMaster.search(request);
    assertNotNull(portfolioSearchResult);
    assertEquals(1, portfolioSearchResult.getDocuments().size());
  }

  private ToolContext getToolContext() {
    return ToolContextUtils.getToolContext(CONFIG_RESOURCE_LOCATION);
  }
}
