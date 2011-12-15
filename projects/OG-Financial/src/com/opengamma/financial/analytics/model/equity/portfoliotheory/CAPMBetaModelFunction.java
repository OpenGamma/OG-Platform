/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.equity.capm.CAPMBetaCalculator;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesIntersector;

/**
 * 
 */
public abstract class CAPMBetaModelFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _resolutionKey;

  public CAPMBetaModelFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Object positionOrNode = getTarget(target);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM")); //TODO country-specific
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties resultProperties = getResultProperties(desiredValue);
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueProperties constraints = desiredValue.getConstraints();
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final HistoricalTimeSeries marketTSObject = historicalSource.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMMarket(), _resolutionKey, startDate, true, now, true);
    if (marketTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get market time series");
    }
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode));
    if (assetPnLObject == null) {
      throw new OpenGammaRuntimeException("Could not get P&L series");
    }    
    final Object fairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    if (fairValueObject == null) {
      throw new OpenGammaRuntimeException("Could not get fair value");
    }
    final double fairValue = (Double) fairValueObject;
    final TimeSeriesReturnCalculator returnCalculator = getReturnCalculator(constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR));
    DoubleTimeSeries<?> marketReturn = returnCalculator.evaluate(marketTSObject.getTimeSeries());
    DoubleTimeSeries<?> assetReturn = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    DoubleTimeSeries<?>[] series = TimeSeriesIntersector.intersect(assetReturn, marketReturn);
    assetReturn = series[0];
    marketReturn = series[1];    
    final CAPMBetaCalculator calculator = getBetaCalculator(constraints.getValues(ValuePropertyNames.COVARIANCE_CALCULATOR),
        constraints.getValues(ValuePropertyNames.VARIANCE_CALCULATOR));
    final double beta = calculator.evaluate(assetReturn, marketReturn);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_BETA, positionOrNode, resultProperties), getUniqueId()), beta));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> samplingPeriodName = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
      if (samplingPeriodName == null || samplingPeriodName.size() != 1) {
        return null;
      }
      final Set<String> scheduleCalculatorName = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
      if (scheduleCalculatorName == null || scheduleCalculatorName.size() != 1) {
        return null;
      }
      final Set<String> samplingFunctionName = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
      if (samplingFunctionName == null || samplingFunctionName.size() != 1) {
        return null;
      }
      final Set<String> returnCalculatorName = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
      if (returnCalculatorName == null || returnCalculatorName.size() != 1) {
        return null;
      }
      final ValueProperties pnlSeriesProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next()).get();
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode, pnlSeriesProperties), 
                             new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_BETA, getTarget(target), getResultProperties()), getUniqueId()));
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);

  private ValueProperties getResultProperties() {
    return createValueProperties()
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.RETURN_CALCULATOR)
      .withAny(ValuePropertyNames.COVARIANCE_CALCULATOR)
      .withAny(ValuePropertyNames.VARIANCE_CALCULATOR).get();
  }
  
  private ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return createValueProperties()
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .with(ValuePropertyNames.RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.RETURN_CALCULATOR))
      .with(ValuePropertyNames.COVARIANCE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.COVARIANCE_CALCULATOR))
      .with(ValuePropertyNames.VARIANCE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.VARIANCE_CALCULATOR)).get();
  }
  
  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    if (samplingPeriodNames == null || samplingPeriodNames.isEmpty() || samplingPeriodNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling period name: " + samplingPeriodNames);
    }  
    return Period.parse(samplingPeriodNames.iterator().next());
  }
 
  private TimeSeriesReturnCalculator getReturnCalculator(final Set<String> returnCalculatorNames) {
    if (returnCalculatorNames == null || returnCalculatorNames.isEmpty() || returnCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + returnCalculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorNames.iterator().next());
  }
  
  private CAPMBetaCalculator getBetaCalculator(final Set<String> covarianceCalculatorNames, final Set<String> varianceCalculatorNames) {
    if (covarianceCalculatorNames == null || covarianceCalculatorNames.isEmpty() || covarianceCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique covariance calculator name: " + covarianceCalculatorNames);
    }
    if (varianceCalculatorNames == null || varianceCalculatorNames.isEmpty() || varianceCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique variance calculator name: " + varianceCalculatorNames);
    }
    final DoubleTimeSeriesStatisticsCalculator covarianceCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(covarianceCalculatorNames.iterator().next()));
    final DoubleTimeSeriesStatisticsCalculator varianceCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(varianceCalculatorNames.iterator().next()));
    return new CAPMBetaCalculator(covarianceCalculator, varianceCalculator);
  }
}
