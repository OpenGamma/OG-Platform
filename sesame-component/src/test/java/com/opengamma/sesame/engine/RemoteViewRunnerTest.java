/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.component.ComponentLogger;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.rest.ComponentRepositoryServletContextListener;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.scenarios.PerturbationMapping;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class RemoteViewRunnerTest {

  private final ViewConfig _viewConfig = configureView("view name", column("col"));

  private Server _jettyServer;
  private ViewRunner _remoteViewRunner;

  @BeforeClass
  public void startServer() throws Exception {
    int port = 49152 + RandomUtils.nextInt(65535 - 49152);
    String serverUrl = "http://localhost:" + port + "/jax";
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(port);
    _jettyServer = new Server();
    _jettyServer.setConnectors(new Connector[]{connector});
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    HandlerCollection handlers = new HandlerCollection();
    handlers.addHandler(contexts);
    WebAppContext ogWebAppContext = new WebAppContext("RemoteViewRunnerTest", "/");
    org.springframework.core.io.Resource resource = new ClassPathResource("web-engine");
    ogWebAppContext.setBaseResource(Resource.newResource(resource.getFile()));
    DataViewRunnerResource viewRunnerResource = new DataViewRunnerResource(new TestViewRunner());
    ComponentRepository repo = new ComponentRepository(ComponentLogger.Console.VERBOSE);
    repo.getRestComponents().publishResource(viewRunnerResource);
    repo.getRestComponents().publishHelper(new FudgeObjectBinaryConsumer());
    repo.getRestComponents().publishHelper(new FudgeObjectBinaryProducer());
    ogWebAppContext.setEventListeners(new EventListener[]{new ComponentRepositoryServletContextListener(repo)});
    handlers.addHandler(ogWebAppContext);
    _jettyServer.setHandler(handlers);
    _jettyServer.start();
    _remoteViewRunner = new RemoteViewRunner(URI.create(serverUrl));
  }

  @AfterClass
  public void stopServer() throws Exception {
    if (_jettyServer != null) {
      _jettyServer.stop();
    }
  }

  public void runView() {
    Results results =
        _remoteViewRunner.runView(
            _viewConfig,
            CalculationArguments.builder().build(),
            MarketDataEnvironmentBuilder.empty(),
            Collections.emptyList());
    assertEquals("runView successfully invoked", results.get(0, 0).getResult().getValue());
  }

  public void runScenarios() {
    MarketDataEnvironment marketData = MarketDataEnvironmentBuilder.empty();
    CalculationArguments calculationArgs = CalculationArguments.builder().build();
    ScenarioDefinition scenarioDefinition = new ScenarioDefinition(ImmutableList.<PerturbationMapping>of());
    ScenarioResults results =
        _remoteViewRunner.runScenarios(
            _viewConfig,
            calculationArgs,
            marketData,
            scenarioDefinition,
            ImmutableList.of());
    Results scenarioResults = results.getResults().get("scenarioName");
    assertEquals("runScenarios successfully invoked", scenarioResults.get(0, 0).getResult().getValue());
  }

  private static class TestViewRunner implements ViewRunner {

    @Override
    public Results runView(
        ViewConfig viewConfig,
        CalculationArguments calculationArguments,
        MarketDataEnvironment marketData,
        List<?> portfolio) {

      ResultBuilder builder = new ResultBuilder(ImmutableList.of("foo"), ImmutableList.of("col"));
      builder.add(0, 0, Result.success("runView successfully invoked"), null);
      return builder.build(Instant.EPOCH, 0, 0, 0);
    }

    //@Override
    public ScenarioResults runScenarios(
        ViewConfig viewConfig,
        CalculationArguments calculationArguments,
        MarketDataEnvironment baseMarketData,
        ScenarioDefinition scenarioDefinition,
        List<?> portfolio) {

      ResultBuilder builder = new ResultBuilder(ImmutableList.of("foo"), ImmutableList.of("col"));
      builder.add(0, 0, Result.success("runScenarios successfully invoked"), null);
      Results results = builder.build(Instant.EPOCH, 0, 0, 0);
      Map<String, Results> resultsMap = ImmutableMap.of("scenarioName", results);
      return new ScenarioResults(resultsMap);
    }
  }
}
