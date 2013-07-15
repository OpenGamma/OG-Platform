/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.view.HistoricalViewEvaluationFunction;
import com.opengamma.financial.view.HistoricalViewEvaluationMarketData;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Uses the base market data values only to create the covariance matrix.
 */
public class MarketDataCovarianceMatrixFunction extends SampledCovarianceMatrixFunction {

  // SampledCovarianceMatrix

  @Override
  protected String getDataType() {
    return "MarketData";
  }

  @Override
  protected void addValueRequirements(final FunctionCompilationContext context, final Position target, final ViewCalculationConfiguration calcConfig) {
    calcConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.VALUE, ComputationTargetSpecification.of(target), ValueProperties.none()));
  }

  @Override
  protected Set<ValueRequirement> createRequirements(final ComputationTargetSpecification tempTargetSpec) {
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, tempTargetSpec, ValueProperties.with(HistoricalViewEvaluationFunction.MARKET_DATA_PROPERTY_NAME,
        HistoricalViewEvaluationFunction.MARKET_DATA_PROPERTY_VALUE).get()));
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalViewEvaluationMarketData marketData = (HistoricalViewEvaluationMarketData) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final ValueSpecification[] marketDataSpecs = marketData.getValueSpecifications().toArray(new ValueSpecification[marketData.getValueSpecifications().size()]);
    final LocalDateDoubleTimeSeries[] timeSeries = new LocalDateDoubleTimeSeries[marketDataSpecs.length];
    for (int i = 0; i < marketDataSpecs.length; i++) {
      timeSeries[i] = marketData.getDoubleTimeSeries(marketDataSpecs[i]);
    }
    final ValueRequirement desiredValueReq = desiredValues.iterator().next();
    final ValueSpecification desiredValueSpec = new ValueSpecification(ValueRequirementNames.COVARIANCE_MATRIX, target.toSpecification(), desiredValueReq.getConstraints());
    return Collections.singleton(new ComputedValue(desiredValueSpec, createCovarianceMatrix(timeSeries, marketDataSpecs)));
  }

}
