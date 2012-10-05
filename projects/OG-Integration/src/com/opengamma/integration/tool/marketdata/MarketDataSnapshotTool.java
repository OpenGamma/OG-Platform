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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.time.Instant;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import com.opengamma.util.generate.scripts.Scriptable;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
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
import com.opengamma.component.tool.AbstractComponentTool;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * The entry point for running OpenGamma batches. 
 */
@Scriptable
public class MarketDataSnapshotTool extends AbstractComponentTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotTool.class);

  /** Logging command line option. */
  private static final String VIEW_NAME_OPTION = "v";
  /** Valuation time command line option. */
  private static final String VALUATION_TIME_OPTION = "t";
  /** Time format: yyyyMMdd */
  private static final DateTimeFormatter VALUATION_TIME_FORMATTER = DateTimeFormatters.pattern("HH:mm:ss");

  private static final List<String> DEFAULT_PREFERRED_CLASSIFIERS = Arrays.asList("central", "main", "default", "shared", "combined");
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new MarketDataSnapshotTool().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

  @Override
  protected void doRun() throws Exception {
    // REVIEW jonathan 2012-05-16 -- refactored to use AbstractComponentTool but retained the original logic. However,
    // this needs some work:
    // 
    //  - the RemoteComponentFactory is probably the way forward for tools, but we need to identify the main instance
    //    of the components rather than iterating over everything there
    //  - it's not clear how many snapshots will be created because of the above, but only one will be saved
    //  - since only one snapshot is eventually saved, no point creating multiple or using executor service
    //  - needs to do some logging in the normal case to provide feedback about what's happening
    //  - searching by view definition _name_ may not be unique enough or may pull out a deleted view definition
    
    final String viewDefinitionName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);

    String valuationTimeArg = getCommandLine().getOptionValue(VALUATION_TIME_OPTION);
    Instant valuationInstant;
    if (!StringUtils.isBlank(valuationTimeArg)) {
      LocalTime valuationTime = LocalTime.parse(valuationTimeArg, VALUATION_TIME_FORMATTER);
      valuationInstant = ZonedDateTime.now().withTime(valuationTime.getHourOfDay(), valuationTime.getMinuteOfHour(), valuationTime.getSecondOfMinute()).toInstant();
    } else {
      valuationInstant = Instant.now();
    }
    
    MarketDataSpecification marketDataSpecification = MarketData.live();
    ViewExecutionOptions viewExecutionOptions = ExecutionOptions.singleCycle(valuationInstant, marketDataSpecification, EnumSet.of(ViewExecutionFlags.AWAIT_MARKET_DATA));
    
    List<RemoteViewProcessor> viewProcessors = getRemoteComponentFactory().getViewProcessors();
    if (viewProcessors.size() == 0) {
      s_logger.warn("No view processors found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }
    MarketDataSnapshotMaster marketDataSnapshotMaster = getRemoteComponentFactory().getMarketDataSnapshotMaster(DEFAULT_PREFERRED_CLASSIFIERS);
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }
    Collection<ConfigMaster> configMasters = getRemoteComponentFactory().getConfigMasters().values();
    if (configMasters.size() == 0) {
      s_logger.warn("No config masters found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }
    
    int cores = Math.max(1, Runtime.getRuntime().availableProcessors()); 
    ExecutorService executor = Executors.newFixedThreadPool(cores);
    
    RemoteViewProcessor viewProcessor = viewProcessors.get(0);
    MarketDataSnapshotter marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter();
    FutureTask<List<StructuredMarketDataSnapshot>> task = null;
    for (ConfigMaster configMaster : configMasters) {
      ConfigSearchRequest<ViewDefinition> searchRequest = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
      searchRequest.setName(viewDefinitionName);
      ConfigSearchResult<ViewDefinition> searchResult = configMaster.search(searchRequest);
      for (ConfigItem<ViewDefinition> viewDefinition : searchResult.getValues()) {
        task = new FutureTask<List<StructuredMarketDataSnapshot>>(new SingleSnapshotter(marketDataSnapshotter, viewProcessor, viewDefinition.getValue(), viewExecutionOptions, task));
        executor.execute(task);
      }
    }

    if (task != null) {
      for (StructuredMarketDataSnapshot snapshot : task.get()) {
        ManageableMarketDataSnapshot manageableMarketDataSnapshot = new ManageableMarketDataSnapshot(
            snapshot.getBasisViewName() + "/" + valuationInstant, snapshot.getGlobalValues(), snapshot.getYieldCurves());
        marketDataSnapshotMaster.add(new MarketDataSnapshotDocument(manageableMarketDataSnapshot));
      }
    }
  }
  
  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption(createViewNameOption());
    options.addOption(createValuationTimeOption());
    return options;
  }

  private static Option createViewNameOption() {
    Option option = new Option(VIEW_NAME_OPTION, "viewName", true, "the view definition name");
    option.setArgName("view name");
    option.setRequired(true);
    return option;
  }
  
  private static Option createValuationTimeOption() {
    Option option = new Option(VALUATION_TIME_OPTION, "valuationTime", true, "the valuation time, HH:mm[:ss] (defaults to now)");
    option.setArgName("valuation time");
    return option;    
  }

  //-------------------------------------------------------------------------
  private static StructuredMarketDataSnapshot makeSnapshot(MarketDataSnapshotter marketDataSnapshotter,
      ViewProcessor viewProcessor, ViewDefinition viewDefinition, ViewExecutionOptions viewExecutionOptions) throws InterruptedException {
    final ViewClient vc = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    vc.setResultListener(new ViewResultListener() {
      @Override
      public UserPrincipal getUser() {
        String ipAddress;
        try {
          ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
          ipAddress = "unknown";
        }
        return new UserPrincipal("MarketDataSnapshotterTool", ipAddress);
      }

      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      }

      @Override
      public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
        s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      }

      @Override
      public void cycleStarted(ViewCycleMetadata cycleMetadata) {
      }

      @Override
      public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
      }

      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        s_logger.info("cycle completed");
      }

      @Override
      public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
        s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      }

      @Override
      public void processCompleted() {
      }

      @Override
      public void processTerminated(boolean executionInterrupted) {
      }

      @Override
      public void clientShutdown(Exception e) {
      }
    });
    vc.setViewCycleAccessSupported(true);
    vc.attachToViewProcess(viewDefinition.getUniqueId(), viewExecutionOptions);

    vc.waitForCompletion();
    vc.pause();
    EngineResourceReference<? extends ViewCycle> cycleReference = null;
    try {
      cycleReference = vc.createLatestCycleReference();
      return marketDataSnapshotter.createSnapshot(vc, cycleReference.get());
    } finally {
      cycleReference.release();
      vc.shutdown();
    }
  }
  
  private static class SingleSnapshotter implements Callable<List<StructuredMarketDataSnapshot>> {
    private ViewDefinition _viewDefinition;
    private MarketDataSnapshotter _marketDataSnapshotter;
    private ViewProcessor _viewProcessor;
    private ViewExecutionOptions _viewExecutionOptions;
    private FutureTask<List<StructuredMarketDataSnapshot>> _prev;

    SingleSnapshotter(MarketDataSnapshotter marketDataSnapshotter, ViewProcessor viewProcessor,
        ViewDefinition viewDefinition, ViewExecutionOptions viewExecutionOptions, FutureTask<List<StructuredMarketDataSnapshot>> prev) {
      _marketDataSnapshotter = marketDataSnapshotter;
      _viewProcessor = viewProcessor;
      _viewExecutionOptions = viewExecutionOptions;
      _viewDefinition = viewDefinition;
      _prev = prev;
    }

    @Override
    public List<StructuredMarketDataSnapshot> call() throws Exception {
      StructuredMarketDataSnapshot snapshot = makeSnapshot(_marketDataSnapshotter, _viewProcessor, _viewDefinition, _viewExecutionOptions);
      if (_prev == null) {
        return newArrayList(snapshot);
      } else {
        _prev.get();
        List<StructuredMarketDataSnapshot> result = newArrayList(snapshot);
        result.addAll(_prev.get());
        return result;
      }
    }
  }

}
