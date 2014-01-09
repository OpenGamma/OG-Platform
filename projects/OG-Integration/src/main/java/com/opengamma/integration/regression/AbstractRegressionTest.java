/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.opengamma.financial.tool.ToolContext;

/**
 * 
 */
public abstract class AbstractRegressionTest {
  
  
  private RegressionTestToolContextManager _contextManager;
  
  
  /**
   * @param dumpDir dump source directory 
   */
  public AbstractRegressionTest(File dumpDir) {
    _contextManager = new RegressionTestToolContextManager(dumpDir);
  }

  @BeforeTest
  public void initContext() {
    _contextManager.init();
  }
  
  @AfterTest
  public void closeContext() {
    _contextManager.close();
  }
  
  
  protected void runTestForView(String viewName, String snapshotName) {
    
    ToolContext toolContext = _contextManager.getToolContext();
    GoldenCopy original = new GoldenCopyPersistenceHelper().load(viewName, snapshotName);
    
    ViewRunner viewRunner = new ViewRunner(toolContext.getConfigMaster(),
        toolContext.getViewProcessor(),
        toolContext.getPositionSource(),
        toolContext.getSecuritySource(),
        toolContext.getMarketDataSnapshotMaster());

    CalculationResults thisRun = viewRunner.run("Test", viewName, snapshotName, original.getValuationTime());
    
    CalculationDifference result = CalculationDifference.between(original.getCalculationResults(), thisRun, 0.0000001);
    
    System.out.println("Equal: " + result.getEqualResultCount());
    System.out.println("Different: " + result.getDifferent().size());
    System.out.println("Only base: " + result.getOnlyBase().size());
    System.out.println("Only test: " + result.getOnlyTest().size());
    
    assertTrue("Found results only in base", result.getOnlyBase().isEmpty());
    assertTrue("Found results only in base", result.getOnlyTest().isEmpty());
    assertTrue("Found results only in base", result.getDifferent().isEmpty());
    
  }
  
  
  
}
