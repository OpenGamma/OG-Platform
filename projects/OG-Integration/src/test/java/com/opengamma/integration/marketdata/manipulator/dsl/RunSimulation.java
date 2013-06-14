/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;

/**
 * Demonstration of the most straightforward way to run a simulation. Assumes the OG-Examples server is running
 * locally with the default configuration and data.
 */
/* package */ class RunSimulation {

  private static final Logger s_logger = LoggerFactory.getLogger(RunSimulation.class);

  private static final Set<String> CURRENCY_PAIRS = ImmutableSet.of("GBPUSD", "EURUSD", "USDJPY", "CHFUSD");
  private static final List<Double> SCALING_FACTORS = ImmutableList.of(0.95, 1.0, 1.05);

  public static void main(String[] args) {

    // set up the connection to the server that will run the simulation ------------------------------------------------

    try (RemoteServer server = RemoteServer.create("http://localhost:8080")) {
      ViewProcessor viewProcessor = server.getViewProcessor();
      ConfigSource configSource = server.getConfigSource();
      String viewDefName = "AUD Swaps (3m / 6m basis) (1)";
      Collection<ConfigItem<ViewDefinition>> viewDefs =
          configSource.get(ViewDefinition.class, viewDefName, VersionCorrection.LATEST);
      UniqueId viewDefId = viewDefs.iterator().next().getValue().getUniqueId();
      List<MarketDataSpecification> marketDataSpecs =
          ImmutableList.<MarketDataSpecification>of(new LiveMarketDataSpecification("Simulated live market data"));

      // define the simulation -----------------------------------------------------------------------------------------

      Simulation simulation = new Simulation();
      for (Double scalingFactor : SCALING_FACTORS) {
        Scenario scenario = simulation.addScenario();
        for (String currencyPair : CURRENCY_PAIRS) {
          scenario.marketDataPoint().id("OG_SYNTHETIC_TICKER", currencyPair).apply().scaling(scalingFactor);
        }
      }

      // run the simulation --------------------------------------------------------------------------------------------

      simulation.run(viewDefId, marketDataSpecs, false, new Listener(), viewProcessor);
    }
  }

  private static class Listener extends AbstractViewResultListener {

      @Override
      public UserPrincipal getUser() {
        return UserPrincipal.getTestUser();
      }

      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
        s_logger.info("view definition compiled");
      }

      @Override
      public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
        s_logger.warn("view definition compilation failed", exception);
      }

      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        s_logger.info("cycle completed");
      }
    }

}
