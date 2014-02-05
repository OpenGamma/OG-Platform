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
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.snapshot.copier.SimpleSnapshotCopier;
import com.opengamma.integration.copier.snapshot.copier.SnapshotCopier;
import com.opengamma.integration.copier.snapshot.reader.CsvSnapshotReader;
import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.reader.XlsSnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.MasterSnapshotWriter;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/** The entry point for running OpenGamma batches. */
@Scriptable
public class MarketDataSnapshotImportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotImportTool.class);

  /** File name option flag */
  private static final String FILE_NAME_OPTION = "f";

  private static ToolContext s_context;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new MarketDataSnapshotImportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    s_context = getToolContext();

    SnapshotReader snapshotReader = constructSnapshotReader(getCommandLine().getOptionValue(FILE_NAME_OPTION));
    SnapshotWriter snapshotWriter = constructSnapshotWriter();
    SnapshotCopier snapshotCopier = new SimpleSnapshotCopier();

    snapshotCopier.copy(snapshotReader, snapshotWriter);

    // close the reader and writer
    snapshotReader.close();
    snapshotWriter.close();

  }

  private static SnapshotReader constructSnapshotReader(String filename) {
    if (SheetFormat.of(filename) == SheetFormat.CSV) {
      return new CsvSnapshotReader(filename);
    } else if (SheetFormat.of(filename) == SheetFormat.XLS) {
      return new XlsSnapshotReader(filename);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  private static SnapshotWriter constructSnapshotWriter() {
    MarketDataSnapshotMaster marketDataSnapshotMaster = s_context.getMarketDataSnapshotMaster();
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", s_context);

    }
    return new MasterSnapshotWriter(marketDataSnapshotMaster);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createFilenameOption());
    return options;
  }

  private static Option createFilenameOption() {
    final Option option = new Option(FILE_NAME_OPTION, "filename", true, "The path to the file to import");
    option.setRequired(true);
    return option;
  }


}
