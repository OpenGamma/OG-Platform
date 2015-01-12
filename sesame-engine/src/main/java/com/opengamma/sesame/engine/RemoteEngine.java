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
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Engine implementation that executes requests on a remote engine over REST.
 */
public class RemoteEngine extends AbstractRemoteClient implements Engine {

  /**
   * @param baseUri the base URI of the remote server
   */
  public RemoteEngine(URI baseUri) {
    super(baseUri);
  }

  @Override
  public Results runView(ViewConfig viewConfig,
                         CalculationArguments calculationArguments,
                         MarketDataEnvironment suppliedData,
                         List<?> portfolio) {
    URI uri = DataEngineResource.uriRunView(getBaseUri());
    EngineRunViewArguments args = new EngineRunViewArguments(viewConfig, calculationArguments, suppliedData, portfolio);
    return accessRemote(uri).post(Results.class, args);
  }

  @Override
  public ScenarioResults runScenarios(ViewConfig viewConfig,
                                      ScenarioCalculationArguments calculationArguments,
                                      ScenarioMarketDataEnvironment marketDataEnvironment,
                                      List<?> portfolio) {
    URI uri = DataEngineResource.uriRunScenarios1(getBaseUri());
    EngineRunScenariosArguments1 args =
        EngineRunScenariosArguments1.builder()
            .viewConfig(viewConfig)
            .calculationArguments(calculationArguments)
            .marketData(marketDataEnvironment)
            .portfolio(portfolio)
            .build();
    return accessRemote(uri).post(ScenarioResults.class, args);
  }

  @Override
  public ScenarioResults runScenarios(ViewConfig viewConfig,
                                      CalculationArguments calculationArguments,
                                      MarketDataEnvironment baseMarketData,
                                      ScenarioDefinition scenarioDefinition,
                                      List<?> portfolio) {
    URI uri = DataEngineResource.uriRunScenarios2(getBaseUri());
    EngineRunScenariosArguments2 args =
        EngineRunScenariosArguments2.builder()
            .viewConfig(viewConfig)
            .calculationArguments(calculationArguments)
            .marketData(baseMarketData)
            .scenarioDefinition(scenarioDefinition)
            .portfolio(portfolio)
            .build();
    return accessRemote(uri).post(ScenarioResults.class, args);
  }
}
