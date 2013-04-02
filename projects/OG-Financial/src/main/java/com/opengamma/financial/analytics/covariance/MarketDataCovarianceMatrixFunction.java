/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
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
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.view.HistoricalViewEvaluationFunction;
import com.opengamma.financial.view.HistoricalViewEvaluationMarketData;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Uses the base market data values only to create the covariance matrix.
 */
public class MarketDataCovarianceMatrixFunction extends SampledCovarianceMatrixFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataCovarianceMatrixFunction.class);

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
  protected ViewCalculationConfiguration createViewCalculationConfiguration(final ViewDefinition viewDefinition, final String calcConfigName) {
    // TODO: Set the view execution options; we only want to capture the market data
    return super.createViewCalculationConfiguration(viewDefinition, calcConfigName);
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
    // Grab the value specifications as a list so that we iterate over them in a consistent order to construct the matrix
    final ValueSpecification[] marketDataSpecs = marketData.getValueSpecifications().toArray(new ValueSpecification[marketData.getValueSpecifications().size()]);
    final ExternalIdBundle[] labels = new ExternalIdBundle[marketDataSpecs.length];
    final double[][] values = new double[marketDataSpecs.length][marketDataSpecs.length];
    final Double[] keys = new Double[marketDataSpecs.length];
    final ComputationTargetResolver.AtVersionCorrection resolver = executionContext.getComputationTargetResolver();
    for (int i = 0; i < marketDataSpecs.length; i++) {
      keys[i] = 0d;
      // TODO: Do we need to do anything here with the resolved object, or identifier bundle? It would probably be better to leave the label as a value spec; unpacking an external id bundle is for demonstration
      final Object marketDataTarget = resolver.resolve(marketDataSpecs[i].getTargetSpecification()).getValue();
      if (marketDataTarget instanceof ExternalBundleIdentifiable) {
        labels[i] = ((ExternalBundleIdentifiable) marketDataTarget).getExternalIdBundle();
      } else if (marketDataTarget instanceof ExternalIdentifiable) {
        labels[i] = ((ExternalIdentifiable) marketDataTarget).getExternalId().toBundle();
      } else {
        labels[i] = ExternalIdBundle.EMPTY;
      }
      // TODO: The line below is just to give the matrix some information until the sub-classes implement their thing properly
      values[i][i] = 1d;
      final LocalDateDoubleTimeSeries timeSeries = marketData.getDoubleTimeSeries(marketDataSpecs[i]);
      s_logger.debug("{} = {}", marketDataSpecs[i], timeSeries);
      // TODO: Need to do something more useful with the time series
    }
    final ValueRequirement desiredValueReq = desiredValues.iterator().next();
    final ValueSpecification desiredValueSpec = new ValueSpecification(VALUE_NAME, target.toSpecification(), desiredValueReq.getConstraints());
    return Collections.singleton(new ComputedValue(desiredValueSpec, new DoubleLabelledMatrix2D(keys, labels, keys, labels, values)));
  }

}
