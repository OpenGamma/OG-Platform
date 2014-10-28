/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

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
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MapMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Default implementation of {@link ScenarioRunner}.
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
   * TODO currently this only supports a single scenario
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param valuationTime the valuation time used in the calculations
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  @Override
  public Results runScenario(ViewConfig viewConfig,
                             MarketDataEnvironment<?> marketDataEnvironment,
                             ZonedDateTime valuationTime,
                             List<?> portfolio) {
    Map<?, MapMarketDataBundle> scenarioData = marketDataEnvironment.getData();

    if (scenarioData.size() == 1) {
      return runOnlyScenario(viewConfig, scenarioData.values().iterator().next(), valuationTime, portfolio);
    }
    /*
    TODO handle multiple scenarios
      iterate over scenarios
        duplicate columns in viewConfig
        create scenario arguments
      create scenario definition
      create view config with all columns, prefixing with scenario ID if there's more than 1 scenario
      create view config with the scenario
      create a view
      run the view
    */
    throw new UnsupportedOperationException("Only one scenario is supported at the moment");
  }

  /**
   * Creates and runs a view for a single scenario using the data in {@code marketDataBundle}.
   * <p>
   * This only handles one very specific use case: views whose calculations require curves and no other market data.
   * The pre-calibrated multicurve bundles must be in {@code marketDataBundle}.
   *
   * @param viewConfig the configuration that defines the calculations in the view
   * @param marketDataBundle the market data required by the calculation in the view
   * @param valuationTime the valuation time to use in the calculations
   * @param portfolio the portfolio used as input to the calculations
   * @return the calculation results
   */
  private Results runOnlyScenario(ViewConfig viewConfig,
                                  MapMarketDataBundle marketDataBundle,
                                  ZonedDateTime valuationTime,
                                  List<?> portfolio) {
    Map<MarketDataRequirement, Object> marketData = marketDataBundle.getMarketData();
    Map<String, MulticurveBundle> multicurves = new HashMap<>();

    for (Map.Entry<MarketDataRequirement, Object> entry : marketData.entrySet()) {
      MarketDataId<?> id = entry.getKey().getMarketDataId();
      Object marketDataItem = entry.getValue();

      if (marketDataItem instanceof MulticurveBundle && id instanceof MulticurveId) {
        MulticurveId multicurveId = (MulticurveId) id;
        MulticurveBundle multicurve = (MulticurveBundle) marketDataItem;

        multicurves.put(multicurveId.getName(), multicurve);
      }
    }
    PreCalibratedMulticurveArguments scenarioArgs = new PreCalibratedMulticurveArguments(multicurves);
    View view = _viewFactory.createView(viewConfig, inputTypes(portfolio));
    EmptyMarketDataFactory marketDataFactory = new EmptyMarketDataFactory();
    CycleArguments cycleArguments = CycleArguments.builder(marketDataFactory)
                                                  .valuationTime(valuationTime)
                                                  .scenarioArguments(scenarioArgs)
                                                  .build();
    return view.run(cycleArguments, portfolio);
  }

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
