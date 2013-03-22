/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.capm.CAPMBetaCalculator;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesIntersector;

/**
 * 
 */
public class CAPMBetaModelFunction extends AbstractFunction.NonCompiledInvoker {

  private final ComputationTargetType _targetType;
  private final String _resolutionKey;

  public CAPMBetaModelFunction(final ComputationTargetType targetType, final String resolutionKey) {
    Validate.notNull(targetType, "target type");
    Validate.notNull(resolutionKey, "resolution key");
    _targetType = targetType;
    _resolutionKey = resolutionKey;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties resultProperties = getResultProperties(desiredValue);
    final ValueProperties constraints = desiredValue.getConstraints();
    final HistoricalTimeSeries marketTSObject = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final DoubleTimeSeries<?> assetPnL = (DoubleTimeSeries<?>) inputs.getValue(ValueRequirementNames.PNL_SERIES);
    final double fairValue = (Double) inputs.getValue(ValueRequirementNames.FAIR_VALUE);
    final TimeSeriesReturnCalculator returnCalculator = getReturnCalculator(constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR));
    DoubleTimeSeries<?> marketReturn = returnCalculator.evaluate(marketTSObject.getTimeSeries());
    DoubleTimeSeries<?> assetReturn = assetPnL.divide(fairValue);
    DoubleTimeSeries<?>[] series = TimeSeriesIntersector.intersect(assetReturn, marketReturn);
    assetReturn = series[0];
    marketReturn = series[1];
    final CAPMBetaCalculator calculator = getBetaCalculator(constraints.getValues(ValuePropertyNames.COVARIANCE_CALCULATOR),
        constraints.getValues(ValuePropertyNames.VARIANCE_CALCULATOR));
    final double beta = calculator.evaluate(assetReturn, marketReturn);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.CAPM_BETA, target.toSpecification(), resultProperties), beta));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodNames = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodNames == null || samplingPeriodNames.size() != 1) {
      return null;
    }
    final String samplingPeriodName = samplingPeriodNames.iterator().next();
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
    final ValueRequirement pnlSeriesRequirement = new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next()).get());
    final ValueRequirement fairValueRequirement = new ValueRequirement(ValueRequirementNames.FAIR_VALUE, target.toSpecification());
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM")); //TODO country-specific
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(bundle.getCAPMMarket(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    final ValueRequirement timeSeriesRequirement = HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries,
        MarketDataRequirementNames.MARKET_VALUE, DateConstraint.VALUATION_TIME.minus(samplingPeriodName), true, DateConstraint.VALUATION_TIME, true);
    return ImmutableSet.of(pnlSeriesRequirement, fairValueRequirement, timeSeriesRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CAPM_BETA, target.toSpecification(), getResultProperties()));
  }

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
