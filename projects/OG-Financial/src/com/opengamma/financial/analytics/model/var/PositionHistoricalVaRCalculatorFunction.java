/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.NormalLinearVaRCalculator;
import com.opengamma.financial.var.NormalStatistics;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 */
public class PositionHistoricalVaRCalculatorFunction extends AbstractFunction implements FunctionInvoker {
  private final DoubleTimeSeriesStatisticsCalculator _stdCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _meanCalculator;
  private final double _confidenceLevel;

  public PositionHistoricalVaRCalculatorFunction(final String meanCalculatorName, final String standardDeviationCalculatorName, final String confidenceLevel) {
    final Function<double[], Double> meanCalculator = StatisticsCalculatorFactory.getCalculator(meanCalculatorName);
    final Function<double[], Double> stdCalculator = StatisticsCalculatorFactory.getCalculator(standardDeviationCalculatorName);
    _meanCalculator = new DoubleTimeSeriesStatisticsCalculator(meanCalculator);
    _stdCalculator = new DoubleTimeSeriesStatisticsCalculator(stdCalculator);
    _confidenceLevel = Double.valueOf(confidenceLevel);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object pnlSeriesObj = inputs.getValue(ValueRequirementNames.PNL_SERIES);
    if (pnlSeriesObj instanceof DoubleTimeSeries<?>) {
      final DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>) pnlSeriesObj;
      final LocalDateDoubleTimeSeries pnlSeriesLD = pnlSeries.toLocalDateDoubleTimeSeries();
      if (!pnlSeriesLD.isEmpty()) {
        final NormalLinearVaRCalculator varCalculator = new NormalLinearVaRCalculator(1, 1, _confidenceLevel); //TODO see note in portfolio VaR function
        final NormalStatistics<DoubleTimeSeries<?>> normalStats = new NormalStatistics<DoubleTimeSeries<?>>(_meanCalculator, _stdCalculator, pnlSeries);
        final double var = varCalculator.evaluate(normalStats);
        return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target.getPosition()), getUniqueIdentifier()), var));
      }
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target.getPosition()), getUniqueIdentifier()));
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
