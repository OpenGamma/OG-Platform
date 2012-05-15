/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static com.google.common.collect.Lists.newArrayList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.time.Instant;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.ComponentInfo;
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
import com.opengamma.integration.component.RemoteEngineUtils;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.RemoteMarketDataSnapshotMaster;

/**
 * The entry point for running OpenGamma batches. 
 */
public class MarketDataSnapshotTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotTool.class);

  /**
   * Time format: yyyyMMdd
   */
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatters.pattern("HH:mm:ss");

  static LocalTime parseTime(String date) {
    return LocalTime.parse(date, TIME_FORMATTER);
  }

  private static Instant getValuationInstant(CommandLine line, Properties configProperties, String configPropertysFile) {
    String valuationTime = getProperty("valuationTime", line, configProperties, configPropertysFile, false);
    if (valuationTime != null) {
      LocalTime time = parseTime(valuationTime);
      return ZonedDateTime.now().withTime(time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute()).toInstant();
    } else {
      return Instant.now();
    }
  }

  private static String getViewDefinitionName(CommandLine line, Properties configProperties) {
    String view = getProperty("view", line, configProperties);
    if (view == null) {
      throw new IllegalArgumentException("View definition unique Id is mandatory parameter");
    }
    return view;
  }

  static class SingleSnaphoter implements Callable<List<StructuredMarketDataSnapshot>> {
    private ViewDefinition _viewDefinition;
    private MarketDataSnapshotter _marketDataSnapshotter;
    private ViewProcessor _viewProcessor;
    private ViewExecutionOptions _viewExecutionOptions;
    private FutureTask<List<StructuredMarketDataSnapshot>> _prev;

    SingleSnaphoter(MarketDataSnapshotter marketDataSnapshotter, ViewProcessor viewProcessor, ViewDefinition viewDefinition, ViewExecutionOptions viewExecutionOptions, FutureTask<List<StructuredMarketDataSnapshot>> prev) {
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

  public static void main(String[] args) throws Exception {  // CSIGNORE

    final int cores = Math.min(1, Runtime.getRuntime().availableProcessors());
    final ExecutorService execSvc = Executors.newFixedThreadPool(2 * cores);

    if (args.length == 0) {
      usage();
      System.exit(-1);
    }

    CommandLine line = null;
    Properties configProperties = null;

    final String propertyFile = "snapshotter.properties";

    String configPropertyFile = null;

    if (System.getProperty(propertyFile) != null) {
      configPropertyFile = System.getProperty(propertyFile);
      try {
        FileInputStream fis = new FileInputStream(configPropertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
      } catch (FileNotFoundException e) {
        s_logger.error("The system cannot find " + configPropertyFile);
        System.exit(-1);
      }
    } else {
      try {
        FileInputStream fis = new FileInputStream(propertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
        configPropertyFile = propertyFile;
      } catch (FileNotFoundException e) {
        // there is no config file so we expect command line arguments
        try {
          CommandLineParser parser = new PosixParser();
          line = parser.parse(getOptions(), args);
        } catch (ParseException e2) {
          usage();
          System.exit(-1);
        }
      }
    }

    RemoteEngineUtils remoteEngineUtils = new RemoteEngineUtils();

    final String baseURI = getProperty("engineURI", line, configProperties, configPropertyFile);
    final String brokerURI = getProperty("brokerURI", line, configProperties, configPropertyFile);


    remoteEngineUtils.setBaseUri(baseURI);

    remoteEngineUtils.setActiveMQBrokerUrl(brokerURI);


    final String viewDefinitionName = getViewDefinitionName(line, configProperties);

    List<RemoteViewProcessor> viewProcessors = remoteEngineUtils.getViewProcessors();

    Instant valuationTime = getValuationInstant(line, configProperties, configPropertyFile);
    MarketDataSpecification marketDataSpecification = MarketData.live();
    ViewExecutionOptions viewExecutionOptions = ExecutionOptions.singleCycle(valuationTime, marketDataSpecification, EnumSet.of(ViewExecutionFlags.AWAIT_MARKET_DATA));

    List<MarketDataSnapshotMaster> marketDataSnapshotMasters = getMarketDataSnapshotMasters(remoteEngineUtils);

    if (viewProcessors.size() == 0) {
      s_logger.warn("No view processors found at {}", remoteEngineUtils.getBaseURI());
    } else {
      if (marketDataSnapshotMasters.size() == 0) {
        s_logger.warn("No market data snapshot masters at {}", remoteEngineUtils.getBaseURI());
      } else {
        // use first view processor
        RemoteViewProcessor viewProcessor = viewProcessors.get(0);
        MarketDataSnapshotMaster marketDataSnapshotMaster = marketDataSnapshotMasters.get(0);
        try {
          MarketDataSnapshotter marketDataSnapshotter = viewProcessor.getMarketDataSnapshotter();
          FutureTask<List<StructuredMarketDataSnapshot>> task = null;
          for (ConfigMaster configMaster : getConfigMasters(remoteEngineUtils)) {
            for (ViewDefinition viewDefinition : findViewDefinitionsByName(viewDefinitionName, configMaster)) {
              task = new FutureTask<List<StructuredMarketDataSnapshot>>(new SingleSnaphoter(marketDataSnapshotter, viewProcessor, viewDefinition, viewExecutionOptions, task));
              execSvc.execute(task);
            }
          }

          if (task != null) {
            for (StructuredMarketDataSnapshot snapshot : task.get()) {
              MarketDataSnapshotDocument document = new MarketDataSnapshotDocument();
              ManageableMarketDataSnapshot manageableMarketDataSnapshot = new ManageableMarketDataSnapshot(snapshot.getBasisViewName() + "/" + valuationTime, snapshot.getGlobalValues(), snapshot.getYieldCurves());
              marketDataSnapshotMaster.add(new MarketDataSnapshotDocument(manageableMarketDataSnapshot));
            }
          }

        } finally {
          remoteEngineUtils.getJmsConnector().close();
        }
      }
    }
  }

  static private StructuredMarketDataSnapshot makeSnapshot(MarketDataSnapshotter marketDataSnapshotter, ViewProcessor viewProcessor, ViewDefinition viewDefinition, ViewExecutionOptions viewExecutionOptions) throws InterruptedException {
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

  static private List<ConfigMaster> getConfigMasters(RemoteEngineUtils remoteEngineUtils) {
    List<ConfigMaster> configMasters = newArrayList();
    for (ComponentInfo o : remoteEngineUtils.getRemoteComponentServer().getComponentServer().getComponentInfos()) {
      if (ConfigMaster.class.isAssignableFrom(o.getType())) {
        configMasters.add(new RemoteConfigMaster(o.getUri()));
      }
    }
    return configMasters;
  }

  static private List<MarketDataSnapshotMaster> getMarketDataSnapshotMasters(RemoteEngineUtils remoteEngineUtils) {
    List<MarketDataSnapshotMaster> marketDataSnapshotMasters = newArrayList();
    for (ComponentInfo o : remoteEngineUtils.getRemoteComponentServer().getComponentServer().getComponentInfos()) {
      if (MarketDataSnapshotMaster.class.isAssignableFrom(o.getType())) {
        marketDataSnapshotMasters.add(new RemoteMarketDataSnapshotMaster(o.getUri()));
      }
    }
    return marketDataSnapshotMasters;
  }

  static private List<ViewDefinition> findViewDefinitionsByName(final String name, ConfigMaster configMaster) {
    ConfigSearchResult<ViewDefinition> searchResult = configMaster.search(new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class) {
      {
        setName(name);
      }
    });
    return searchResult.getValues();
  }

  private static String getProperty(String property, CommandLine line, Properties properties) {
    return getProperty(property, line, properties, null);
  }

  private static String getProperty(String property, CommandLine line, Properties properties, String configPropertysFile) {
    return getProperty(property, line, properties, configPropertysFile, true);
  }

  private static String getProperty(String propertyName, CommandLine line, Properties properties, String configPropertysFile, boolean required) {
    String optionValue = null;
    if (line != null) {
      optionValue = line.getOptionValue(propertyName);
      if (optionValue != null) {
        return optionValue;
      }
    }
    if (properties != null) {
      optionValue = properties.getProperty(propertyName);
      if (optionValue == null && required) {
        s_logger.error("Cannot find property " + propertyName + " in " + configPropertysFile);
        System.exit(-1);
      }
    } else {
      if (required) {
        s_logger.error("Cannot find option " + propertyName + " in command line arguments");
        System.exit(-1);
      }
    }
    return optionValue;
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java [-DbatchJob.properties={property file}] com.opengamma.financial.batch.BatchJobRunner [options]", getOptions());
  }

  private static Options getOptions() {
    Options options = new Options();

    options.addOption("valuationTime", true, "Valuation time. HH:mm[:ss] - for example, 16:22:09. Default - system clock.");

    options.addOption("view", true, "View name in configuration database. You must specify this.");

    options.addOption("engineURI", true, "URI to remote OG engine - for example 'http://localhost:8080/jax/components/ViewProcessor/main'. You must specify this.");
    options.addOption("brokerURI", true, "URL to activeMQ broker - for example 'tcp://localhost:61616'. You must specify this.");

    return options;
  }

}
