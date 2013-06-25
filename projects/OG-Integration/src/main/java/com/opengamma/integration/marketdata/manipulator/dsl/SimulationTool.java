/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Tool for running simulations. It's probably easier to use the approach in {@code RunSimulation} instead.
 * @deprecated This can probably be deleted
 */
@Scriptable
public class SimulationTool extends AbstractTool<ToolContext> {

  /** Command line option for view definition name. */
  private static final String VIEW_DEF_NAME_OPTION = "v";
  /** Command line option for the class name of an implementation of SimulationSupplier. */
  private static final String SIMULATION_SUPPLIER_CLASS_OPTION = "s";
  /** Command line option for whether to execute in batch mode. */
  private static final String BATCH_MODE_OPTION = "b";
  /** Command line option for the class name of an implementation of ViewResultListener. */
  private static final String RESULT_LISTENER_CLASS_OPTION = "r";

  public static void main(final String[] args) {
    new SimulationTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    ConfigSource configSource = getToolContext().getConfigSource();
    String portfolioName = getCommandLine().getOptionValue('p');
    String supplierClassName = getCommandLine().getOptionValue('s');
    boolean batchMode = getCommandLine().hasOption('b');
    ViewResultListener listener;
    if (getCommandLine().hasOption('v')) {
      String listenerClass = getCommandLine().getOptionValue('v');
      listener = instantiate(listenerClass, ViewResultListener.class);
    } else {
      listener = null;
    }
    // TODO option for market data
    List<MarketDataSpecification> marketDataSpecs =
        ImmutableList.<MarketDataSpecification>of(new LiveMarketDataSpecification("Simulated live market data"));
    SimulationSupplier supplier = instantiate(supplierClassName, SimulationSupplier.class);
    Simulation simulation = supplier.get();
    VersionCorrection viewDefVersionCorrection = VersionCorrection.LATEST;
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        configSource.get(ViewDefinition.class, portfolioName, viewDefVersionCorrection);
    if (viewDefs.isEmpty()) {
      throw new IllegalStateException("View definition " + portfolioName + " not found");
    }
    ConfigItem<ViewDefinition> viewDef = viewDefs.iterator().next();
    UniqueId viewDefId = viewDef.getUniqueId();
    simulation.run(viewDefId, marketDataSpecs, batchMode, listener, viewProcessor);
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

    Option viewDefNameOption = new Option(VIEW_DEF_NAME_OPTION, true, "View definition name");
    viewDefNameOption.setRequired(true);
    viewDefNameOption.setArgName("viewdefname");
    options.addOption(viewDefNameOption);

    Option simulationSupplierOption = new Option(SIMULATION_SUPPLIER_CLASS_OPTION, true, "Simulation supplier class");
    simulationSupplierOption.setRequired(true);
    simulationSupplierOption.setArgName("supplierclass");
    options.addOption(simulationSupplierOption);

    Option batchModeOption = new Option(BATCH_MODE_OPTION, false, "Run in batch mode");
    batchModeOption.setArgName("batchmode");
    options.addOption(batchModeOption);

    Option resultListenerClassOption = new Option(RESULT_LISTENER_CLASS_OPTION, true, "Result listener class " +
        "implementing ViewResultListener");
    resultListenerClassOption.setArgName("resultlistenerclass");
    options.addOption(resultListenerClassOption);

    return options;
  }
}

