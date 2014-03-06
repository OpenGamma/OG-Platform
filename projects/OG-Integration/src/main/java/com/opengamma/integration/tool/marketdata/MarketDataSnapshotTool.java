/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static java.lang.String.format;
import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter.Mode;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.financial.tool.marketdata.MarketDataSnapshotSaver;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.cli.MarketDataSourceCli;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/**
 * The entry point for running OpenGamma batches.
 */
@Scriptable
public class MarketDataSnapshotTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotTool.class);

  /** Snapshot name command line option. */
  private static final String SNAPSHOT_NAME_OPTION = "s";
  /** View name command line option. */
  private static final String VIEW_NAME_OPTION = "v";
  /** Existing view process unique identifier option. */
  private static final String VIEW_PROCESS_ID_OPTION = "p";
  /** Snapshotter timeout option when awaiting market data. */
  private static final String TIMEOUT_OPTION = "o";
  /** Valuation time command line option. */
  private static final String VALUATION_TIME_OPTION = "t";
  /** Take data from historical timeseries */
  private static final String HISTORICAL_OPTION = "historical";
  /** Take an unstructured only snapshot */
  private static final String UNSTRUCTURED_OPTION = "u";
  /** Time format: yyyyMMdd */
  private static final DateTimeFormatter VALUATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static ToolContext s_context;
  
  private static MarketDataSourceCli s_mktDataSourceCli = new MarketDataSourceCli();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new MarketDataSnapshotTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    s_context = getToolContext();
    
    final RemoteViewProcessor viewProcessor = (RemoteViewProcessor) s_context.getViewProcessor();
    if (viewProcessor == null) {
      s_logger.warn("No view processors found at {}", s_context);
      return;
    }
    final MarketDataSnapshotMaster marketDataSnapshotMaster = s_context.getMarketDataSnapshotMaster();
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", s_context);
      return;
    }
    final MarketDataSnapshotter marketDataSnapshotter;
    if (getCommandLine().hasOption(UNSTRUCTURED_OPTION)) {
      marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter(Mode.UNSTRUCTURED);
    } else {
      marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter(Mode.STRUCTURED);
    }
    Long marketDataTimeoutSeconds = getCommandLine().hasOption(TIMEOUT_OPTION) ? Long.parseLong(getCommandLine().getOptionValue(TIMEOUT_OPTION)) : null;
    Long marketDataTimeoutMillis = marketDataTimeoutSeconds != null ? TimeUnit.SECONDS.toMillis(marketDataTimeoutSeconds) : null;
    final MarketDataSnapshotSaver snapshotSaver = MarketDataSnapshotSaver.of(marketDataSnapshotter, viewProcessor, s_context.getConfigMaster(), marketDataSnapshotMaster, marketDataTimeoutMillis);

    if (getCommandLine().hasOption(VIEW_PROCESS_ID_OPTION)) {
      final UniqueId viewProcessId = UniqueId.parse(getCommandLine().getOptionValue(VIEW_PROCESS_ID_OPTION));
      s_logger.info("Creating snapshot from existing view process " + viewProcessId);
      try {
        snapshotSaver.createSnapshot(null, viewProcessId);
      } catch (Exception e) {
        endWithError(e.getMessage());
      }
    } else {
      final String viewDefinitionName = StringUtils.trimToNull(getCommandLine().getOptionValue(VIEW_NAME_OPTION));
      if (viewDefinitionName == null) {
        s_logger.warn("Given view definition name is blank");
        return;
      }
      final String valuationTimeArg = StringUtils.trimToNull(getCommandLine().getOptionValue(VALUATION_TIME_OPTION));
      Instant valuationInstant;
      if (valuationTimeArg != null) {
        final LocalTime valuationTime = LocalTime.parse(valuationTimeArg, VALUATION_TIME_FORMATTER);
        valuationInstant = ZonedDateTime.now().with(valuationTime.truncatedTo(SECONDS)).toInstant();
      } else {
        valuationInstant = Instant.now();
      }
      
      List<MarketDataSpecification> marketDataSpecs = new ArrayList<>();
      if (getCommandLine().hasOption(HISTORICAL_OPTION)) {
        marketDataSpecs.add(new LatestHistoricalMarketDataSpecification());
      } else {
        marketDataSpecs.addAll(getMarketDataSpecs());
      }
      
      s_logger.info("Creating snapshot for view definition " + viewDefinitionName);
      try {
        String snapshotName = StringUtils.trimToNull(getCommandLine().getOptionValue(SNAPSHOT_NAME_OPTION));
        if (snapshotName == null) {
          s_logger.warn("Given snapshot name is blank, using {}/{}", viewDefinitionName, valuationInstant);
          snapshotName = viewDefinitionName + "/" + valuationInstant;
        }
        snapshotSaver.createSnapshot(snapshotName, viewDefinitionName, valuationInstant, marketDataSpecs);
      } catch (Exception e) {
        endWithError(e.getMessage());
      }
    }
  }

  private List<MarketDataSpecification> getMarketDataSpecs() {
    return s_mktDataSourceCli.getMarketDataSpecs(getCommandLine(), s_context.getMarketDataSnapshotMaster());
  }

  private void endWithError(String message, Object... messageArgs) {
    String formattedMessage = format(message, messageArgs);
    System.err.println(formattedMessage);
    s_logger.error(formattedMessage);
    System.exit(1);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOptionGroup(createViewOptionGroup());
    options.addOption(createSnapshotNameOption());
    options.addOption(createValuationTimeOption());
    options.addOption(createTimeoutOption());
    options.addOptionGroup(createMarketDataSourceOptionGroup());
    options.addOption(createUnstructuredSnapshot());
    return options;
  }
  
  private OptionGroup createMarketDataSourceOptionGroup() {
    final OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(createHistoricalOption());
    optionGroup.addOption(s_mktDataSourceCli.getOption());
    optionGroup.setRequired(true);
    return optionGroup;
  }

  private static OptionGroup createViewOptionGroup() {
    final OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(createViewNameOption());
    optionGroup.addOption(createViewProcessIdOption());
    optionGroup.setRequired(true);
    return optionGroup;
  }

  private static Option createViewNameOption() {
    final Option option = new Option(VIEW_NAME_OPTION, "viewName", true, "the view definition name");
    option.setArgName("view name");
    return option;
  }
  
  private static Option createSnapshotNameOption() {
    final Option option = new Option(SNAPSHOT_NAME_OPTION, "snapshotName", true, "the name to use when persisting the snapshot. (defaults to '<view name>/<valuation time>' )");
    option.setArgName("snapshot name");
    return option;
  }
  
  private static Option createViewProcessIdOption() {
    final Option option = new Option(VIEW_PROCESS_ID_OPTION, "viewProcessId", true, "the unique identifier of an existing view process e.g ViewProcess~1234");
    option.setArgName("unique identifier");
    return option;
  }

  private static Option createValuationTimeOption() {
    final Option option = new Option(VALUATION_TIME_OPTION, "valuationTime", true, "the valuation time, HH:mm[:ss] (defaults to now)");
    option.setArgName("valuation time");
    return option;
  }
  
  private static Option createTimeoutOption() {
    final Option option = new Option(TIMEOUT_OPTION, "timeout", true, "the timeout, in seconds, for market data to populate the snapshot (defaults to the engine default)");
    option.setArgName("seconds");
    return option;
  }

  private static Option createHistoricalOption() {
    return new Option("hts", HISTORICAL_OPTION, false, "if true use data from latest hts");
  }
  
  private static Option createUnstructuredSnapshot() {
    return new Option(UNSTRUCTURED_OPTION, "unstructured", false, "if set, do not capture structures and include data for those in unstructured section");
  }

}
