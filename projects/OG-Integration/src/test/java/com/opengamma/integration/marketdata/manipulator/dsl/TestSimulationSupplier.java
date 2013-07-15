/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;

/**
 * Provdes simulation data for testing with the Examples-Simulated server, a few currency pairs and scaling factors to
 * apply to them.
 */
public class TestSimulationSupplier implements SimulationSupplier {

  private static final Logger s_logger = LoggerFactory.getLogger(TestSimulationSupplier.class);

  private static final Set<String> CURRENCY_PAIRS = ImmutableSet.of("GBPUSD", "EURUSD", "USDJPY", "CHFUSD");
  private static final List<Double> SCALING_FACTORS = ImmutableList.of(0.95, 1.0, 1.05);

  @Override
  public Simulation get() {
    Simulation simulation = new Simulation();
    for (Double scalingFactor : SCALING_FACTORS) {
      Scenario scenario = simulation.scenario(Double.toString(scalingFactor));
      for (String currencyPair : CURRENCY_PAIRS) {
        scenario.marketDataPoint().id("OG_SYNTHETIC_TICKER", currencyPair).apply().scaling(scalingFactor);
      }
    }
    return simulation;
  }

  public static class Listener extends AbstractViewResultListener {

    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getTestUser();
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition,
                                       boolean hasMarketDataPermissions) {
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

/**
 * This is necessary because the SimulationTool creates an instance of the listener class using reflection. And if
 * the tool is run directly it has no access to the test classpath and can't create the listener. Running it from
 * here ensures the test classes are on the classpath.
 */
/* package */ class SimulationToolRunner {

  public static void main(String[] args) {
    SimulationTool.main(args);
  }
}
