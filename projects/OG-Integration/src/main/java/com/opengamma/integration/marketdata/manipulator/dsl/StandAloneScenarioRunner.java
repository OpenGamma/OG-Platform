/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurityLink;

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
    return runScenarioScript(new File(scriptFileName));
  }

  /**
   * Runs a set of scenarios defined in a Groovy DSL script against a running server.
   * @param scriptFile The file containing the scenario definitions
   * @return The results of running the scenarios
   * @throws IOException If the script file can't be found or read
   * @throws IllegalArgumentException If any data in the script is invalid or missing
   */
  public static List<ScenarioResultModel> runScenarioScript(File scriptFile) throws IOException {
    StandAloneScenarioScript script = runScript(scriptFile);

    // pull the data out of the script and check it's valid and complete
    ViewDelegate viewDelegate = script.getViewDelegate();
    String viewName = viewDelegate.getName();
    String serverUrl = viewDelegate.getServer();
    List<MarketDataDelegate.MarketDataSpec> marketDataSpecs = viewDelegate.getMarketDataDelegate().getSpecifications();
    validateScript(viewName, serverUrl, marketDataSpecs);

    ScenarioListener listener;
    List<SimpleResultModel> results;

    // connect to the server and execute the scenarios
    try (RemoteServer server = RemoteServer.create(serverUrl)) {
      ViewDefinition viewDef = server.getConfigSource().getLatestByName(ViewDefinition.class, viewName);
      if (viewDef == null) {
        throw new IllegalArgumentException("No view definition found with name " + viewName);
      }
      List<MarketDataSpecification> dataSpecs = convertMarketData(marketDataSpecs, server.getMarketDataSnapshotMaster());
      Simulation simulation = script.getSimulation();
      listener = new ScenarioListener(simulation.getScenarioNames());
      simulation.run(viewDef.getUniqueId(), dataSpecs, false, listener, server.getViewProcessor());
      results = listener.getResults();
      populateSecurities(results, server.getSecuritySource());
    }
    List<ScenarioResultModel> scenarioResults = Lists.newArrayListWithCapacity(results.size());

    for (SimpleResultModel result : results) {
      scenarioResults.add(new ScenarioResultModel(result, script.getScenarioParameters(result.getCycleName())));
    }
    return scenarioResults;
  }

  private static void populateSecurities(List<SimpleResultModel> results, SecuritySource securitySource) {
    for (SimpleResultModel resultModel : results) {
      for (UniqueIdentifiable uniqueIdentifiable : resultModel.getTargets()) {
        SecurityLink securityLink = getSecurityLink(uniqueIdentifiable);

        if (securityLink != null) {
          Security security = loadSecurity(securityLink, securitySource);

          if (security != null) {
            setSecurityLink(uniqueIdentifiable, security);
          }
        }
      }
    }
  }

  private static Security loadSecurity(SecurityLink securityLink, SecuritySource securitySource) {
    Collection<Security> securities;

    if (!securityLink.getExternalId().isEmpty()) {
      securities = securitySource.get(securityLink.getExternalId());

      if (!securities.isEmpty()) {
        return securities.iterator().next();
      }
    }
    if (securityLink.getObjectId() != null) {
      return securitySource.get(securityLink.getObjectId(), VersionCorrection.LATEST);
    }
    return null;
  }

  private static SecurityLink getSecurityLink(Object target) {
    if (target instanceof ManageablePosition) {
      return ((ManageablePosition) target).getSecurityLink();
    } else if (target instanceof SimplePosition) {
      return ((SimplePosition) target).getSecurityLink();
    } else if (target instanceof ManageableTrade) {
      return ((ManageableTrade) target).getSecurityLink();
    } else if (target instanceof SimpleTrade) {
      return ((SimpleTrade) target).getSecurityLink();
    } else {
      return null;
    }
  }

  private static void setSecurityLink(Object positionOrTrade, Security security) {
    if (positionOrTrade instanceof ManageablePosition) {
      ((ManageablePosition) positionOrTrade).setSecurityLink(ManageableSecurityLink.of(security));
    } else if (positionOrTrade instanceof ManageableTrade) {
      ((ManageableTrade) positionOrTrade).setSecurityLink(ManageableSecurityLink.of(security));
    } else if (positionOrTrade instanceof SimplePosition) {
      ((SimplePosition) positionOrTrade).setSecurityLink(SimpleSecurityLink.of(security));
    } else if (positionOrTrade instanceof SimpleTrade) {
      ((SimpleTrade) positionOrTrade).setSecurityLink(SimpleSecurityLink.of(security));
    }
  }

  private static StandAloneScenarioScript runScript(File scriptFile) throws IOException {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(StandAloneScenarioScript.class.getName());
    GroovyShell shell = new GroovyShell(config);
    StandAloneScenarioScript script;

    try (Reader reader = new BufferedReader(new FileReader(scriptFile))) {
      script = (StandAloneScenarioScript) shell.parse(reader, scriptFile.getAbsolutePath());
    }
    Binding binding = new Binding();
    SimulationUtils.registerAliases(binding);
    script.setBinding(binding);
    script.run();
    return script;
  }

  private static void validateScript(String viewName,
                                     String serverUrl,
                                     List<MarketDataDelegate.MarketDataSpec> marketDataSpecs) {
    if (StringUtils.isEmpty(viewName)) {
      throw new IllegalArgumentException("A view name must be specified");
    }
    if (StringUtils.isEmpty(serverUrl)) {
      throw new IllegalArgumentException("A server must be specified");
    }
    if (marketDataSpecs == null || marketDataSpecs.isEmpty()) {
      throw new IllegalArgumentException("Market data must be specified to run the view");
    }
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
