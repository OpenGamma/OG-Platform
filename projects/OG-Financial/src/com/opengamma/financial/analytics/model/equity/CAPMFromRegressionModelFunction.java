/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.equity.capm.CAPMFromRegressionCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class CAPMFromRegressionModelFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double DAYS_IN_YEAR = 365.25;
  private static final CAPMFromRegressionCalculator CAPM_REGRESSION_MODEL = new CAPMFromRegressionCalculator();
  private final LocalDate _startDate;

  public CAPMFromRegressionModelFunction(final String startDate) {
    Validate.notNull(startDate, "start date");
    _startDate = LocalDate.parse(startDate);
  }

  //TODO need to have a schedule for the price series

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Object positionOrNode = getTarget(target);
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode)); //TODO replace with return series when portfolio weights are in
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final HistoricalTimeSeries marketTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMMarket(), null, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, _startDate, true, now, false);
    final HistoricalTimeSeries riskFreeTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMRiskFreeRate(), null, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, _startDate, true, now, false);
    if (marketTSObject != null && assetPnLObject != null && assetFairValueObject != null && riskFreeTSObject != null) {
      final double fairValue = (Double) assetFairValueObject;
      DoubleTimeSeries<?> marketReturn = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT).evaluate(
          marketTSObject.getTimeSeries());
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
      final Set<ComputedValue> result = new HashSet<ComputedValue>();
      //TODO need to find some way of getting {beta-label, beta-value} into the computed value and displayed
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode), getUniqueId()),
          regression.getAdjustedRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode), getUniqueId()), alpha));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode), getUniqueId()), regression
          .getBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode), getUniqueId()),
          regression.getMeanSquareError()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode), getUniqueId()),
          alphaPValue));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode), getUniqueId()),
          regression.getPValues()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode), getUniqueId()), regression
          .getRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode), getUniqueId()),
          alphaResidual));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode), getUniqueId()),
          regression.getResiduals()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode),
          getUniqueId()), alphaStdError));
      result.add(new ComputedValue(
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode), getUniqueId()), regression
              .getStandardErrorOfBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode), getUniqueId()),
          alphaTStat));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode), getUniqueId()),
          regression.getTStatistics()[1]));
      return result;
    }
    return null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      final Object positionOrNode = getTarget(target);
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode));
      requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      final Object positionOrNode = getTarget(target);
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode), getUniqueId()));
      return results;
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
}
