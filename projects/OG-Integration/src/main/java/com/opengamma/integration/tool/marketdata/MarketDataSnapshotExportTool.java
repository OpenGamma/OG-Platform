/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.snapshot.copier.SimpleSnapshotCopier;
import com.opengamma.integration.copier.snapshot.copier.SnapshotCopier;
import com.opengamma.integration.copier.snapshot.reader.MasterSnapshotReader;
import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.MasterSnapshotWriter;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/** The entry point for running OpenGamma batches. */
@Scriptable
public class MarketDataSnapshotExportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotExportTool.class);

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Snapshot uid option flag */
  private static final String SNAPSHOT_UID_OPTION = "uid";

  private static ToolContext s_context;

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool. No arguments are needed.
   *
   * @param args the arguments, no null
   */
  public static void main(final String[] args) { // CSIGNORE
    final boolean success = new MarketDataSnapshotExportTool().initAndRun(args, ToolContext.class);
    System.exit(success ? 0 : 1);
  }

  @Override
  protected void doRun() throws Exception {
    s_context = getToolContext();

    SnapshotReader snapshotReader = constructSnapshotReader(UniqueId.parse(getCommandLine().getOptionValue(
        SNAPSHOT_UID_OPTION)));
    SnapshotWriter snapshotWriter = constructSnapshotWriter(getCommandLine().getOptionValue(FILE_NAME_OPT));
    SnapshotCopier snapshotCopier = new SimpleSnapshotCopier();

    snapshotCopier.copy(snapshotReader, snapshotWriter);

    // close the reader and writer
    snapshotReader.close();
    snapshotWriter.close();

  }

  private static SnapshotReader constructSnapshotReader(UniqueId uniqueId) {
    MarketDataSnapshotMaster marketDataSnapshotMaster = s_context.getMarketDataSnapshotMaster();
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", s_context);

    }
    return new MasterSnapshotReader(uniqueId, marketDataSnapshotMaster);
  }

  private static SnapshotWriter constructSnapshotWriter(String filename) {
    MarketDataSnapshotMaster marketDataSnapshotMaster = s_context.getMarketDataSnapshotMaster();
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", s_context);

    }
    return new MasterSnapshotWriter(filename, marketDataSnapshotMaster);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createSnapshotUidOption());
    options.addOption(createFilenameOption());
    return options;
  }

  private static Option createFilenameOption() {
    final Option filenameOption = new Option(FILE_NAME_OPT,
                                             "filename",
                                             true,
                                             "The path to the file to create and export to (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    return filenameOption;
  }

  private static Option createSnapshotUidOption() {
    final Option option = new Option(SNAPSHOT_UID_OPTION, "snapshotUid", true, "the snapshot unique identifier");
    option.setArgName("snapshot uid");
    option.setRequired(true);
    return option;
  }

}
