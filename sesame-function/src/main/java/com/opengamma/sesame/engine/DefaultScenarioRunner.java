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
   * This only handles one very specific use case: views whose calculations require curves and no other market data.
   * The pre-calibrated multicurve bundles must be in the market data environment.
   *
   * @param viewConfig configuration of the view that performs the calculations
   * @param marketDataEnvironment contains the market data for the scenarios
   * @param portfolio the items in the portfolio
   * @return the results of running the calculations in the view for every item in the portfolio and every scenario
   */
  @Override
  public ScenarioResults runScenario(ViewConfig viewConfig,
                                     ScenarioMarketDataEnvironment marketDataEnvironment,
                                     List<?> portfolio) {

    Map<String, MarketDataEnvironment> scenarioData = marketDataEnvironment.getData();
    Map<String, Results> resultsMap = new HashMap<>();

    for (Map.Entry<String, MarketDataEnvironment> entry : scenarioData.entrySet()) {
      String scenarioName = entry.getKey();
      MarketDataEnvironment scenarioMarketData = entry.getValue();
      PreCalibratedMulticurveArguments scenarioArgument = getScenarioArguments(scenarioMarketData);
      ScenarioDefinition scenarioDefinition =
          new ScenarioDefinition(scenarioArgument).mergedWith(viewConfig.getScenarioDefinition());
      ViewConfig scenarioViewConfig = viewConfig.toBuilder().scenarioDefinition(scenarioDefinition).build();
      View view = _viewFactory.createView(scenarioViewConfig, inputTypes(portfolio));
      CycleArguments cycleArguments = CycleArguments.builder(new EmptyMarketDataFactory())
                                                    .valuationTime(scenarioMarketData.getValuationTime())
                                                    .build();
      Results results = view.run(cycleArguments, portfolio);
      resultsMap.put(scenarioName, results);
    }
    return new ScenarioResults(resultsMap);
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

    @Override
    public boolean equals(Object obj) {
      return obj != null && (obj == this || obj.getClass() == getClass());
    }

    @Override
    public int hashCode() {
      return getClass().hashCode();
    }
  }
}
