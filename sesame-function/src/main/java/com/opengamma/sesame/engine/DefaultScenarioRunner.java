/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.PreCalibratedMulticurveArguments;
import com.opengamma.sesame.config.NonPortfolioOutput;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.scenarios.ScenarioDefinition;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Default implementation of {@link ScenarioRunner}.
 * TODO move everything to DefaultEngine and delegate to an Engine
 */
public class DefaultScenarioRunner implements ScenarioRunner {

  private final ViewFactory _viewFactory;

  /**
   * @param viewFactory factory for creating views to perform calculations
   */
  public DefaultScenarioRunner(ViewFactory viewFactory) {
    _viewFactory = ArgumentChecker.notNull(viewFactory, "viewFactory");
  }

  /**
   * Performs the calculations defined in a view multiple times, using data from a different scenario each time.
   * <p>
   * If the view configuration contains {@code m} columns and the market data environment contains {@code n}
   * scenarios, the results will contain {@code m x n} values for each item in the portfolio.
   * <p>
   * This only handles one very specific use case: views whose calculations require curves and no other market data.
   * The pre-calibrated multicurve bundles must be in the market data environment.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  @Override
  public Results runScenario(ViewConfig viewConfig, ScenarioMarketDataEnvironment marketDataEnvironment, List<?> portfolio) {
    Map<String, MarketDataEnvironment> scenarioData = marketDataEnvironment.getData();

    if (scenarioData.size() == 1) {
      return runSingleScenario(viewConfig, scenarioData.values().iterator().next(), portfolio);
    }
    List<ViewColumn> columns = new ArrayList<>();
    List<NonPortfolioOutput> outputs = new ArrayList<>();
    ScenarioDefinition scenarioDefinition = viewConfig.getScenarioDefinition();

    CycleArguments.Builder cycleArgumentsBuilder = CycleArguments.builder(new EmptyMarketDataFactory());

    for (Map.Entry<String, MarketDataEnvironment> entry : scenarioData.entrySet()) {
      Object scenarioId = entry.getKey();
      MarketDataEnvironment scenarioDataBundle = entry.getValue();
      PreCalibratedMulticurveArguments scenarioArgument = getScenarioArguments(scenarioDataBundle);

      for (ViewColumn column : viewConfig.getColumns()) {
        // add a copy of the column for this scenario, use a name created from the column and scenario names
        String columnName = mungeName(column.getName(), scenarioId);
        ViewColumn copiedColumn = column.toBuilder().name(columnName).build();
        columns.add(copiedColumn);
        // add the scenario arg with this scenario's curve to the scenario definition for this column
        scenarioDefinition = scenarioDefinition.with(scenarioArgument, columnName);
        cycleArgumentsBuilder.valuationTime(scenarioDataBundle.getValuationTime(), columnName);
      }
      for (NonPortfolioOutput output : viewConfig.getNonPortfolioOutputs()) {
        // add a copy of the output for this scenario, use a name created from the output and scenario names
        String outputName = mungeName(output.getName(), scenarioId);
        NonPortfolioOutput copiedOutput = output.toBuilder().name(outputName).build();
        outputs.add(copiedOutput);
        // add the scenario arg with this scenario's curve to the scenario definition for this output
        scenarioDefinition = scenarioDefinition.with(scenarioArgument, outputName);
        cycleArgumentsBuilder.valuationTime(scenarioDataBundle.getValuationTime(), outputName);
      }
    }
    ViewConfig scenarioViewConfig = viewConfig.toBuilder()
        .columns(columns)
        .nonPortfolioOutputs(outputs)
        .scenarioDefinition(scenarioDefinition)
        .build();
    View view = _viewFactory.createView(scenarioViewConfig, inputTypes(portfolio));
    CycleArguments cycleArguments = cycleArgumentsBuilder.build();
    return view.run(cycleArguments, portfolio);
  }

  /**
   * Creates a unique name for a column or non-portfolio output by combining the name with the scenario ID.
   *
   * @param name the column or output name
   * @param scenarioId the scenario ID
   * @return a name derived from the original name and the scenario ID
   */
  private static String mungeName(String name, Object scenarioId) {
    return scenarioId.toString() + " / " + name;
  }

  /**
   * Creates and runs a view for a single scenario using the data in {@code marketData}.
   * <p>
   * This only handles one very specific use case: views whose calculations require curves and no other market data.
   * The pre-calibrated multicurve bundles must be in {@code marketData}.
   *
   * @param viewConfig the configuration that defines the calculations in the view
   * @param marketData the market data required by the calculation in the view
   * @param portfolio the portfolio used as input to the calculations
   * @return the calculation results
   */
  private Results runSingleScenario(ViewConfig viewConfig, MarketDataEnvironment marketData, List<?> portfolio) {
    ScenarioDefinition scenarioDefinition = new ScenarioDefinition(getScenarioArguments(marketData));
    ViewConfig scenarioViewConfig = viewConfig.toBuilder().scenarioDefinition(scenarioDefinition).build();
    View view = _viewFactory.createView(scenarioViewConfig, inputTypes(portfolio));
    EmptyMarketDataFactory marketDataFactory = new EmptyMarketDataFactory();
    ZonedDateTime valuationTime = marketData.getValuationTime();
    CycleArguments cycleArguments = CycleArguments.builder(marketDataFactory).valuationTime(valuationTime).build();
    return view.run(cycleArguments, portfolio);
  }

  /**
   * Creates the scenario arguments to feed the pre-calibrated curves into the curve function.
   *
   * @param marketData the market data bundle for a scenario containing pre-calibrated curves
   * @return the scenario arguments that pass the curves into the engine
   */
  private PreCalibratedMulticurveArguments getScenarioArguments(MarketDataEnvironment marketData) {
    Map<String, MulticurveBundle> multicurves = new HashMap<>();

    for (Map.Entry<MarketDataRequirement, Object> entry : marketData.getData().entrySet()) {
      MarketDataId<?> id = entry.getKey().getMarketDataId();
      Object marketDataItem = entry.getValue();

      if (marketDataItem instanceof MulticurveBundle && id instanceof MulticurveId) {
        MulticurveId multicurveId = (MulticurveId) id;
        MulticurveBundle multicurve = (MulticurveBundle) marketDataItem;

        multicurves.put(multicurveId.getName(), multicurve);
      }
    }
    return new PreCalibratedMulticurveArguments(multicurves);
  }

  /**
   * Returns the set of the types of all items in the portfolio.
   *
   * @param portfolio a list of portfolio items
   * @return the set of the types of all items in the portfolio
   */
  private Set<Class<?>> inputTypes(List<?> portfolio) {
    Set<Class<?>> types = new HashSet<>();

    for (Object item : portfolio) {
      types.add(item.getClass());
    }
    return types;
  }

  /**
   * Cycle market data factory that contains no data.
   */
  private static class EmptyMarketDataFactory implements CycleMarketDataFactory {

    private final MarketDataSource _marketDataSource = new EmptyMarketDataSource();

    @Override
    public MarketDataSource getPrimaryMarketDataSource() {
      return _marketDataSource;
    }

    @Override
    public MarketDataSource getMarketDataSourceForDate(ZonedDateTime valuationDate) {
      return _marketDataSource;
    }

    @Override
    public CycleMarketDataFactory withMarketDataSpecification(MarketDataSpecification marketDataSpec) {
      throw new UnsupportedOperationException("No market data is available");
    }

    @Override
    public CycleMarketDataFactory withPrimedMarketDataSource() {
      throw new UnsupportedOperationException("No market data is available");
    }
  }

  /**
   * Market data source that contains no data.
   */
  private static class EmptyMarketDataSource implements MarketDataSource {

    @Override
    public Result<?> get(ExternalIdBundle id, FieldName fieldName) {
      return Result.failure(FailureStatus.MISSING_DATA, "No market data is available");
    }
  }
}
