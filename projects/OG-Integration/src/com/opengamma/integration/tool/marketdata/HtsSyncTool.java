/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static com.google.common.collect.Lists.newArrayList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.time.Instant;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.annotations.Sets;

import com.beust.jcommander.internal.Lists;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleMetadata;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.integration.tool.AbstractDualComponentTool;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * The entry point for running OpenGamma batches. 
 */
public class HtsSyncTool extends AbstractDualComponentTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HtsSyncTool.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new HtsSyncTool().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

  @Override
  protected void doRun() throws Exception {
    Map<String, HistoricalTimeSeriesMaster> srcHtsMasters = getSourceRemoteComponentFactory().getHistoricalTimeSeriesMasters();
    Map<String, HistoricalTimeSeriesMaster> destHtsMasters = getDestinationRemoteComponentFactory().getHistoricalTimeSeriesMasters();
    boolean fast = getCommandLine().hasOption("fast");
    boolean hardSync = getCommandLine().hasOption("hard-sync");
    boolean verbose = getCommandLine().hasOption("verbose");
    boolean noAdditions = getCommandLine().hasOption("no-additions");
    if (hardSync && noAdditions) {
      System.err.println("Cannot specify both hard-sync and no-additions options");
      return;
    }
    Set<String> filteredClassifiers = filterClassifiers(srcHtsMasters.keySet(), destHtsMasters.keySet());
    for (String classifier : filteredClassifiers) {
      HistoricalTimeSeriesMaster srcHtsMaster = srcHtsMasters.get(classifier);
      HistoricalTimeSeriesMaster destHtsMaster = destHtsMasters.get(classifier);
      HistoricalTimeSeriesMasterCopier copier = new HistoricalTimeSeriesMasterCopier(srcHtsMaster, destHtsMaster);
      copier.copy(fast, hardSync, verbose, noAdditions);
    }
  }
  
  private Set<String> filterClassifiers(Set<String> srcMasterClassifiers, Set<String> destMasterClassifiers) {
    Set<String> commonComponentNames = Sets.newLinkedHashSet();
    commonComponentNames.addAll(srcMasterClassifiers);
    commonComponentNames.retainAll(destMasterClassifiers);
    if (getCommandLine().hasOption("classifiers")) {
      List<String> classifiersList = Arrays.asList(getCommandLine().getOptionValues("classifiers"));
      Set<String> classifiers = Sets.newHashSet();
      classifiers.addAll(classifiersList);
      classifiers.removeAll(classifiers);
      if (classifiers.size() > 0) {
        System.err.println("Couldn't find classifiers: " + classifiers.toString() + ", skipping those");
      }
      classifiers.clear();
      classifiers.addAll(classifiersList);
      commonComponentNames.retainAll(classifiers);
    }    
    return commonComponentNames;
  }
  
  @SuppressWarnings("static-access")
  private Option createClassifiersOption() {
    return OptionBuilder.hasArgs()
                        .withArgName("classifier name")
                        .withDescription("specify classifiers for masters to sync")
                        .isRequired(false)
                        .withLongOpt("classifiers")
                        .create("c");
  }
  
  @SuppressWarnings("static-access")
  private Option createFastOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("assume only new data needs copying")
                        .isRequired(false)
                        .withLongOpt("fast")
                        .create("f");
  }
  
  @SuppressWarnings("static-access")
  private Option createHardSyncOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("remove time series at destination not present in source")
                        .isRequired(false)
                        .withLongOpt("hard-sync")
                        .create("h");
  }
  
  @SuppressWarnings("static-access")
  private Option createNoAdditionsOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("don't add any time series to the destination, only update what's there")
                        .isRequired(false)
                        .withLongOpt("no-additions")
                        .create("n");
  }
    
  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.hasArg(false)
                        .withDescription("show extra messages")
                        .isRequired(false)
                        .withLongOpt("verbose")
                        .create("v");
  }
  
  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption(createClassifiersOption());
    options.addOption(createVerboseOption());
    options.addOption(createFastOption());
    options.addOption(createHardSyncOption());
    options.addOption(createNoAdditionsOption());
    return options;
  }

}
