/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import com.opengamma.financial.tool.ToolContext;

/**
 * 
 */
public abstract class AbstractRegressionTest {
  
  
  private static RegressionTestToolContextManager s_contextManager;
  
  static {
    s_contextManager = new RegressionTestToolContextManager();
    s_contextManager.init();
  }
  
  
  protected void runTestForView(String viewName, String snapshotName) {
    
    ToolContext toolContext = s_contextManager.getToolContext();
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
    
    
  }
  
  
  
}
