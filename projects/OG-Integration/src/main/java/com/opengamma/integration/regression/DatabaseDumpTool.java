/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class DatabaseDumpTool extends AbstractTool<ToolContext> {

  private static final String DATA_DIRECTORY = "d";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new DatabaseDumpTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    String dataDir = getCommandLine().getOptionValue(DATA_DIRECTORY);
    DatabaseDump databaseDump = new DatabaseDump(dataDir,
                                                 getToolContext().getSecurityMaster(),
                                                 getToolContext().getPositionMaster(),
                                                 getToolContext().getPortfolioMaster(),
                                                 getToolContext().getConfigMaster(),
                                                 getToolContext().getHistoricalTimeSeriesMaster(),
                                                 getToolContext().getHolidayMaster(),
                                                 getToolContext().getExchangeMaster(),
                                                 getToolContext().getMarketDataSnapshotMaster(),
                                                 getToolContext().getOrganizationMaster(),
                                                 getToolContext().getConventionMaster());
    databaseDump.dumpDatabase();
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);

    Option dataDirOption = new Option(DATA_DIRECTORY, true, "Directory where the database dump files should be saved");
    dataDirOption.setRequired(true);
    options.addOption(dataDirOption);

    return options;
  }
}
