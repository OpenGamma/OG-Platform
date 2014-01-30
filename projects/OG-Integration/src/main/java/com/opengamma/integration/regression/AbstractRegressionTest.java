/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
public abstract class AbstractRegressionTest {
  
  
  private static final String LATEST_BUILD_VERSION = "Latest build";
  
  private static final String FILE_WRITE_MODE_PROPERTY = "Regression.writeReportToFile";
  private static final String CONSOLE_WRITE_MODE_PROPERTY = "Regression.writeReportToConsole";

  private static final double s_defaultAcceptableDelta = 0.0000001;
  
  private RegressionTestToolContextManager _contextManager;
  private GoldenCopyPersistenceHelper _goldenCopyPersistenceHelper;
  
  private static final String s_defaultRegressionToolContext = "classpath:regression/regression-toolcontext.properties";
  
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
  
  public AbstractRegressionTest(File regressionRoot, String regressionPropertiesFile) {
    this(regressionRoot, s_defaultRegressionToolContext, regressionPropertiesFile);
  }
  
  
  @BeforeTest(groups = TestGroup.UNIT)
  public void initContext() {
    _contextManager.init();
  }
  
  @AfterTest(groups = TestGroup.UNIT)
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

    CalculationResults thisRun = viewRunner.run(LATEST_BUILD_VERSION, viewName, snapshotName, original.getValuationTime());
    
    CalculationDifference differences = evaluateDifferences(original, thisRun);
    
    String baseVersion = original.getCalculationResults().getVersion();
    
    RegressionTestResults testResults = new RegressionTestResults(baseVersion, LATEST_BUILD_VERSION, Collections.singleton(differences));
    
    if (isWriteReportToFile()) {
      writeReportToFile(testResults, viewName, snapshotName);
    }
    
    if (isWriteReportToConsole()) {
      writeReportToConsole(testResults);
    }
    
    assertTrue("Found results only in base", differences.getOnlyBase().isEmpty());
    assertTrue("Found results only in test", differences.getOnlyTest().isEmpty());
    assertTrue("Found differing results", differences.getDifferent().isEmpty());
    assertTrue("Found differing result properties", differences.getDifferentProperties().isEmpty());
    
  }

  
  private void writeReportToConsole(RegressionTestResults testResults) {
    OutputStreamWriter writer = new OutputStreamWriter(System.out);
    ReportGenerator.generateReport(testResults, ReportGenerator.Format.TEXT, writer);
  }
  

  private void writeReportToFile(RegressionTestResults testResults, String viewName, String snapshotName) {
    File file;
    FileWriter fileWriter;
    try {
      file = getDifferencesReportFile(viewName, snapshotName);
      fileWriter = new FileWriter(getDifferencesReportFile(viewName, snapshotName));
    } catch (IOException ex) {
      throw Throwables.propagate(ex);
    }
    ReportGenerator.generateReport(testResults, ReportGenerator.Format.TEXT, fileWriter);
    
    System.out.println("Differences report written to " + file.getAbsolutePath());
    
  }

  
  private CalculationDifference evaluateDifferences(GoldenCopy original, CalculationResults thisRun) {
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
    
    
    return result;
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

  
  /**
   * If true, a report with the differences will be written to
   * the location specified by {@link #getDifferencesReportFile(String, String)}.
   * @return True if specified as a system property by {@value #FILE_WRITE_MODE_PROPERTY},
   * else false.
   */
  protected boolean isWriteReportToFile() {
    return Boolean.getBoolean(FILE_WRITE_MODE_PROPERTY);
  }
  
  /**
   * A {@link File} giving the location to write the difference
   * report to.
   * @param viewName the view which ran
   * @param snapshotName the snapshot which was run against
   * @return the location to write the differences report to
   * @throws IOException 
   */
  protected File getDifferencesReportFile(String viewName, String snapshotName) throws IOException {
    String tempDir = System.getProperty("java.io.tmpdir");  
    File regressionDir = new File(tempDir, "regression-report");
    if (!regressionDir.exists()) {
      Preconditions.checkState(regressionDir.mkdirs(), "Unable to mkdir " + regressionDir.getPath());
    }
    return new File(regressionDir, viewName + "-" + snapshotName + ".txt");
  }
  
  /**
   * If true, a report will be written to console.
   * @return If defined, will return the value given as a system property by 
   * {@value #CONSOLE_WRITE_MODE_PROPERTY}. Else defaults to true.
   */
  protected boolean isWriteReportToConsole() {
    if (System.getProperty(CONSOLE_WRITE_MODE_PROPERTY) != null) {
      return Boolean.getBoolean(CONSOLE_WRITE_MODE_PROPERTY);
    }
    return true;
  }
  
}
