/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;

/* package */class HistoricalViewEvaluationResultBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalViewEvaluationResultBuilder.class);

  // TODO: If the duration of the time series is high, or there are simply a large quantity then we could modify these builders to
  // write directly to the time series database instead (or batch up the points in internal arrays) and the result bundle will be
  // the identifiers of the time series that were written to the database

  /**
   * Builder for time-series where there is at most one result for a given date. Points may be added in any order, allowing the results to be executed in parallel.
   */
  private static class TimeSeriesBuilder {

    private final SortedMap<Integer, Object> _datedResultMap = new Int2ObjectRBTreeMap<Object>();

    public TimeSeriesBuilder() {
    }

    public TimeSeriesBuilder addPoint(final int date, final Object value) {
      if (_datedResultMap.put(date, value) != null) {
        throw new OpenGammaRuntimeException("Received multiple results for date " + date);
      }
      return this;
    }

    @SuppressWarnings("rawtypes")
    public TimeSeries makeTimeSeries() {
      if (_datedResultMap.isEmpty() || Iterables.get(_datedResultMap.values(), 0) instanceof Number) {
        return makeDoubleTimeSeries();
      } else {
        return makeObjectTimeSeries();
      }
    }

    private LocalDateDoubleTimeSeries makeDoubleTimeSeries() {
      int[] dates = new int[_datedResultMap.size()];
      double[] values = new double[_datedResultMap.size()];
      int i = 0;
      for (Map.Entry<Integer, Object> datedResult : _datedResultMap.entrySet()) {
        dates[i] = datedResult.getKey();
        values[i] = (Double) datedResult.getValue();
        i++;
      }
      return ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    }

    private LocalDateObjectTimeSeries<?> makeObjectTimeSeries() {
      int[] dates = new int[_datedResultMap.size()];
      Object[] values = new Object[_datedResultMap.size()];
      int i = 0;
      for (Map.Entry<Integer, Object> datedResult : _datedResultMap.entrySet()) {
        dates[i] = datedResult.getKey();
        values[i] = datedResult.getValue();
        i++;
      }
      return ImmutableLocalDateObjectTimeSeries.of(dates, values);
    }
  }

  private static class ConfigurationResults {

    private Map<ValueSpecification, Set<ValueRequirement>> _requirements;
    private final Map<ValueRequirement, TimeSeriesBuilder> _results = new HashMap<ValueRequirement, TimeSeriesBuilder>();

    public ConfigurationResults(final Collection<ValueRequirement> requirements) {
      for (final ValueRequirement requirement : requirements) {
        _results.put(requirement, new TimeSeriesBuilder());
      }
    }

    public void setRequirements(final Map<ValueSpecification, Set<ValueRequirement>> requirements) {
      _requirements = requirements;
    }

    public void store(final int date, final ValueSpecification specification, final Object value) {
      final Set<ValueRequirement> requirements = _requirements.get(specification);
      if (requirements != null) {
        for (final ValueRequirement requirement : requirements) {
          final TimeSeriesBuilder builder = _results.get(requirement);
          if (builder == null) {
            s_logger.warn("View produced value {} for unrequested requirement {}", specification, requirement);
          } else {
            final TimeSeriesBuilder newBuilder = builder.addPoint(date, value);
            if (newBuilder != builder) {
              _results.put(requirement, newBuilder);
            }
          }
        }
      } else {
        s_logger.warn("View produced unrequested value {}", specification);
      }
    }

    public HistoricalViewEvaluationResult makeResult() {
      final HistoricalViewEvaluationResult results = new HistoricalViewEvaluationResult();
      for (final Map.Entry<ValueRequirement, TimeSeriesBuilder> result : _results.entrySet()) {
        results.addTimeSeries(result.getKey(), result.getValue().makeTimeSeries());
      }
      return results;
    }

  }

  private static class MarketData {

    private final TimeSeriesBuilder _builder = new TimeSeriesBuilder();
    private Collection<ValueSpecification> _alias;

  }

  private static class MarketDataResults extends HashMap<ValueSpecification, MarketData> {

    private static final long serialVersionUID = 1L;

    public void addMarketDataAlias(final ValueSpecification valueSpec, final Collection<ValueSpecification> aliasValueSpecs) {
      MarketData data = get(valueSpec);
      if (data == null) {
        data = new MarketData();
        put(valueSpec, data);
      }
      if (aliasValueSpecs != null) {
        if (data._alias == null) {
          data._alias = new ArrayList<ValueSpecification>(aliasValueSpecs);
        } else {
          data._alias.addAll(aliasValueSpecs);
        }
      }
    }

    public void store(final int date, final ValueSpecification specification, final Object value) {
      MarketData data = get(specification);
      if (data != null) {
        data._builder.addPoint(date, value);
      } else {
        s_logger.warn("View produced unrequested market data {}", specification);
      }
    }

    @SuppressWarnings("rawtypes")
    public HistoricalViewEvaluationMarketData makeResult() {
      final HistoricalViewEvaluationMarketData results = new HistoricalViewEvaluationMarketData();
      for (final Map.Entry<ValueSpecification, MarketData> result : entrySet()) {
        final TimeSeries timeSeries = result.getValue()._builder.makeTimeSeries();
        for (ValueSpecification alias : result.getValue()._alias) {
          results.addTimeSeries(alias, timeSeries);
        }
      }
      return results;
    }

  }

  private final Map<String, ConfigurationResults> _results = new HashMap<String, ConfigurationResults>();
  private final MarketDataResults _marketData;
  private boolean _compiled;

  public HistoricalViewEvaluationResultBuilder(final ViewDefinition viewDefinition, final boolean includeMarketData) {
    for (final ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      final ConfigurationResults configResults = new ConfigurationResults(calcConfig.getSpecificRequirements());
      _results.put(calcConfig.getName(), configResults);
    }
    if (includeMarketData) {
      _marketData = new MarketDataResults();
    } else {
      _marketData = null;
    }
  }

  public synchronized void store(final CompiledViewDefinition viewDefinition) {
    if (!_compiled) {
      for (final CompiledViewCalculationConfiguration calcConfig : viewDefinition.getCompiledCalculationConfigurations()) {
        final ConfigurationResults configResults = _results.get(calcConfig.getName());
        configResults.setRequirements(calcConfig.getTerminalOutputSpecifications());
        if (_marketData != null) {
          for (Map.Entry<ValueSpecification, Collection<ValueSpecification>> marketDataAlias : calcConfig.getMarketDataAliases().entrySet()) {
            _marketData.addMarketDataAlias(marketDataAlias.getKey(), marketDataAlias.getValue());
          }
        }
      }
      _compiled = true;
    }
  }

  public synchronized void store(LocalDate resultsDate, ViewComputationResultModel results) {
    assert _compiled;
    final int date = LocalDateToIntConverter.convertToInt(resultsDate);
    for (final ViewResultEntry viewResult : results.getAllResults()) {
      final ComputedValue computedValue = viewResult.getComputedValue();
      final Object value = computedValue.getValue();
      if ((value != null) && !(value instanceof MissingValue)) {
        final ConfigurationResults configResults = _results.get(viewResult.getCalculationConfiguration());
        configResults.store(date, computedValue.getSpecification(), value);
      }
    }
    if (_marketData != null) {
      for (final ComputedValue computedValue : results.getAllMarketData()) {
        final Object value = computedValue.getValue();
        if ((value != null) && !(value instanceof MissingValue)) {
          _marketData.store(date, computedValue.getSpecification(), value);
        }
      }
    }
  }

  public synchronized Map<String, HistoricalViewEvaluationResult> getResults() {
    final Map<String, HistoricalViewEvaluationResult> results = Maps.newHashMapWithExpectedSize(_results.size());
    for (final Map.Entry<String, ConfigurationResults> result : _results.entrySet()) {
      results.put(result.getKey(), result.getValue().makeResult());
    }
    return results;
  }

  public synchronized HistoricalViewEvaluationMarketData getMarketData() {
    if (_marketData == null) {
      return null;
    }
    return _marketData.makeResult();
  }

}
