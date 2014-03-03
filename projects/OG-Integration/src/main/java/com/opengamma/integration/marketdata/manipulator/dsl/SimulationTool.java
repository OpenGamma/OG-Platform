/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.scripts.Scriptable;

/**
 * Tool for running simulations defined in Groovy DSL scripts.
 */
@Scriptable
public class SimulationTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SimulationTool.class);

  /** Command line option for view definition name. */
  private static final String VIEW_DEF_NAME_OPTION = "v";
  /** Command line option for whether to execute in batch mode. */
  private static final String BATCH_MODE_OPTION = "b";
  /** Command line option for the class name of an implementation of ViewResultListener. */
  private static final String RESULT_LISTENER_CLASS_OPTION = "r";
  /** Command line option for the location of the Groovy script that defines the simulation. */
  private static final String SIMULATION_SCRIPT_OPTION = "s";
  /** Command line option for the location of the Groovy script that defines the simulation parameters. */
  private static final String PARAMETER_SCRIPT_OPTION = "p";
  /** Command line option for the names of the market data sources used for running the view. */
  private static final String MARKET_DATA_OPTION = "m";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new SimulationTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    ConfigSource configSource = getToolContext().getConfigSource();
    String viewDefName = getCommandLine().getOptionValue(VIEW_DEF_NAME_OPTION);
    boolean batchMode = getCommandLine().hasOption(BATCH_MODE_OPTION);
    ViewResultListener listener;
    if (getCommandLine().hasOption(RESULT_LISTENER_CLASS_OPTION)) {
      String listenerClass = getCommandLine().getOptionValue(RESULT_LISTENER_CLASS_OPTION);
      listener = instantiate(listenerClass, ViewResultListener.class);
    } else {
      listener = null;
    }
    String[] marketDataSpecStrs = getCommandLine().getOptionValues(MARKET_DATA_OPTION);
    List<MarketDataSpecification> marketDataSpecs = Lists.newArrayListWithCapacity(marketDataSpecStrs.length);
    for (String marketDataSpecStr : marketDataSpecStrs) {
      try {
        marketDataSpecs.add(MarketDataSpecificationParser.parse(marketDataSpecStr));
      } catch (IllegalArgumentException e) {
        s_logger.warn(MarketDataSpecificationParser.getUsageMessage());
        throw e;
      }
    }
    Map<String, Object> paramValues;
    if (getCommandLine().hasOption(PARAMETER_SCRIPT_OPTION)) {
      String paramScript = getCommandLine().getOptionValue(PARAMETER_SCRIPT_OPTION);
      ScenarioDslParameters params = ScenarioDslParameters.of(FileUtils.readFileToString(new File(paramScript)));
      paramValues = params.getParameters();
    } else {
      paramValues = null;
    }
    String simulationScript = getCommandLine().getOptionValue(SIMULATION_SCRIPT_OPTION);
    Simulation simulation = SimulationUtils.createSimulationFromDsl(simulationScript, paramValues);
    VersionCorrection viewDefVersionCorrection = VersionCorrection.LATEST;
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        configSource.get(ViewDefinition.class, viewDefName, viewDefVersionCorrection);
    if (viewDefs.isEmpty()) {
      throw new IllegalStateException("View definition " + viewDefName + " not found");
    }
    ConfigItem<ViewDefinition> viewDef = viewDefs.iterator().next();
    UniqueId viewDefId = viewDef.getUniqueId();
    s_logger.info("Running simulation using script {}, view '{}', market data {}, batch mode {}",
                  simulationScript, viewDefName, marketDataSpecs, batchMode);
    simulation.run(viewDefId, marketDataSpecs, batchMode, listener, viewProcessor);
  }

  @SuppressWarnings("unchecked")
  private <T> T instantiate(String className, Class<T> expectedType) {
    Class<?> supplierClass;
    try {
      supplierClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Failed to create instance of class " + className, e);
    }
    if (!expectedType.isAssignableFrom(supplierClass)) {
      throw new IllegalArgumentException("Class " + className + " doesn't implement " + expectedType.getName());
    }
    try {
      return (T) supplierClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Failed to instantiate " + supplierClass.getName(), e);
    }
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);

    Option viewDefNameOption = new Option(VIEW_DEF_NAME_OPTION, true, "View definition name");
    viewDefNameOption.setRequired(true);
    viewDefNameOption.setArgName("viewdefname");
    options.addOption(viewDefNameOption);

    Option marketDataOption = new Option(MARKET_DATA_OPTION, true, "Market data source names");
    marketDataOption.setRequired(true);
    marketDataOption.setArgName("marketdata");
    options.addOption(marketDataOption);

    Option simulationScriptOption = new Option(SIMULATION_SCRIPT_OPTION, true, "Simulation script location");
    simulationScriptOption.setRequired(true);
    simulationScriptOption.setArgName("simulationscript");
    options.addOption(simulationScriptOption);

    Option paramScriptOption = new Option(PARAMETER_SCRIPT_OPTION, true, "Simulation parameters script location");
    paramScriptOption.setArgName("simulationparameters");
    options.addOption(paramScriptOption);

    Option batchModeOption = new Option(BATCH_MODE_OPTION, false, "Run in batch mode");
    batchModeOption.setArgName("batchmode");
    options.addOption(batchModeOption);

    Option resultListenerClassOption = new Option(RESULT_LISTENER_CLASS_OPTION, true, "Result listener class " +
        "implementing ViewResultListener");
    resultListenerClassOption.setArgName("resultlistenerclass");
    options.addOption(resultListenerClassOption);

    return options;
  }
}

/* package */ class MarketDataSpecificationParser {

  private static final String LIVE = "live";
  private static final String SNAPSHOT = "snapshot";
  private static final String FIXED_HISTORICAL = "fixedhistorical";
  private static final String LATEST_HISTORICAL = "latesthistorical";

  /**
   * Parses a string to produce a {@link MarketDataSpecification}. See the output of {@link #getUsageMessage()}
   * for examples.
   * @param specStr String representation of a {@link MarketDataSpecification}
   * @return A {@link MarketDataSpecification} instance built from the string
   * @throws IllegalArgumentException If the string can't be parsed
   */
  /* package */ static MarketDataSpecification parse(String specStr) {
    if (specStr.startsWith(LIVE)) {
      return createLiveSpec(removePrefix(specStr, LIVE));
    } else {
      if (specStr.startsWith(SNAPSHOT)) {
        return createSnapshotSpec(removePrefix(specStr, SNAPSHOT));
      } else if (specStr.startsWith(FIXED_HISTORICAL)) {
        return createFixedHistoricalSpec(removePrefix(specStr, FIXED_HISTORICAL));
      } else if (specStr.startsWith(LATEST_HISTORICAL)) {
        return createLatestHistoricalSpec(removePrefix(specStr, LATEST_HISTORICAL));
      } else {
        throw new IllegalArgumentException("Market data must be one of 'live', 'fixedhistorical', 'latesthistorical'" +
                                               " or 'snapshot'");
      }
    }
  }

  private static MarketDataSpecification createLatestHistoricalSpec(String specStr) {
    if (specStr.isEmpty()) {
      return new LatestHistoricalMarketDataSpecification();
    }
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'latesthistorical[:time series rating]'");
    }
    String timeSeriesRating = specStr.substring(1).trim();
    if (timeSeriesRating.isEmpty()) {
      throw new IllegalArgumentException(specStr + " doesn't match 'latesthistorical[:time series rating]'");
    }
    return new LatestHistoricalMarketDataSpecification(timeSeriesRating);
  }

  private static MarketDataSpecification createFixedHistoricalSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'fixedhistorical:timestamp[,time series rating]'");
    }
    String[] strings = specStr.split(",");
    LocalDate date;
    try {
      date = LocalDate.parse(strings[0].substring(1).trim());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Unknown date format", e);
    }
    if (strings.length > 1) {
      String timeSeriesRating = strings[1].trim();
      if (timeSeriesRating.isEmpty()) {
        throw new IllegalArgumentException(specStr + " doesn't match 'fixedhistorical:timestamp[,time series rating]'");
      }
      return new FixedHistoricalMarketDataSpecification(timeSeriesRating, date);
    } else {
      return new FixedHistoricalMarketDataSpecification(date);
    }
  }

  // TODO accept 'snapshot name/timestamp'? friendlier but requires looking up data from the server
  private static MarketDataSpecification createSnapshotSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'snapshot:snapshot ID'");
    }
    String id = specStr.substring(1).trim();
    return UserMarketDataSpecification.of(UniqueId.parse(id));
  }

  private static MarketDataSpecification createLiveSpec(String specStr) {
    if (!specStr.startsWith(":")) {
      throw new IllegalArgumentException(specStr + " doesn't match 'live:source name'");
    }
    String sourceName = specStr.substring(1).trim();
    if (sourceName.isEmpty()) {
      throw new IllegalArgumentException(specStr + " doesn't match 'live:source name'");
    }
    return LiveMarketDataSpecification.of(sourceName);
  }

  private static String removePrefix(String specStr, String prefix) {
    return specStr.substring(prefix.length(), specStr.length());
  }

  /* package */ static String getUsageMessage() {
    return "Examples of valid market data strings:\n" +
        "live:Data provider name\n" +
        "latesthistorical\n" +
        "latesthistorical:Time series rating name\n" +
        "fixedhistorical:2011-08-03\n" +
        "fixedhistorical:2011-08-03,Time series rating name\n" +
        "snapshot:DbSnp~1234";
  }
}
