/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.impl.AutoStartViewDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;

/**
 * Simple tool for loading auto start view definitions. If the server is suitably
 * configured then views will be automatically started when the engine starts.
 */
public class AutoStartViewLoaderTool extends AbstractTool<ToolContext> {

  /**
   * View definition command line option.
   */
  private static final String VIEW_DEFINITION = "v";

  /**
   * Execution flags command line option.
   */
  private static final String EXECUTION_FLAGS = "f";

  /**
   * Market data specs command line option.
   */
  private static final String MARKET_DATA_SPECIFICATIONS = "m";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new AutoStartViewLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {

    String viewName = getCommandLine().getOptionValue(VIEW_DEFINITION);
    UniqueId viewDefinitionId = lookupViewDefinition(viewName);
    AutoStartViewDefinition viewDef = new AutoStartViewDefinition(
        viewDefinitionId.toLatest(),
        ExecutionOptions.infinite(parseMarketDataSpecifications(), parseExecutionFlags(), VersionCorrection.LATEST));

    ConfigMaster configMaster = getToolContext().getConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(viewDef, viewName + " Auto Start")));
  }

  private List<MarketDataSpecification> parseMarketDataSpecifications() {

    List<MarketDataSpecification> marketDataSpecifications = new ArrayList<>();
    String[] values = getCommandLine().getOptionValues(MARKET_DATA_SPECIFICATIONS);
    for (String value : values) {
      marketDataSpecifications.add(parseMarketDataSpecification(value));
    }
    return marketDataSpecifications;
  }

  private MarketDataSpecification parseMarketDataSpecification(String value) {

    // Format is expected to be Live:Activ,Snapshot:MySpecialSnap etc.
    String[] parsed = value.split(":");
    if (parsed.length != 2 || parsed[0].isEmpty() || parsed[1].isEmpty()) {
      throw new OpenGammaRuntimeException("Unable to parse market data spec from [" + value +
                                              "] - needs to be of the form <Type>:<Name> e.g. Live:Bloomberg");
    }

    switch (parsed[0].toUpperCase()) {
      case "LIVE":
        return LiveMarketDataSpecification.of(parsed[1]);
      case "HISTORICAL":
        return new HistoricalMarketDataSpecification(parsed[1]);
      case "SNAPSHOT":
        // To be implemented
      default:
        throw new OpenGammaRuntimeException("Unknown market data specification type: " + parsed[0]);
    }
  }

  private EnumSet<ViewExecutionFlags> parseExecutionFlags() {

    Set<ViewExecutionFlags> flags = new HashSet<>();
    String[] values = getCommandLine().getOptionValues(EXECUTION_FLAGS);
    for (String value : values) {
      flags.add(ViewExecutionFlags.valueOf(value));
    }
    return EnumSet.copyOf(flags);
  }

  private UniqueId lookupViewDefinition(String viewName) {
    return getToolContext().getConfigSource().getLatestByName(ViewDefinition.class, viewName).getUniqueId();
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {

    Options options = super.createOptions(mandatoryConfigResource);

    Option viewDefinitionOption = new Option(VIEW_DEFINITION, "viewDefinition", true, "View definition to be used");
    viewDefinitionOption.setRequired(true);
    options.addOption(viewDefinitionOption);

    Option flagsOption = new Option(EXECUTION_FLAGS, "executionFlags", true, "Comma separated list of execution flags to be used");
    flagsOption.setRequired(true);
    flagsOption.setArgs(Option.UNLIMITED_VALUES);
    flagsOption.setValueSeparator(',');
    options.addOption(flagsOption);

    Option marketDataOption = new Option(MARKET_DATA_SPECIFICATIONS, "marketDataSpecifications", true, "Comma separated list of market data specifications to be used");
    marketDataOption.setRequired(true);
    marketDataOption.setArgs(Option.UNLIMITED_VALUES);
    marketDataOption.setValueSeparator(',');
    options.addOption(marketDataOption);

    return options;
  }

}
