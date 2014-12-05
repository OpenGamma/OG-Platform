/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.net.URI;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exposing a remote {@link Engine}.
 */
@Path("engine")
public class DataEngineResource extends AbstractDataResource {

  /** REST path for running a calculation cycle on a view. */
  public static final String RUN_VIEW_PATH = "runView";

  /** REST path for executing multiple calculation cycles on a view for different scenarios. */
  public static final String RUN_SCENARIOS_PATH = "runScenarios";

  /** The engine that handle the remote requests. */
  private final Engine _engine;

  /**
   * @param engine the engine that handle the remote requests
   */
  public DataEngineResource(Engine engine) {
    _engine = ArgumentChecker.notNull(engine, "engine");
  }

  /**
   * Retrieve the URI for the {@link #runView(EngineRunViewArguments)} method.
   *
   * @param baseUri the base URI for all requests
   * @return the URI for the method
   */
  public static URI uriRunView(URI baseUri) {
    return createUri(baseUri, RUN_VIEW_PATH);
  }

  /**
   * Retrieve the URI for the {@link #runScenarios(EngineRunScenariosArguments)} method.
   *
   * @param baseUri the base URI for all requests
   * @return the URI for the method
   */
  public static URI uriRunScenarios(URI baseUri) {
    return createUri(baseUri, RUN_SCENARIOS_PATH);
  }

  private static URI createUri(URI baseUri, String path) {
    final String fullPath = "/engine/" + path;
    return UriBuilder.fromUri(baseUri).path(fullPath).build();
  }

  /**
   * Exposes {@link Engine#runView(ViewConfig, CalculationArguments, MarketDataEnvironment, List)} via REST.
   *
   * @param arguments arguments to the method call
   * @return the result of calling {@link Engine#runView(ViewConfig, CalculationArguments, MarketDataEnvironment, List)}
   */
  @POST
  @Path(RUN_VIEW_PATH)
  public Results runView(EngineRunViewArguments arguments) {
    return _engine.runView(arguments.getViewConfig(),
                           arguments.getCalculationArguments(),
                           arguments.getSuppliedData(),
                           arguments.getPortfolio());
  }

  /**
   * Exposes {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)} via REST.
   *
   * @param arguments arguments to the method call
   * @return the result of calling {@link Engine#runScenarios(ViewConfig, ScenarioCalculationArguments, ScenarioMarketDataEnvironment, List)}
   */
  @POST
  @Path(RUN_SCENARIOS_PATH)
  public ScenarioResults runScenarios(EngineRunScenariosArguments arguments) {
    return _engine.runScenarios(arguments.getViewConfig(),
                                arguments.getCalculationArguments(),
                                arguments.getMarketData(),
                                arguments.getPortfolio());
  }
}
