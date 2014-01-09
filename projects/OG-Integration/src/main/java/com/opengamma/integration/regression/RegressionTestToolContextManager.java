/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.PlatformConfigUtils;

/**
 * 
 */
public class RegressionTestToolContextManager {

  private ToolContext _toolContext;
  
  private static String s_toolContext = "classpath:regression/regression-toolcontext.properties";
  private static String s_regressionPropertiesFile = "classpath:regression/regression-examplessimulated.properties";
  
  public void init() {
    
    PlatformConfigUtils.configureSystemProperties();
    System.out.println("Initializing DB");
    initialiseDB();
    System.out.println("Initialized DB");
    
    //start toolcontext
    System.out.println("Starting full context");
    _toolContext = ToolContextUtils.getToolContext(s_regressionPropertiesFile, ToolContext.class);
    System.out.println("Full context started");
    
  }


  /**
   * Create a new DB, schema, and populate tables.
   */
  private void initialiseDB() {
    System.out.println("Creating empty DB...");
    EmptyDatabaseCreator.createForConfig(s_toolContext);
    
    System.out.println("Creating tool context for DB...");
    ToolContext toolContext = ToolContextUtils.getToolContext(s_toolContext, ToolContext.class);
    

    DatabaseRestore restore = new DatabaseRestore(
          "regression/dbdump", //cwd relative
          toolContext.getSecurityMaster(),
          toolContext.getPositionMaster(),
          toolContext.getPortfolioMaster(),
          toolContext.getConfigMaster(),
          toolContext.getHistoricalTimeSeriesMaster(),
          toolContext.getHolidayMaster(),
          toolContext.getExchangeMaster(),
          toolContext.getMarketDataSnapshotMaster(),
          toolContext.getOrganizationMaster()
    );
    
    System.out.println("Initializing DB state...");
    restore.restoreDatabase();
    
    toolContext.close();
  }
  
  //TODO teardown
  
  /**
   * @return the tool context managed by this instance
   */
  public ToolContext getToolContext() {
    return _toolContext;
  }
  
  
}
