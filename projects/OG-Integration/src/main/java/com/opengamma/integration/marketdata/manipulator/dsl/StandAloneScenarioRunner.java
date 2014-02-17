/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Runs a set of scenarios defined in a Groovy DSL script against a running server and returns the results.
 */
public class StandAloneScenarioRunner {

  private StandAloneScenarioRunner() {
  }

  // TODO allow a default server to be picked up from config or the registry. or maybe supplied as an argument

  /**
   * Runs a set of scenarios defined in a Groovy DSL script against a running server.
   * @param scriptFileName The file containing the scenario definitions
   * @return The results of running the scenarios
   * @throws IOException If the script file can't be found or read
   * @throws IllegalArgumentException If any data in the script is invalid or missing
   */
  public static List<ScenarioResultModel> runScenarioScript(String scriptFileName) throws IOException {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(StandAloneScenarioScript.class.getName());
    GroovyShell shell = new GroovyShell(config);
    StandAloneScenarioScript script;

    try (Reader reader = new BufferedReader(new FileReader(scriptFileName))) {
      script = (StandAloneScenarioScript) shell.parse(reader, scriptFileName);
    }
    Binding binding = new Binding();
    SimulationUtils.registerAliases(binding);
    script.setBinding(binding);
    script.run();

    // pull the data out of the script
    ViewDelegate viewDelegate = script.getViewDelegate();
    String viewName = viewDelegate.getName();
    String serverUrl = viewDelegate.getServer();
    List<MarketDataDelegate.MarketDataSpec> marketDataSpecs = viewDelegate.getMarketDataDelegate().getSpecifications();

    if (StringUtils.isEmpty(viewName)) {
      throw new IllegalArgumentException("A view name must be specified");
    }
    if (StringUtils.isEmpty(serverUrl)) {
      throw new IllegalArgumentException("A server must be specified");
    }
    if (marketDataSpecs == null || marketDataSpecs.isEmpty()) {
      throw new IllegalArgumentException("Market data must be specified to run the view");
    }

    ScenarioListener listener;
    try (RemoteServer server = RemoteServer.create(serverUrl)) {
      ViewDefinition viewDef = server.getConfigSource().getLatestByName(ViewDefinition.class, viewName);
      if (viewDef == null) {
        throw new IllegalArgumentException("No view definition found with name " + viewName);
      }
      List<MarketDataSpecification> dataSpecs = convertMarketData(marketDataSpecs,
                                                                  server.getMarketDataSnapshotMaster());
      Simulation simulation = script.getSimulation();
      listener = new ScenarioListener(simulation.getScenarioNames());
      simulation.run(viewDef.getUniqueId(), dataSpecs, false, listener, server.getViewProcessor());
    }
    List<SimpleResultModel> results = listener.getResults();
    List<ScenarioResultModel> scenarioResults = Lists.newArrayListWithCapacity(results.size());
    for (SimpleResultModel result : results) {
      scenarioResults.add(new ScenarioResultModel(result, script.getScenarioParameters(result.getCycleName())));
    }
    return scenarioResults;
  }

  /**
   * Converts the market data specified by the script into a format usable by the engine.
   * This is necessary because snapshot data sources must be created with a {@link UniqueId} but we
   * want to refer to them by name in the script.
   * @param marketDataSpecs Market data sources defined in the script
   * @param snapshotMaster For converting snapshot names to IDs
   * @return Market data specifications suitable for the engine
   */
  private static List<MarketDataSpecification> convertMarketData(List<MarketDataDelegate.MarketDataSpec> marketDataSpecs,
                                                                 MarketDataSnapshotMaster snapshotMaster) {
    List<MarketDataSpecification> specifications = Lists.newArrayListWithCapacity(marketDataSpecs.size());
    for (MarketDataDelegate.MarketDataSpec spec : marketDataSpecs) {
      MarketDataSpecification specification;
      switch (spec.getType()) {
        case LIVE:
          specification = LiveMarketDataSpecification.of(spec.getSpec());
          break;
        case FIXED_HISTORICAL:
          LocalDate date;
          try {
            date = LocalDate.parse(spec.getSpec());
          } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Historical market data date isn't in a valid format. Expected format " +
                                                   "'yyyy-MM-dd', value: " + spec.getSpec());
          }
          specification = new FixedHistoricalMarketDataSpecification(date);
          break;
        case LATEST_HISTORICAL:
          specification = new LatestHistoricalMarketDataSpecification();
          break;
        case SNAPSHOT:
          MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
          searchRequest.setName(spec.getSpec());
          MarketDataSnapshotSearchResult searchResult = snapshotMaster.search(searchRequest);
          List<ManageableMarketDataSnapshot> snapshots = searchResult.getSnapshots();
          if (snapshots.isEmpty()) {
            throw new IllegalArgumentException("No snapshot found named " + spec.getSpec());
          }
          if (snapshots.size() > 1) {
            throw new IllegalArgumentException("Multiple snapshots found named " + spec.getSpec());
          }
          specification = UserMarketDataSpecification.of(snapshots.get(0).getUniqueId());
          break;
        default:
          throw new IllegalArgumentException("Unexpected market data type: " + spec.getType());
      }
      specifications.add(specification);
    }
    return specifications;
  }
}
