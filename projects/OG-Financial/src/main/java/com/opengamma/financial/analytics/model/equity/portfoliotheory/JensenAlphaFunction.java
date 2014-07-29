/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.riskreward.JensenAlphaCalculator;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
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
public class JensenAlphaFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ComputationTargetType TYPE = ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION);
  private static final double DAYS_PER_YEAR = 365.25; //TODO
  private final String _resolutionKey;

  public JensenAlphaFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final HistoricalTimeSeries marketTS = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, bundle.getCAPMMarket());
    if (marketTS == null) {
      throw new OpenGammaRuntimeException("Market value series was not availble");
    }
    final HistoricalTimeSeries riskFreeRateTS = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, bundle.getCAPMRiskFreeRate());
    if (riskFreeRateTS == null) {
      throw new OpenGammaRuntimeException("Risk free rate series was not available");
    }
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new OpenGammaRuntimeException("Asset P&L was null");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, targetSpec));
    if (assetFairValueObject == null) {
      throw new OpenGammaRuntimeException("Asset fair value was null");
    }
    final Object betaObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CAPM_BETA, targetSpec));
    if (betaObject == null) {
      throw new OpenGammaRuntimeException("Beta was null");
    }
    final double beta = (Double) betaObject;
    final double fairValue = (Double) assetFairValueObject;
    DoubleTimeSeries<?> assetReturnTS = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    DoubleTimeSeries<?> riskFreeReturnTS = riskFreeRateTS.getTimeSeries().divide(100 * DAYS_PER_YEAR);
    final TimeSeriesReturnCalculator returnCalculator = getReturnCalculator(constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR));
    DoubleTimeSeries<?> marketReturnTS = returnCalculator.evaluate(marketTS.getTimeSeries());
    final DoubleTimeSeries<?>[] series = TimeSeriesIntersector.intersect(assetReturnTS, riskFreeReturnTS, marketReturnTS);
    assetReturnTS = series[0];
    riskFreeReturnTS = series[1];
    marketReturnTS = series[2];
    final JensenAlphaCalculator calculator = getCalculator(constraints.getValues(ValuePropertyNames.EXCESS_RETURN_CALCULATOR));
    final double alpha = calculator.evaluate(assetReturnTS, riskFreeReturnTS, beta, marketReturnTS);
    final ValueProperties resultProperties = getResultProperties(desiredValues.iterator().next());
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.JENSENS_ALPHA, targetSpec, resultProperties), alpha));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodNames = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodNames == null || samplingPeriodNames.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculatorNames = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionNames = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctionNames == null || samplingFunctionNames.size() != 1) {
      return null;
    }
    final Set<String> returnCalculatorNames = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnCalculatorNames == null || returnCalculatorNames.size() != 1) {
      return null;
    }
    final Set<String> stdDevCalculatorNames = constraints.getValues(ValuePropertyNames.STD_DEV_CALCULATOR);
    if (stdDevCalculatorNames == null || stdDevCalculatorNames.size() != 1) {
      return null;
    }
    final Set<String> covarianceCalculatorNames = constraints.getValues(ValuePropertyNames.COVARIANCE_CALCULATOR);
    if (covarianceCalculatorNames == null || covarianceCalculatorNames.size() != 1) {
      return null;
    }
    final Set<String> varianceCalculatorNames = constraints.getValues(ValuePropertyNames.VARIANCE_CALCULATOR);
    if (varianceCalculatorNames == null || varianceCalculatorNames.size() != 1) {
      return null;
    }
    final String samplingPeriodName = samplingPeriodNames.iterator().next();
    final String scheduleCalculatorName = scheduleCalculatorNames.iterator().next();
    final String samplingFunctionName = samplingFunctionNames.iterator().next();
    final String returnCalculatorName = returnCalculatorNames.iterator().next();
    final ValueProperties pnlSeriesProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName)
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName).get();
    final ValueProperties betaProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName)
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName)
        .with(ValuePropertyNames.COVARIANCE_CALCULATOR, covarianceCalculatorNames.iterator().next())
        .with(ValuePropertyNames.VARIANCE_CALCULATOR, varianceCalculatorNames.iterator().next()).get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    result.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, pnlSeriesProperties));
    result.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, targetSpec));
    result.add(new ValueRequirement(ValueRequirementNames.CAPM_BETA, targetSpec, betaProperties));
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final DateConstraint startDate = DateConstraint.VALUATION_TIME.minus(samplingPeriodName);
    HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(bundle.getCAPMMarket(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    result.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE, startDate, true,
        DateConstraint.VALUATION_TIME, true));
    timeSeries = resolver.resolve(bundle.getCAPMRiskFreeRate(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    result.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE, startDate, true,
        DateConstraint.VALUATION_TIME, true));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.JENSENS_ALPHA, target.toSpecification(), getResultProperties()));
  }

  private ValueProperties getResultProperties() {
    return createValueProperties()
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
        .withAny(ValuePropertyNames.EXCESS_RETURN_CALCULATOR)
        .withAny(ValuePropertyNames.COVARIANCE_CALCULATOR)
        .withAny(ValuePropertyNames.VARIANCE_CALCULATOR).get();
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return createValueProperties()
        .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
        .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
        .with(ValuePropertyNames.RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.RETURN_CALCULATOR))
        .with(ValuePropertyNames.STD_DEV_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.STD_DEV_CALCULATOR))
        .with(ValuePropertyNames.EXCESS_RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.EXCESS_RETURN_CALCULATOR))
        .with(ValuePropertyNames.COVARIANCE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.COVARIANCE_CALCULATOR))
        .with(ValuePropertyNames.VARIANCE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.VARIANCE_CALCULATOR)).get();
  }

  private TimeSeriesReturnCalculator getReturnCalculator(final Set<String> returnCalculatorNames) {
    if (returnCalculatorNames == null || returnCalculatorNames.isEmpty() || returnCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + returnCalculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorNames.iterator().next());
  }

  private JensenAlphaCalculator getCalculator(final Set<String> excessReturnCalculatorNames) {
    if (excessReturnCalculatorNames == null || excessReturnCalculatorNames.isEmpty() || excessReturnCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique excess return calculator name: " + excessReturnCalculatorNames);
    }
    final Function<double[], Double> expectedExcessReturnCalculator = StatisticsCalculatorFactory.getCalculator(excessReturnCalculatorNames.iterator().next());
    final DoubleTimeSeriesStatisticsCalculator excessReturnCalculator = new DoubleTimeSeriesStatisticsCalculator(expectedExcessReturnCalculator);
    return new JensenAlphaCalculator(excessReturnCalculator, excessReturnCalculator, excessReturnCalculator); //TODO check that they can both be the same
  }
}
