/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
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
    final MarketDataSnapshotMaster marketDataSnapshotMaster = getToolContext().getMarketDataSnapshotMaster();
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", getToolContext());
      return;
    }

    StructuredMarketDataSnapshot snapshot;
    snapshot = marketDataSnapshotMaster.get(UniqueId.parse(getCommandLine().getOptionValue(SNAPSHOT_UID_OPTION))).getSnapshot();


    String name= snapshot.getName();
    String basisViewName  = snapshot.getBasisViewName();
    UnstructuredMarketDataSnapshot globalValues = snapshot.getGlobalValues();;
    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = snapshot.getYieldCurves();;
    Map<CurveKey, CurveSnapshot> curves = snapshot.getCurves();;
    Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurfaces = snapshot.getVolatilitySurfaces();;


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
