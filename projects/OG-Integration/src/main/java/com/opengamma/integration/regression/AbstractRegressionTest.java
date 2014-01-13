/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.BeforeTest;

import com.opengamma.financial.tool.ToolContext;

/**
 * 
 */
public abstract class AbstractRegressionTest {
  
  
  private static final double s_defaultAcceptableDelta = 0.0000001;
  
  private RegressionTestToolContextManager _contextManager;
  private GoldenCopyPersistenceHelper _goldenCopyPersistenceHelper;
  
  
  /**
   * Initializes the test. A valid tool context properties file is required - this cut down context is used to
   * initialize the database. Secondly, a "regressionPropertiesFile" is also required. This is used to execute
   * the views and must therefore contain a full engine configuration. Typically this can be created by using
   * the fullstack.properties/ini as a starting point, removing the enterprise services such as web and amq
   * exposure.
   * 
   * @param regressionRoot the root for this set of tests (i.e. the directory
   * containing the dbdump zip and golden_copy folder
   * @param toolContextPropertiesFile path to a valid tool context properties file
   * @param regressionPropertiesFile path to a valid regression properties file
   */
  public AbstractRegressionTest(File regressionRoot, String toolContextPropertiesFile, String regressionPropertiesFile) {
    _contextManager = new RegressionTestToolContextManager(new File(regressionRoot, GoldenCopyDumpCreator.DB_DUMP_ZIP), toolContextPropertiesFile, regressionPropertiesFile);
    _goldenCopyPersistenceHelper = new GoldenCopyPersistenceHelper(regressionRoot);
  }
  
  
  @BeforeTest(alwaysRun = true)
  public void initContext() {
    _contextManager.init();
  }
  
  @BeforeTest(alwaysRun = true)
  public void closeContext() {
    _contextManager.close();
  }
  
  
  
  /**
   * Executes viewName against snapshotName in the running engine context.
   * @param viewName name of the view to run
   * @param snapshotName name of the snapshot to run
   */
  protected final void runTestForView(String viewName, String snapshotName) {
    
    ToolContext toolContext = _contextManager.getToolContext();
    GoldenCopy original = _goldenCopyPersistenceHelper.load(viewName, snapshotName);
    
    ViewRunner viewRunner = new ViewRunner(toolContext.getConfigMaster(),
        toolContext.getViewProcessor(),
        toolContext.getPositionSource(),
        toolContext.getSecuritySource(),
        toolContext.getMarketDataSnapshotMaster());

    CalculationResults thisRun = viewRunner.run("Test", viewName, snapshotName, original.getValuationTime());
    
    evaluateDifferences(original, thisRun);
    
  }


  private void evaluateDifferences(GoldenCopy original, CalculationResults thisRun) {
    CalculationDifference result = CalculationDifference.generatorWithDelta(getAcceptableDelta()).
                                                        compareValueProperties(compareValueProperties()).
                                                        between(original.getCalculationResults(), thisRun);
    
    System.out.println("Total results in golden copy: " + original.getCalculationResults().getValues().size());
    System.out.println("Total results in test run: " + thisRun.getValues().size());
    
    System.out.println("Equal: " + result.getEqualResultCount());
    System.out.println("Different: " + result.getDifferent().size());
    if (compareValueProperties()) {
      System.out.println("Different properties: " + result.getDifferentProperties().size());
    }
    System.out.println("Only base: " + result.getOnlyBase().size());
    System.out.println("Only test: " + result.getOnlyTest().size());
    
    assertTrue("Found results only in base", result.getOnlyBase().isEmpty());
    assertTrue("Found results only in test", result.getOnlyTest().isEmpty());
    assertTrue("Found differing results", result.getDifferent().isEmpty());
    assertTrue("Found differing result properties", result.getDifferentProperties().isEmpty());
  }
  
  
  //---------------------------------------------------------------------------------------------------------
  //default test settings for overriding
  //---------------------------------------------------------------------------------------------------------
  
  /**
   * The smallest acceptable delta when comparing outputs.
   * @return a double
   */
  protected double getAcceptableDelta() {
    return s_defaultAcceptableDelta;
  }
  
  /**
   * Whether to compare value properties. Not normally significant if numbers match ok. 
   * False unless overridden.
   * @return a boolean
   */
  protected boolean compareValueProperties() {
    return false;
  }
  
}
