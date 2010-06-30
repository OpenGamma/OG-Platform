/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Set;

import javax.time.calendar.LocalDate;

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
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 */
public class PositionHistoricalVaRCalculatorFunction extends AbstractFunction implements FunctionInvoker{

  private static double CONFIDENCE_LEVEL = 0.99;
  private static double ONE_YEAR = 365.25;
  private static DoubleTimeSeriesStatisticsCalculator s_stdCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleStandardDeviationCalculator());
  private static DoubleTimeSeriesStatisticsCalculator s_meanCalculator = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Object pnlSeriesObj= inputs.getValue(ValueRequirementNames.PNL_SERIES);
    System.err.println("execute called");
    if (pnlSeriesObj instanceof DoubleTimeSeries<?>) {
      DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>)pnlSeriesObj;
      LocalDateDoubleTimeSeries pnlSeriesLD = pnlSeries.toLocalDateDoubleTimeSeries();
      if (!pnlSeriesLD.isEmpty()) {
        System.err.println("PnL Series not empty");
        LocalDate earliest = pnlSeriesLD.getEarliestTime();
        LocalDate latest = pnlSeriesLD.getLatestTime();
        long days = latest.toEpochDays() - earliest.toEpochDays();
        NormalLinearVaRCalculator s_varCalculator = new NormalLinearVaRCalculator(1, (252 * days) / ONE_YEAR, CONFIDENCE_LEVEL);
        NormalStatistics<DoubleTimeSeries<?>> normalStats = new NormalStatistics<DoubleTimeSeries<?>>(s_meanCalculator, s_stdCalculator, pnlSeries);
        double var = s_varCalculator.evaluate(normalStats);
        System.err.println("VaR="+var);
        return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target.getPosition())), var));
      }
    }
    return null;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) { System.err.println("canApplyTo returning true"); }
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    System.err.println("getRequirements");
    return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition()));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    System.err.println("getResults");
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, target.getPosition())));
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
