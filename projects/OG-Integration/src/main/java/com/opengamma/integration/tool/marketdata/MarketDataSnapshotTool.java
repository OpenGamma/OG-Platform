/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static com.google.common.collect.Lists.newArrayList;
import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatters;

import com.opengamma.component.tool.AbstractComponentTool;
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
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.generate.scripts.Scriptable;

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
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    final boolean success = new MarketDataSnapshotTool().initAndRun(args);
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

    final String valuationTimeArg = getCommandLine().getOptionValue(VALUATION_TIME_OPTION);
    Instant valuationInstant;
    if (!StringUtils.isBlank(valuationTimeArg)) {
      final LocalTime valuationTime = LocalTime.parse(valuationTimeArg, VALUATION_TIME_FORMATTER);
      valuationInstant = ZonedDateTime.now().with(valuationTime.truncatedTo(SECONDS)).toInstant();
    } else {
      valuationInstant = Instant.now();
    }

    final MarketDataSpecification marketDataSpecification = MarketData.live();
    final ViewExecutionOptions viewExecutionOptions = ExecutionOptions.singleCycle(valuationInstant, marketDataSpecification, EnumSet.of(ViewExecutionFlags.AWAIT_MARKET_DATA));

    final List<RemoteViewProcessor> viewProcessors = getRemoteComponentFactory().getViewProcessors();
    if (viewProcessors.size() == 0) {
      s_logger.warn("No view processors found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }
    final MarketDataSnapshotMaster marketDataSnapshotMaster = getRemoteComponentFactory().getMarketDataSnapshotMaster(DEFAULT_PREFERRED_CLASSIFIERS);
    if (marketDataSnapshotMaster == null) {
      s_logger.warn("No market data snapshot masters found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }
    final Collection<ConfigMaster> configMasters = getRemoteComponentFactory().getConfigMasters().values();
    if (configMasters.size() == 0) {
      s_logger.warn("No config masters found at {}", getRemoteComponentFactory().getBaseUri());
      return;
    }

    final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
    final ExecutorService executor = Executors.newFixedThreadPool(cores);

    final RemoteViewProcessor viewProcessor = viewProcessors.get(0);
    final MarketDataSnapshotter marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter();
    FutureTask<List<StructuredMarketDataSnapshot>> task = null;
    for (final ConfigMaster configMaster : configMasters) {
      final ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
      request.setName(viewDefinitionName);
      for (final ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, request)) {
        task = new FutureTask<List<StructuredMarketDataSnapshot>>(new SingleSnapshotter(marketDataSnapshotter, viewProcessor, (ViewDefinition) doc.getConfig().getValue(), viewExecutionOptions, task));
        executor.execute(task);
      }
    }

    if (task != null) {
      for (final StructuredMarketDataSnapshot snapshot : task.get()) {
        final ManageableMarketDataSnapshot manageableMarketDataSnapshot = new ManageableMarketDataSnapshot(snapshot);
        manageableMarketDataSnapshot.setBasisViewName(snapshot.getBasisViewName() + "/" + valuationInstant);
        marketDataSnapshotMaster.add(new MarketDataSnapshotDocument(manageableMarketDataSnapshot));
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions() {
    final Options options = super.createOptions();
    options.addOption(createViewNameOption());
    options.addOption(createValuationTimeOption());
    return options;
  }

  private static Option createViewNameOption() {
    final Option option = new Option(VIEW_NAME_OPTION, "viewName", true, "the view definition name");
    option.setArgName("view name");
    option.setRequired(true);
    return option;
  }

  private static Option createValuationTimeOption() {
    final Option option = new Option(VALUATION_TIME_OPTION, "valuationTime", true, "the valuation time, HH:mm[:ss] (defaults to now)");
    option.setArgName("valuation time");
    return option;
  }

  //-------------------------------------------------------------------------
  private static StructuredMarketDataSnapshot makeSnapshot(final MarketDataSnapshotter marketDataSnapshotter,
      final ViewProcessor viewProcessor, final ViewDefinition viewDefinition, final ViewExecutionOptions viewExecutionOptions) throws InterruptedException {
    final ViewClient vc = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    vc.setResultListener(new ViewResultListener() {
      @Override
      public UserPrincipal getUser() {
        String ipAddress;
        try {
          ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
          ipAddress = "unknown";
        }
        return new UserPrincipal("MarketDataSnapshotterTool", ipAddress);
      }

      @Override
      public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      }

      @Override
      public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
        s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      }

      @Override
      public void cycleStarted(final ViewCycleMetadata cycleMetadata) {
      }

      @Override
      public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, final ViewDeltaResultModel deltaFragment) {
      }

      @Override
      public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
        s_logger.info("cycle completed");
      }

      @Override
      public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
        s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      }

      @Override
      public void processCompleted() {
      }

      @Override
      public void processTerminated(final boolean executionInterrupted) {
      }

      @Override
      public void clientShutdown(final Exception e) {
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
    private final ViewDefinition _viewDefinition;
    private final MarketDataSnapshotter _marketDataSnapshotter;
    private final ViewProcessor _viewProcessor;
    private final ViewExecutionOptions _viewExecutionOptions;
    private final FutureTask<List<StructuredMarketDataSnapshot>> _prev;

    SingleSnapshotter(final MarketDataSnapshotter marketDataSnapshotter, final ViewProcessor viewProcessor,
        final ViewDefinition viewDefinition, final ViewExecutionOptions viewExecutionOptions, final FutureTask<List<StructuredMarketDataSnapshot>> prev) {
      _marketDataSnapshotter = marketDataSnapshotter;
      _viewProcessor = viewProcessor;
      _viewExecutionOptions = viewExecutionOptions;
      _viewDefinition = viewDefinition;
      _prev = prev;
    }

    @Override
    public List<StructuredMarketDataSnapshot> call() throws Exception {
      final StructuredMarketDataSnapshot snapshot = makeSnapshot(_marketDataSnapshotter, _viewProcessor, _viewDefinition, _viewExecutionOptions);
      if (_prev == null) {
        return newArrayList(snapshot);
      } else {
        _prev.get();
        final List<StructuredMarketDataSnapshot> result = newArrayList(snapshot);
        result.addAll(_prev.get());
        return result;
      }
    }
  }

}
