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
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exposing a remote {@link ViewRunner}.
 */
@Path("viewRunner")
public class DataViewRunnerResource extends AbstractDataResource {

  /** REST path for running a calculation cycle on a view. */
  public static final String RUN_VIEW_PATH = "runView";

  /**
   * REST path for executing multiple calculation cycles on a view for different scenarios.
   * In this version the user provides all the market data for each scenario.
   */
  public static final String RUN_SCENARIOS_PATH = "runScenarios";

  /** The view runner that handle the remote requests. */
  private final ViewRunner _viewRunner;

  /**
   * @param viewRunner the view runner that handle the remote requests
   */
  public DataViewRunnerResource(ViewRunner viewRunner) {
    _viewRunner = ArgumentChecker.notNull(viewRunner, "viewRunner");
  }

  /**
   * Retrieve the URI for the {@link #runView(RunViewArguments)} method.
   *
   * @param baseUri the base URI for all requests
   * @return the URI for the method
   */
  public static URI uriRunView(URI baseUri) {
    return createUri(baseUri, RUN_VIEW_PATH);
  }

  /**
   * Retrieve the URI for the {@link #runScenarios(RunScenariosArguments)} method.
   *
   * @param baseUri the base URI for all requests
   * @return the URI for the method
   */
  public static URI uriRunScenarios(URI baseUri) {
    return createUri(baseUri, RUN_SCENARIOS_PATH);
  }

  private static URI createUri(URI baseUri, String path) {
    final String fullPath = "/viewRunner/" + path;
    return UriBuilder.fromUri(baseUri).path(fullPath).build();
  }

  /**
   * Exposes {@link ViewRunner#runView(ViewConfig, CalculationArguments, MarketDataEnvironment, List)} via REST.
   *
   * @param arguments arguments to the method call
   * @return the result of calling {@link Engine#runView(ViewConfig, CalculationArguments, MarketDataEnvironment, List)}
   */
  @POST
  @Path(RUN_VIEW_PATH)
  public Results runView(RunViewArguments arguments) {
    return _viewRunner.runView(
        arguments.getViewConfig(),
        arguments.getCalculationArguments(),
        arguments.getSuppliedData(),
        arguments.getPortfolio());
  }

  /**
   * Exposes
   * {@link ViewRunner#runScenarios(ViewConfig, CalculationArguments, MarketDataEnvironment, ScenarioDefinition, List)}
   * via REST.
   *
   * @param arguments arguments to the method call
   * @return the result of calling
   *   {@link ViewRunner#runScenarios(ViewConfig, CalculationArguments, MarketDataEnvironment, ScenarioDefinition, List)}
   */
  @POST
  @Path(RUN_SCENARIOS_PATH)
  public ScenarioResults runScenarios(RunScenariosArguments arguments) {
    return _viewRunner.runScenarios(
        arguments.getViewConfig(),
        arguments.getCalculationArguments(),
        arguments.getMarketData(),
        arguments.getScenarioDefinition(),
        arguments.getPortfolio());
  }
}
