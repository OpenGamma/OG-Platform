/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 *
 *
 */
@Deprecated
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
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.SKEW, target.toSpecification(), createValueProperties().get()), skew));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PEARSON_KURTOSIS, target.toSpecification(), createValueProperties().get()), pearson));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.FISHER_KURTOSIS, target.toSpecification(), createValueProperties().get()), fisher));
    return results;
  }

  @Override
  public String getShortName() {
    return "HistoricalSkewKurtosisFunction";
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(target.getSecurity().getExternalIdBundle(), null, _dataSource, _dataProvider, _field, null);
    if (timeSeries == null) {
      return null;
    }
    return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, _field, DateConstraint.of(_startDate), true, DateConstraint.VALUATION_TIME, true));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties().get();
    results.add(new ValueSpecification(ValueRequirementNames.SKEW, targetSpec, properties));
    results.add(new ValueSpecification(ValueRequirementNames.PEARSON_KURTOSIS, targetSpec, properties));
    results.add(new ValueSpecification(ValueRequirementNames.FISHER_KURTOSIS, targetSpec, properties));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
