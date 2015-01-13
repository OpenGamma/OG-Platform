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
import com.opengamma.sesame.marketdata.ScenarioDataBuilder;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.scenarios.PerturbationMapping;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class RemoteEngineTest {

  private final ViewConfig _viewConfig = configureView("view name", column("col"));

  private Server _jettyServer;
  private Engine _remoteEngine;

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
    WebAppContext ogWebAppContext = new WebAppContext("RemoteEngineTest", "/");
    org.springframework.core.io.Resource resource = new ClassPathResource("web-engine");
    ogWebAppContext.setBaseResource(Resource.newResource(resource.getFile()));
    DataEngineResource engineResource = new DataEngineResource(new TestEngine());
    ComponentRepository repo = new ComponentRepository(ComponentLogger.Console.VERBOSE);
    repo.getRestComponents().publishResource(engineResource);
    repo.getRestComponents().publishHelper(new FudgeObjectBinaryConsumer());
    repo.getRestComponents().publishHelper(new FudgeObjectBinaryProducer());
    ogWebAppContext.setEventListeners(new EventListener[]{new ComponentRepositoryServletContextListener(repo)});
    handlers.addHandler(ogWebAppContext);
    _jettyServer.setHandler(handlers);
    _jettyServer.start();
    _remoteEngine = new RemoteEngine(URI.create(serverUrl));
  }

  @AfterClass
  public void stopServer() throws Exception {
    if (_jettyServer != null) {
      _jettyServer.stop();
    }
  }

  @Test
  public void runView() {
    Results results = _remoteEngine.runView(_viewConfig,
                                            CalculationArguments.builder().build(),
                                            MarketDataEnvironmentBuilder.empty(),
                                            Collections.emptyList());
    assertEquals("runView successfully invoked", results.get(0, 0).getResult().getValue());
  }

  public void runScenarios1() {
    ScenarioMarketDataEnvironment marketData = new ScenarioDataBuilder().build();
    CalculationArguments calculationArguments = CalculationArguments.builder().build();
    ScenarioCalculationArguments scenarioArgs = ScenarioCalculationArguments.of(calculationArguments);
    ScenarioResults results = _remoteEngine.runScenarios(_viewConfig, scenarioArgs, marketData, ImmutableList.of());
    Results scenarioResults = results.getResults().get("scenarioName");
    assertEquals("runScenarios successfully invoked", scenarioResults.get(0, 0).getResult().getValue());
  }

  public void runScenarios2() {
    MarketDataEnvironment marketData = MarketDataEnvironmentBuilder.empty();
    CalculationArguments calculationArgs = CalculationArguments.builder().build();
    ScenarioDefinition scenarioDefinition = new ScenarioDefinition(ImmutableList.<PerturbationMapping>of());
    ScenarioResults results =
        _remoteEngine.runScenarios(
            _viewConfig,
            calculationArgs,
            marketData,
            scenarioDefinition,
            ImmutableList.of());
    Results scenarioResults = results.getResults().get("scenarioName");
    assertEquals("runScenarios successfully invoked", scenarioResults.get(0, 0).getResult().getValue());
  }

  private static class TestEngine implements Engine {

    @Override
    public Results runView(ViewConfig viewConfig,
                           CalculationArguments calculationArguments,
                           MarketDataEnvironment suppliedData,
                           List<?> portfolio) {
      ResultBuilder builder = new ResultBuilder(ImmutableList.of("foo"), ImmutableList.of("col"));
      builder.add(0, 0, Result.success("runView successfully invoked"), null);
      return builder.build(Instant.EPOCH, 0, 0, 0);
    }

    @Override
    public ScenarioResults runScenarios(ViewConfig viewConfig,
                                        ScenarioCalculationArguments calculationArguments,
                                        ScenarioMarketDataEnvironment marketDataEnvironment,
                                        List<?> portfolio) {
      ResultBuilder builder = new ResultBuilder(ImmutableList.of("foo"), ImmutableList.of("col"));
      builder.add(0, 0, Result.success("runScenarios successfully invoked"), null);
      Results results = builder.build(Instant.EPOCH, 0, 0, 0);
      Map<String, Results> resultsMap = ImmutableMap.of("scenarioName", results);
      return new ScenarioResults(resultsMap);
    }

    @Override
    public ScenarioResults runScenarios(ViewConfig viewConfig,
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
