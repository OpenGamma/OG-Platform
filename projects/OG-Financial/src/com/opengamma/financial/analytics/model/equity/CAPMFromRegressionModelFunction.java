/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.security.SecurityUtils;
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
import com.opengamma.financial.equity.CAPMFromRegressionCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

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
    final ConventionBundle bundle = conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> marketTSObject = historicalDataSource.getHistoricalData(IdentifierBundle.of(
        SecurityUtils.bloombergTickerSecurityId(bundle.getCAPMMarketName())), "BLOOMBERG", null, "PX_LAST", _startDate, true, now, false);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> riskFreeTSObject = historicalDataSource.getHistoricalData(IdentifierBundle.of(
        SecurityUtils.bloombergTickerSecurityId(bundle.getCAPMRiskFreeRateName())), "BLOOMBERG", "CMPL", "PX_LAST", _startDate, true, now, false);
    if (marketTSObject != null && assetPnLObject != null && assetFairValueObject != null && riskFreeTSObject != null) {
      final double fairValue = (Double) assetFairValueObject;
      DoubleTimeSeries<?> marketReturn = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT).evaluate(
          marketTSObject.getSecond());
      final DoubleTimeSeries<?> riskFreeTS = riskFreeTSObject.getSecond().divide(100 * DAYS_IN_YEAR);
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
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode), getUniqueIdentifier()),
          regression.getAdjustedRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode), getUniqueIdentifier()), alpha));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode), getUniqueIdentifier()), regression
          .getBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode), getUniqueIdentifier()),
          regression.getMeanSquareError()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode), getUniqueIdentifier()),
          alphaPValue));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode), getUniqueIdentifier()),
          regression.getPValues()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode), getUniqueIdentifier()), regression
          .getRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode), getUniqueIdentifier()),
          alphaResidual));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode), getUniqueIdentifier()),
          regression.getResiduals()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode),
          getUniqueIdentifier()), alphaStdError));
      result.add(new ComputedValue(
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode), getUniqueIdentifier()), regression
              .getStandardErrorOfBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode), getUniqueIdentifier()),
          alphaTStat));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode), getUniqueIdentifier()),
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
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, positionOrNode), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, positionOrNode), getUniqueIdentifier()));
      return results;
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
}
