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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.snapshot.copier.SimpleSnapshotCopier;
import com.opengamma.integration.copier.snapshot.copier.SnapshotCopier;
import com.opengamma.integration.copier.snapshot.reader.MasterSnapshotReader;
import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.CsvSnapshotWriter;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;
import com.opengamma.integration.copier.snapshot.writer.XlsSnapshotWriter;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/** The entry point for running OpenGamma batches. */
@Scriptable
public class MarketDataSnapshotExportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotExportTool.class);

  /** File name option flag */
  private static final String FILE_NAME_OPTION = "f";
  /** Snapshot uid option flag */
  private static final String SNAPSHOT_UID_OPTION = "uid";
  /** Snapshot name option flag */
  private static final String SNAPSHOT_NAME_OPTION = "n";
  /** Snapshot version date option flag */
  private static final String SNAPSHOT_DATE_OPTION = "d";  

  private static ToolContext s_context;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new MarketDataSnapshotExportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    s_context = getToolContext();
    SnapshotUtils snapshotUtils = SnapshotUtils.of(s_context.getMarketDataSnapshotMaster());
    if (!MarketDataSnapshotToolUtils.handleQueryOptions(snapshotUtils, getCommandLine())) {
      SnapshotReader snapshotReader = constructSnapshotReader(UniqueId.parse(getCommandLine().getOptionValue(
          SNAPSHOT_UID_OPTION)));
      SnapshotWriter snapshotWriter = constructSnapshotWriter(getCommandLine().getOptionValue(FILE_NAME_OPTION));
      SnapshotCopier snapshotCopier = new SimpleSnapshotCopier();
  
      snapshotCopier.copy(snapshotReader, snapshotWriter);
  
      // close the reader and writer
      snapshotReader.close();
      snapshotWriter.close();
    }
    System.exit(0);
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

    if (SheetFormat.of(filename) == SheetFormat.CSV) {
      return new CsvSnapshotWriter(filename);
    } else if (SheetFormat.of(filename) == SheetFormat.XLS) {
      return new XlsSnapshotWriter(filename);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }

  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createSnapshotUidOption());
    options.addOption(createFilenameOption());
    options.addOption(createSnapshotNameOption());
    options.addOption(createSnapshotDateOption());
    options.addOption(MarketDataSnapshotToolUtils.createSnapshotListOption());
    options.addOption(MarketDataSnapshotToolUtils.createSnapshotQueryOption());
    options.addOption(MarketDataSnapshotToolUtils.createSnapshotVersionListOption());
    return options;
  }

  private static Option createFilenameOption() {
    final Option option = new Option(FILE_NAME_OPTION, "filename", true, "The path to the file to create and export to");
    //option.setRequired(true);
    option.setArgName("file path/name");
    return option;
  }

  private static Option createSnapshotUidOption() {
    final Option option = new Option(SNAPSHOT_UID_OPTION, "snapshotUid", true, "The snapshot unique identifier to export");
    option.setArgName("snapshot uid");
    return option;
  }

  private static Option createSnapshotNameOption() {
    final Option option = new Option(SNAPSHOT_NAME_OPTION, "snapshot-name", true, "The snapshot name to export");
    option.setArgName("snapshot name");
    return option;
  }
   
  private static Option createSnapshotDateOption() {
    final Option option = new Option(SNAPSHOT_DATE_OPTION, "snapshot-date", true, "Specify a version date for a named snapshot");
    option.setArgName("snapshot name");
    return option;    
  }
}
