/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.manipulator.CompositeMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class SimulationTool extends AbstractTool<ToolContext> {

  /** Command line option for portfolio name. */
  private static final String PORTFOLIO_NAME_OPTION = "p";
  /** Command line option for the class name of an implementation of SimulationSupplier. */
  private static final String SIMULATION_SUPPLIER_CLASS_OPTION = "s";
  /** Command line option for whether to execute in batch mode. */
  private static final String BATCH_MODE_OPTION = "b";
  /** Command line option for the class name of an implementation of ViewResultListener. */
  private static final String LISTENER_CLASS_OPTION = "v";

  public static void main(final String[] args) {
    new SimulationTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getTestUser());
    String portfolioName = getCommandLine().getOptionValue('p');
    String supplierClassName = getCommandLine().getOptionValue('s');
    SimulationSupplier supplier = instantiate(supplierClassName, SimulationSupplier.class);
    Simulation simulation = supplier.get();
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        getToolContext().getConfigSource().get(ViewDefinition.class, portfolioName, VersionCorrection.LATEST);
    if (viewDefs.isEmpty()) {
      throw new IllegalStateException("View definition " + portfolioName + " not found");
    }
    ConfigItem<ViewDefinition> viewDef = viewDefs.iterator().next();
    // TODO option for market data
    ImmutableList<MarketDataSpecification> marketDataSpecs =
        ImmutableList.<MarketDataSpecification>of(new LiveMarketDataSpecification("Simulated live market data"));
    Set<DistinctMarketDataSelector> allSelectors = simulation.allSelectors();
    ViewCycleExecutionOptions baseOptions =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(Instant.now())
            .setMarketDataSpecifications(marketDataSpecs)
            .setMarketDataSelector(CompositeMarketDataSelector.of(allSelectors))
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    List<ViewCycleExecutionOptions> cycleOptions = simulation.cycleExecutionOptions(baseOptions);
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(cycleOptions);
    EnumSet<ViewExecutionFlags> executionFlags = ExecutionFlags.none().awaitMarketData().runAsFastAsPossible().get();
    ViewExecutionOptions executionOptions;
    if (getCommandLine().hasOption('v')) {
      String listenerClass = getCommandLine().getOptionValue('v');
      ViewResultListener listener = instantiate(listenerClass, ViewResultListener.class);
      viewClient.setResultListener(listener);
    }
    if (getCommandLine().hasOption('b')) {
      executionOptions = ExecutionOptions.batch(sequence, baseOptions);
    } else {
      // TODO warn if no listener, the results are going nowhere
      executionOptions = ExecutionOptions.of(sequence, executionFlags);
    }
    viewClient.attachToViewProcess(viewDef.getUniqueId(), executionOptions, true);
    viewClient.waitForCompletion();
    viewClient.shutdown();
  }

  @SuppressWarnings("unchecked")
  private <T> T instantiate(String className, Class<T> expectedType) {
    Class<?> supplierClass;
    try {
      supplierClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new OpenGammaRuntimeException("Failed to create instance of class " + className, e);
    }
    if (!expectedType.isAssignableFrom(supplierClass)) {
      throw new IllegalArgumentException("Class " + className + " doesn't implement " + expectedType.getName());
    }
    try {
      return (T) supplierClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new OpenGammaRuntimeException("Failed to instantiate " + supplierClass.getName(), e);
    }
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);

    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPTION, true, "Portfolio name");
    portfolioNameOption.setRequired(true);
    portfolioNameOption.setArgName("portfolio");
    options.addOption(portfolioNameOption);

    Option simulationSupplierOption = new Option(SIMULATION_SUPPLIER_CLASS_OPTION, true, "Simulation supplier class");
    simulationSupplierOption.setRequired(true);
    simulationSupplierOption.setArgName("supplierclass");
    options.addOption(simulationSupplierOption);

    Option batchModeOption = new Option(BATCH_MODE_OPTION, false, "Run in batch mode");
    batchModeOption.setArgName("batchmode");
    options.addOption(batchModeOption);

    Option listenerClassOption = new Option(LISTENER_CLASS_OPTION, true, "ViewResultListener class");
    listenerClassOption.setArgName("viewresultlistenerclass");
    options.addOption(listenerClassOption);

    return options;
  }
}

