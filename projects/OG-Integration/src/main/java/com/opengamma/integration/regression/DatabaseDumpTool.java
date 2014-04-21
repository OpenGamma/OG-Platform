/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;

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
    new DatabaseDumpTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    String dataDir = getCommandLine().getOptionValue(DATA_DIRECTORY);
    SubdirsRegressionIO io = new SubdirsRegressionIO(new File(dataDir), new FudgeXMLFormat(), true);
    DatabaseDump databaseDump = new DatabaseDump(io,
                                                 getToolContext().getSecurityMaster(),
                                                 getToolContext().getPositionMaster(),
                                                 getToolContext().getPortfolioMaster(),
                                                 getToolContext().getConfigMaster(),
                                                 getToolContext().getHistoricalTimeSeriesMaster(),
                                                 getToolContext().getHolidayMaster(),
                                                 getToolContext().getExchangeMaster(),
                                                 getToolContext().getMarketDataSnapshotMaster(),
                                                 getToolContext().getLegalEntityMaster(),
                                                 getToolContext().getConventionMaster(),
                                                 MasterQueryManager.queryAll());
    io.beginWrite();
    try {
      databaseDump.dumpDatabase();
    } finally {
      io.endWrite();
    }
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
