/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 *
 */
public class HistoricalSkewKurtosisFunction extends AbstractFunction.NonCompiledInvoker {

  private final TimeSeriesReturnCalculator _returnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _skewCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _kurtosisCalculator;
  private final boolean _isPearson;
  private final LocalDate _startDate;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _field;

  public HistoricalSkewKurtosisFunction(final String returnCalculatorName, final String skewCalculatorName, final String kurtosisCalculatorName, final String isPearson, final String startDate,
      final String dataSource, final String dataProvider, final String field) {
    Validate.notNull(returnCalculatorName, "return calculator name");
    Validate.notNull(skewCalculatorName, "skew calculator name");
    Validate.notNull(kurtosisCalculatorName, "kurtosis calculator name");
    Validate.notNull(startDate, "start date");
    Validate.notNull(dataSource, "data source");
    Validate.notNull(field, "field");
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    _skewCalculator = new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(skewCalculatorName));
    _kurtosisCalculator = new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(kurtosisCalculatorName));
    _isPearson = Boolean.parseBoolean(isPearson);
    _startDate = LocalDate.parse(startDate);
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _field = field;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeries tsObject = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(tsObject.getTimeSeries());
    final double skew = _skewCalculator.evaluate(returnTS);
    final double kurtosis = _kurtosisCalculator.evaluate(returnTS);
    double pearson, fisher;
    if (_isPearson) {
      pearson = kurtosis;
      fisher = kurtosis - 3;
    } else {
      fisher = kurtosis;
      pearson = fisher + 3;
    }
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY), getUniqueId()), skew));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY), getUniqueId()), pearson));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY), getUniqueId()), fisher));
    return results;
  }

  @Override
  public String getShortName() {
    return "HistoricalSkewKurtosisFunction";
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(target.getSecurity().getExternalIdBundle(), null, _dataSource, _dataProvider, _field, null);
    if (timeSeries == null) {
      return null;
    }
    return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(), DateConstraint.of(_startDate), true,
        DateConstraint.VALUATION_TIME, true));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final UniqueId uid = target.getSecurity().getUniqueId();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY, uid), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()));
      return results;
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
