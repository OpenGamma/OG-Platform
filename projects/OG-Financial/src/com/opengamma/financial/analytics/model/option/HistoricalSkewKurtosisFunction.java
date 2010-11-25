/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

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
    final ZonedDateTime now = executionContext.getSnapshotClock().zonedDateTime();
    final Security security = target.getSecurity();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsObject = historicalDataSource.getHistoricalData(security.getIdentifiers(), _dataSource, _dataProvider, _field, _startDate, true, now
        .toLocalDate(), false);
    if (tsObject == null) {
      throw new NullPointerException("Could not get time series for " + security.getIdentifiers());
    }
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(tsObject.getSecond());
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
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY), getUniqueIdentifier()), skew));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY), getUniqueIdentifier()), pearson));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY), getUniqueIdentifier()), fisher));
    return results;
  }

  @Override
  public String getShortName() {
    return "HistoricalSkewKurtosisFunction";
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement>emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final UniqueIdentifier uid = target.getSecurity().getUniqueIdentifier();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY, uid), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueIdentifier()));
      return results;
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
