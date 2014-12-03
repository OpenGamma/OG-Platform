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
    URI uri = DataEngineResource.uriRunScenarios(getBaseUri());
    EngineRunScenariosArguments args = new EngineRunScenariosArguments(viewConfig,
                                                                       calculationArguments,
                                                                       marketDataEnvironment,
                                                                       portfolio);
    return accessRemote(uri).post(ScenarioResults.class, args);
  }
}
