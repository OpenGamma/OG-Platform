/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.NormalLinearVaRCalculator;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 *
 */
public class PositionHistoricalVaRCalculatorFunction extends AbstractFunction.NonCompiledInvoker {
  private final DoubleTimeSeriesStatisticsCalculator _stdCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _meanCalculator;
  private final NormalLinearVaRCalculator<DoubleTimeSeries<?>> _varCalculator;

  public PositionHistoricalVaRCalculatorFunction(final String meanCalculatorName, final String standardDeviationCalculatorName, final String confidenceLevel) {
    final Function<double[], Double> meanCalculator = StatisticsCalculatorFactory.getCalculator(meanCalculatorName);
    final Function<double[], Double> stdCalculator = StatisticsCalculatorFactory.getCalculator(standardDeviationCalculatorName);
    _meanCalculator = new DoubleTimeSeriesStatisticsCalculator(meanCalculator);
    _stdCalculator = new DoubleTimeSeriesStatisticsCalculator(stdCalculator);
    _varCalculator = new NormalLinearVaRCalculator<DoubleTimeSeries<?>>(1, 1, Double.valueOf(confidenceLevel), _meanCalculator, _stdCalculator); //TODO see note in portfolio VaR function
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    
    String currency = getCurrency(inputs);
    
    final Object pnlSeriesObj = inputs.getValue(ValueRequirementNames.PNL_SERIES);
    if (pnlSeriesObj == null) {
      throw new NullPointerException("Could not get P&L series for " + target.getPosition());
    }
    final DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>) pnlSeriesObj;
    if (!pnlSeries.isEmpty()) {
      final double var = _varCalculator.evaluate(pnlSeries);
      ValueRequirement vr = new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target.getPosition(), createValueProperties().with(ValuePropertyNames.CURRENCY, currency).get());
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(vr, getUniqueId()), var));
    }
    return null;
  }

  private String getCurrency(final FunctionInputs inputs) {
    String currency = null;
    for (ComputedValue value : inputs.getAllValues()) {
      currency = value.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      if (currency != null) {
        break;
      }
    }
    return currency;
  }

  private String getCurrency(Map<ValueSpecification, ValueRequirement> inputs) {
    String currency = null;
    for (Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      currency = entry.getKey().getProperty(ValuePropertyNames.CURRENCY);
      if (currency != null) {
        break;
      }
    }
    return currency;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target
          .getPosition(), createValueProperties().withAny(ValuePropertyNames.CURRENCY).get()), getUniqueId()));
    }
    return null;
  }
  

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (canApplyTo(context, target)) {
      String currency = getCurrency(inputs);
      if (currency == null) {
        return null;
      }
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target
          .getPosition(), createValueProperties().with(ValuePropertyNames.CURRENCY, currency).get()), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PositionHistoricalVaRCalculatorFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
