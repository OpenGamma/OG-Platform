/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.manipulator.CompositeMarketDataSelector;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ScenarioTool extends AbstractTool<ToolContext> {

  /** Command line option. */
  private static final String PORTFOLIO_NAME_OPTION = "portfolio";
  /** Command line option. */
  private static final String SCENARIO_PROVIDER_CLASS_OPTION = "scenario";

  public static void main(final String[] args) {
    new ScenarioTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getTestUser());
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        getToolContext().getConfigSource().get(ViewDefinition.class,
                                               "AUD Swaps (3m / 6m basis) (1)", // TODO option for this
                                               VersionCorrection.LATEST);
    if (viewDefs.isEmpty()) {
      throw new IllegalStateException("View definition 'AUD Swaps (3m / 6m basis) (1)' not found");
    }
    ConfigItem<ViewDefinition> viewDef = viewDefs.iterator().next();
    Scenario scenario = createScenario();
    ImmutableList<MarketDataSpecification> marketDataSpecs =
        ImmutableList.<MarketDataSpecification>of(new LiveMarketDataSpecification("Simulated live market data"));
    ViewCycleExecutionOptions defaultOptions =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(Instant.now())
            .setMarketDataSpecifications(marketDataSpecs)
            .setMarketDataSelector(CompositeMarketDataSelector.of(scenario.getMarketDataManipulations().keySet()))
            .setFunctionParameters(scenario.getMarketDataManipulations())
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().awaitMarketData().get();
    ViewCycleExecutionSequence sequence = ArbitraryViewCycleExecutionSequence.single(defaultOptions);
    ViewExecutionOptions executionOptions = ExecutionOptions.of(sequence, defaultOptions, flags);
    viewClient.setResultListener(new Listener());
    viewClient.attachToViewProcess(viewDef.getUniqueId(), executionOptions, true);
    viewClient.triggerCycle();
    viewClient.waitForCompletion();
    viewClient.shutdown();
  }

  // TODO use ScenarioProvider
  private Scenario createScenario() {
    Scenario scenario = new Scenario();
    scenario.curve().named("Discounting").currencies("AUD").apply().parallelShift(0.001).execute();
    return scenario;
  }

  private static class Listener extends AbstractViewResultListener {

    private static final Logger s_logger = LoggerFactory.getLogger(Listener.class);

    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getTestUser();
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      s_logger.info("view definition complied");
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
/*
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);

    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPTION, true, "Portfolio name");
    portfolioNameOption.setRequired(true);
    options.addOption(portfolioNameOption);

    Option scenarioProviderOption = new Option(SCENARIO_PROVIDER_CLASS_OPTION, true, "Scenario provider class");
    portfolioNameOption.setRequired(true);
    options.addOption(portfolioNameOption);
    return options;
  }*/
}
