/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.impl.DataTrackingConfigMaster;
import com.opengamma.master.convention.impl.DataTrackingConventionMaster;
import com.opengamma.master.exchange.impl.DataTrackingExchangeMaster;
import com.opengamma.master.historicaltimeseries.impl.DataTrackingHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.marketdatasnapshot.impl.DataTrackingMarketDataSnapshotMaster;
import com.opengamma.master.organization.impl.DataTrackingOrganizationMaster;
import com.opengamma.master.portfolio.impl.DataTrackingPortfolioMaster;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.security.impl.DataTrackingSecurityMaster;

/**
 * 
 */
public class GoldenCopyCreationTool extends AbstractTool<ToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(GoldenCopyCreationTool.class);
  
  private static final ImmutableSet<CharSequence> UNSUPPORTED_CHAR_SEQUENCES = ImmutableSet.<CharSequence>of("/");
  
  /**
   * The version name to use against calculation results in the golden copy.
   */
  public static final String GOLDEN_COPY_VERSION_NAME = "Golden Copy";
  
  
  public static void main(String[] args) {
    try {
      new GoldenCopyCreationTool().initAndRun(args, ToolContext.class);
    } finally {
      System.exit(0);
    }
  }

  @Override
  protected void doRun() throws Exception {
    
    CommandLine commandLine = getCommandLine();
    
    String regressionDirectory = commandLine.getOptionValue("db-dump-output-dir");

    GoldenCopyCreator goldenCopyCreator = new GoldenCopyCreator(getToolContext());
    
    String[] viewSnapshotPairs = commandLine.getArgs();
    
    validateAsFilesystemNames(viewSnapshotPairs);
    
    Preconditions.checkArgument(viewSnapshotPairs.length % 2 == 0, "Should be an even number of view/snapshot pairs. Found %s", Arrays.toString(viewSnapshotPairs));
    
    for (int i = 0; i < viewSnapshotPairs.length; i += 2) {
      String viewName = viewSnapshotPairs[i];
      String snapshotName = viewSnapshotPairs[i + 1];
      s_logger.info("Executing {} against snapshot {}", viewName, snapshotName);
      GoldenCopy goldenCopy = goldenCopyCreator.run(viewName, snapshotName, GOLDEN_COPY_VERSION_NAME);
      s_logger.info("Persisting golden copy for {} against snapshot {}", viewName, snapshotName);
      new GoldenCopyPersistenceHelper(new File(regressionDirectory)).save(goldenCopy);
      s_logger.info("Persisted golden copy for {} against snapshot {}", viewName, snapshotName);
    }
    
    ToolContext tc = getToolContext();
    
    RegressionIO io = ZipFileRegressionIO.createWriter(new File(regressionDirectory, GoldenCopyDumpCreator.DB_DUMP_ZIP), new FudgeXMLFormat());
    
    
    GoldenCopyDumpCreator goldenCopyDumpCreator = new GoldenCopyDumpCreator(io, 
        (DataTrackingSecurityMaster) tc.getSecurityMaster(),
        (DataTrackingPositionMaster) tc.getPositionMaster(),
        (DataTrackingPortfolioMaster) tc.getPortfolioMaster(),
        (DataTrackingConfigMaster) tc.getConfigMaster(),
        (DataTrackingHistoricalTimeSeriesMaster) tc.getHistoricalTimeSeriesMaster(),
        (DataTrackingHolidayMaster) tc.getHolidayMaster(),
        (DataTrackingExchangeMaster) tc.getExchangeMaster(),
        (DataTrackingMarketDataSnapshotMaster) tc.getMarketDataSnapshotMaster(),
        (DataTrackingOrganizationMaster) tc.getOrganizationMaster(),
        (DataTrackingConventionMaster) tc.getConventionMaster());
    
    s_logger.info("Persisting db dump with tracked data");
    goldenCopyDumpCreator.execute();
    
  }

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createDbDumpOutputDirectory());
    return options;
  }


  @SuppressWarnings("static-access")
  private static Option createDbDumpOutputDirectory() {
    return OptionBuilder.isRequired(true)
        .hasArg(true)
        .withArgName("outputdir")
        .withDescription("Where to write the golden copy(ies) and the corresponding dump.")
        .withLongOpt("db-dump-output-dir")
        .create("o");
  }
  
  private void validateAsFilesystemNames(String[] names) {
    for (String name : names) {
      for (CharSequence unsupportedCharSequence : UNSUPPORTED_CHAR_SEQUENCES) {
        Preconditions.checkArgument(!name.contains(unsupportedCharSequence), "Unsupported char sequence '%s' found in string '%s'", unsupportedCharSequence, name);
      }
    }
  }
  
}
