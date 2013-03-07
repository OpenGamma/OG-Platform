/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Runs an execution sequence over a series of historical dates, returning the results as time-series.
 */
public class HistoricalViewEvaluationFunction extends ViewEvaluationFunction<HistoricalViewEvaluationTarget, HistoricalViewEvaluationResultBuilder> {

  public HistoricalViewEvaluationFunction() {
    super(ValueRequirementNames.HISTORICAL_TIME_SERIES, HistoricalViewEvaluationTarget.class);
  }
  
  @Override
  protected ViewCycleExecutionOptions getDefaultCycleOptions(FunctionExecutionContext context) {
    return ViewCycleExecutionOptions.builder().setValuationTime(context.getValuationTime()).create();
  }
  
  @Override
  protected HistoricalViewEvaluationResultBuilder createResultBuilder(ViewEvaluationTarget target) {
    return new HistoricalViewEvaluationResultBuilder(target.getViewDefinition());
  }

  @Override
  protected void store(ViewComputationResultModel results, HistoricalViewEvaluationResultBuilder resultBuilder) {
    resultBuilder.store(getResultsDate(results.getViewCycleExecutionOptions()), results);
  }

  @Override
  protected void store(CompiledViewDefinition compiledViewDefinition, HistoricalViewEvaluationResultBuilder resultBuilder) {
    resultBuilder.store(compiledViewDefinition);
  }

  @Override
  protected Set<ComputedValue> buildResults(ComputationTarget target, HistoricalViewEvaluationResultBuilder resultBuilder) {
    final Map<String, HistoricalViewEvaluationResult> viewResults = resultBuilder.getResults();
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(viewResults.size());
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final Map.Entry<String, HistoricalViewEvaluationResult> viewResult : viewResults.entrySet()) {
      String calcConfigName = viewResult.getKey();
      HistoricalViewEvaluationResult value = viewResult.getValue();
      results.add(new ComputedValue(getResultSpec(calcConfigName, targetSpec), value));
    }
    return results;
  }
  
  //-------------------------------------------------------------------------
  private LocalDate getResultsDate(ViewCycleExecutionOptions cycleExecutionOptions) {
    // NOTE jonathan 2013-02-28 -- could imagine using constraints
    List<MarketDataSpecification> marketDataSpecifications = cycleExecutionOptions.getMarketDataSpecifications();
    if (marketDataSpecifications.size() != 1) {
      throw new OpenGammaRuntimeException("Expected cycle execution options to contain exactly 1 market data specification but found " +
          marketDataSpecifications.size() + ": " + cycleExecutionOptions);
    }
    MarketDataSpecification marketDataSpec = marketDataSpecifications.get(0);
    if (!(marketDataSpec instanceof FixedHistoricalMarketDataSpecification)) {
      throw new OpenGammaRuntimeException("Unsupported market data specification: " + marketDataSpec);
    }
    return ((FixedHistoricalMarketDataSpecification) marketDataSpec).getSnapshotDate();
  }

  
}
