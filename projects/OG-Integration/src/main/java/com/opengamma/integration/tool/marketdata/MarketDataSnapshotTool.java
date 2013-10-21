/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static java.lang.String.format;
import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.financial.tool.marketdata.MarketDataSnapshotSaver;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.scripts.Scriptable;

/**
 * The entry point for running OpenGamma batches.
 */
@Scriptable
public class MarketDataSnapshotTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotTool.class);

  /** View name command line option. */
  private static final String VIEW_NAME_OPTION = "v";
  /** Existing view process unique identifier option. */
  private static final String VIEW_PROCESS_ID_OPTION = "p";
  /** Valuation time command line option. */
  private static final String VALUATION_TIME_OPTION = "t";
  /** Take data from historical timeseries */
  private static final String HISTORICAL_OPTION = "historical";
  /** Time format: yyyyMMdd */
  private static final DateTimeFormatter VALUATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final List<String> DEFAULT_PREFERRED_CLASSIFIERS = Arrays.asList("central", "main", "default", "shared", "combined");

  private static ToolContext s_context;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    final boolean success = new MarketDataSnapshotTool().initAndRun(args, ToolContext.class);
    System.exit(success ? 0 : 1);
  }

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
    final MarketDataSnapshotter marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter();
    final MarketDataSnapshotSaver snapshotSaver = MarketDataSnapshotSaver.of(marketDataSnapshotter, viewProcessor, s_context.getConfigMaster(), marketDataSnapshotMaster);

    if (getCommandLine().hasOption(VIEW_PROCESS_ID_OPTION)) {
      final UniqueId viewProcessId = UniqueId.parse(getCommandLine().getOptionValue(VIEW_PROCESS_ID_OPTION));
      s_logger.info("Creating snapshot from existing view process " + viewProcessId);
      try {
        snapshotSaver.createSnapshot(null, viewProcessId);
      } catch (Exception e) {
        endWithError(e.getMessage());
      }
    } else {
      final String viewDefinitionName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);
      final String valuationTimeArg = getCommandLine().getOptionValue(VALUATION_TIME_OPTION);
      Instant valuationInstant;
      if (!StringUtils.isBlank(valuationTimeArg)) {
        final LocalTime valuationTime = LocalTime.parse(valuationTimeArg, VALUATION_TIME_FORMATTER);
        valuationInstant = ZonedDateTime.now().with(valuationTime.truncatedTo(SECONDS)).toInstant();
      } else {
        valuationInstant = Instant.now();
      }
      final boolean historicalInput = getCommandLine().hasOption(HISTORICAL_OPTION);
  
      s_logger.info("Creating snapshot for view definition " + viewDefinitionName);
      final MarketDataSpecification marketDataSpecification = historicalInput ? new LatestHistoricalMarketDataSpecification() : MarketData.live();
      try {
        String snapshotName = viewDefinitionName + "/" + valuationInstant;
        snapshotSaver.createSnapshot(snapshotName, viewDefinitionName, valuationInstant, Collections.singletonList(marketDataSpecification));
      } catch (Exception e) {
        endWithError(e.getMessage());
      }
    }
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
    options.addOption(createValuationTimeOption());
    options.addOption(createHistoricalOption());
    return options;
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
  
  private static Option createViewProcessIdOption() {
    final Option option = new Option(VIEW_PROCESS_ID_OPTION, "viewProcessId", true, "the unique identifier of an existing view process");
    option.setArgName("unique identifier");
    return option;
  }

  private static Option createValuationTimeOption() {
    final Option option = new Option(VALUATION_TIME_OPTION, "valuationTime", true, "the valuation time, HH:mm[:ss] (defaults to now)");
    option.setArgName("valuation time");
    return option;
  }

  private static Option createHistoricalOption() {
    return new Option(null, HISTORICAL_OPTION, false, "if true use data from hts else use live data");
  }

}
