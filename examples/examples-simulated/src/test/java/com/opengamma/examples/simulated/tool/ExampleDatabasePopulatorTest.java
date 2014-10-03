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

import com.opengamma.examples.simulated.DBTestUtils;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test example database population
 */
@Test(groups = TestGroup.INTEGRATION)
public class ExampleDatabasePopulatorTest {

  private static final String CONFIG_RESOURCE_LOCATION = "classpath:/toolcontext/toolcontext-examplessimulated.properties";

  @Test
  public void testPortfolioAndDataLoaded() throws Exception {
    // setup thread-local
    ThreadLocalServiceContext.init(ServiceContext.of(VersionCorrectionProvider.class, new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    }));
    
    // override to run test with ToolContext used by populator
    class PopulatorTest extends ExampleDatabasePopulator {
      @Override
      protected void doRun() {
        // setup database
        super.doRun();
        
        // test
        ToolContext toolContext = getToolContext();
        assertMultiCurrencySwaptionPortfolio(toolContext);
        assertEquityPortfolio(toolContext);
        assertMultiCurrencySwapPortfolio(toolContext);
      }
    }
    
    // run test
    try {
      DBTestUtils.createTestHsqlDB(CONFIG_RESOURCE_LOCATION);
      if (!(new PopulatorTest().run(CONFIG_RESOURCE_LOCATION, ToolContext.class))) {
        fail();
      }
    } finally {
      DBTestUtils.cleanUp(CONFIG_RESOURCE_LOCATION);
    }
  }

  private void assertMultiCurrencySwaptionPortfolio(ToolContext toolContext) {
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    assertPortfolio(portfolioMaster, ExampleDatabasePopulator.MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME);
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

}
