/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractComponentTool;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/** The entry point for running OpenGamma batches. */
@Scriptable
public class MarketDataSnapshotExportTool extends AbstractComponentTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotExportTool.class);

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Snapshot uid option flag */
  private static final String SNAPSHOT_UID_OPTION = "uid";

  private static final List<String> DEFAULT_PREFERRED_CLASSIFIERS = Arrays.asList("central",
                                                                                  "main",
                                                                                  "default",
                                                                                  "shared",
                                                                                  "combined");

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool. No arguments are needed.
   *
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    final boolean success = new MarketDataSnapshotExportTool().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

  @Override
  protected void doRun() throws Exception {
    final MarketDataSnapshotMaster marketDataSnapshotMaster = getRemoteComponentFactory().getMarketDataSnapshotMaster(
        DEFAULT_PREFERRED_CLASSIFIERS);
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }

    StructuredMarketDataSnapshot snapshot = marketDataSnapshotMaster.get(UniqueId.of("bla", "bla")).getSnapshot();

    snapshot.getBasisViewName();
    snapshot.getCurves();
    snapshot.getGlobalValues();
    snapshot.getName();
    snapshot.getVolatilitySurfaces();
    snapshot.getYieldCurves();


  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions() {
    final Options options = super.createOptions();
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
