/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

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
import com.opengamma.financial.equity.capm.CAPMFromRegressionCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class CAPMFromRegressionModelFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double DAYS_IN_YEAR = 365.25;
  private static final CAPMFromRegressionCalculator CAPM_REGRESSION_MODEL = new CAPMFromRegressionCalculator();
  private final String _resolutionKey;

  public CAPMFromRegressionModelFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  //TODO need to have a schedule for the price series

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Object positionOrNode = getTarget(target);
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new OpenGammaRuntimeException("Could not get asset P&L series");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    if (assetFairValueObject == null) {
      throw new OpenGammaRuntimeException("Could not get asset fair value");
    }
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM")); //TODO 
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final HistoricalTimeSeries marketTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMMarket(), _resolutionKey, startDate, true, now, true);
    if (marketTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get market time series");
    }
    final HistoricalTimeSeries riskFreeTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMRiskFreeRate(), _resolutionKey, startDate, true, now, true);
    if (riskFreeTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get risk-free time series");
    }    
    final double fairValue = (Double) assetFairValueObject;
    final TimeSeriesReturnCalculator returnCalculator = getReturnCalculator(constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR));
    DoubleTimeSeries<?> marketReturn = returnCalculator.evaluate(marketTSObject.getTimeSeries());
    final DoubleTimeSeries<?> riskFreeTS = riskFreeTSObject.getTimeSeries().divide(100 * DAYS_IN_YEAR);
    marketReturn = marketReturn.subtract(riskFreeTS);
    DoubleTimeSeries<?> assetReturn = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    assetReturn = assetReturn.subtract(riskFreeTS);
    assetReturn = assetReturn.intersectionFirstValue(marketReturn);
    marketReturn = marketReturn.intersectionFirstValue(assetReturn);
    final LeastSquaresRegressionResult regression = CAPM_REGRESSION_MODEL.evaluate(assetReturn, marketReturn);
    final double alpha = regression.getBetas()[0];
    final double alphaPValue = regression.getPValues()[0];
    final double alphaTStat = regression.getTStatistics()[0];
    final double alphaResidual = regression.getResiduals()[0];
    final double alphaStdError = regression.getStandardErrorOfBetas()[0];
    final ValueProperties resultProperties = getResultProperties(desiredValues.iterator().next());
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    final String uniqueId = getUniqueId();
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode, resultProperties), uniqueId),
        regression.getAdjustedRSquared()));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode, resultProperties), uniqueId), alpha));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode, resultProperties), uniqueId), regression
        .getBetas()[1]));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode, resultProperties), uniqueId),
        regression.getMeanSquareError()));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode, resultProperties), uniqueId),
        alphaPValue));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode, resultProperties), uniqueId),
        regression.getPValues()[1]));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode, resultProperties), uniqueId), regression
        .getRSquared()));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode, resultProperties), uniqueId),
        alphaResidual));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode, resultProperties), uniqueId),
        regression.getResiduals()[1]));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode, resultProperties),
        uniqueId), alphaStdError));
    result.add(new ComputedValue(
        new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode, resultProperties), uniqueId), regression
            .getStandardErrorOfBetas()[1]));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode, resultProperties), uniqueId),
        alphaTStat));
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode, resultProperties), uniqueId),
        regression.getTStatistics()[1]));
    return result;
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
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      final Object positionOrNode = getTarget(target);
      final ValueProperties pnlSeriesProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next()).get();
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode, pnlSeriesProperties));
      requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final ValueProperties resultProperties = getResultProperties();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      final Object positionOrNode = getTarget(target);
      final String uniqueId = getUniqueId();
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode, resultProperties), uniqueId));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode, resultProperties), uniqueId));
      return results;
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
  
  private ValueProperties getResultProperties() {
    return createValueProperties()
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.RETURN_CALCULATOR).get();
  }
  
  private ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return createValueProperties()    
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .with(ValuePropertyNames.RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.RETURN_CALCULATOR)).get();
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
}
