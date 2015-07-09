/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.net.URI;
import java.util.List;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * View runner implementation that executes requests on a remote view runner over REST.
 */
public class RemoteViewRunner extends AbstractRemoteClient implements ViewRunner {

  /**
   * @param baseUri the base URI of the remote server
   */
  public RemoteViewRunner(URI baseUri) {
    super(baseUri);
  }

  @Override
  public Results runView(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment marketData,
      List<?> portfolio) {

    URI uri = DataViewRunnerResource.uriRunView(getBaseUri());
    RunViewArguments args = new RunViewArguments(viewConfig, calculationArguments, marketData, portfolio);
    return accessRemote(uri).post(Results.class, args);
  }

  @Override
  public ScenarioResults runScenarios(
      ViewConfig viewConfig,
      CalculationArguments calculationArguments,
      MarketDataEnvironment baseMarketData,
      ScenarioDefinition scenarioDefinition,
      List<?> portfolio) {

    URI uri = DataViewRunnerResource.uriRunScenarios(getBaseUri());

    // As RunScenariosArguments works with List<Object>, if we call the
    // portfolio method on the builder it picks the version taking
    // Object... rather than the version taking List<Object>. This
    // means that the portfolio gets wrapped in another list
    // and fails later.
    // For this reason we need to cast to enforce the correct call
    @SuppressWarnings("unchecked")
    List<Object> pf = (List<Object>) portfolio;

    RunScenariosArguments args =
        RunScenariosArguments.builder()
            .viewConfig(viewConfig)
            .calculationArguments(calculationArguments)
            .marketData(baseMarketData)
            .scenarioDefinition(scenarioDefinition)
            .portfolio(pf)
            .build();
    return accessRemote(uri).post(ScenarioResults.class, args);
  }
}
