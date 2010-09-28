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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.equity.CAPMFromRegressionCalculator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.IdentificationScheme;
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
public class CAPMFromRegressionModelEquityFunction extends AbstractFunction implements FunctionInvoker {
  private static final CAPMFromRegressionCalculator CAPM_REGRESSION_MODEL = new CAPMFromRegressionCalculator();
  private final LocalDate _startDate;

  public CAPMFromRegressionModelEquityFunction(final String startDate) {
    _startDate = LocalDate.parse(startDate);
  }

  //TODO need to have a schedule for the price series

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position)); //TODO replace with return series when portfolio weights are in
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, position));
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> marketTSObject = historicalDataSource.getHistoricalData(IdentifierBundle.of(Identifier.of(
        IdentificationScheme.BLOOMBERG_TICKER, bundle.getCAPMMarketName())), "BLOOMBERG", null, "PX_LAST", _startDate, now);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> riskFreeTSObject = historicalDataSource.getHistoricalData(IdentifierBundle.of(Identifier.of(
        IdentificationScheme.BLOOMBERG_TICKER, bundle.getCAPMRiskFreeRateName())), "BLOOMBERG", null, "PX_LAST", _startDate, now);
    if (marketTSObject != null && assetPnLObject != null && assetFairValueObject != null && riskFreeTSObject != null) {
      DoubleTimeSeries<?> marketTS = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT)
          .evaluate(marketTSObject.getSecond());
      final DoubleTimeSeries<?> riskFreeTS = (DoubleTimeSeries<?>) riskFreeTSObject.getSecond();
      marketTS = marketTS.subtract(riskFreeTS.divide(100));
      DoubleTimeSeries<?> assetTS = ((DoubleTimeSeries<?>) assetPnLObject).divide(position.getQuantity().doubleValue());
      assetTS = assetTS.intersectionFirstValue(marketTS);
      marketTS = marketTS.intersectionFirstValue(assetTS);
      final LeastSquaresRegressionResult regression = CAPM_REGRESSION_MODEL.evaluate(assetTS, marketTS);
      final double alpha = regression.getBetas()[0];
      final double alphaPValue = regression.getPValues()[0];
      final double alphaTStat = regression.getTStatistics()[0];
      final double alphaResidual = regression.getResiduals()[0];
      final double alphaStdError = regression.getStandardErrorOfBetas()[0];
      final Set<ComputedValue> result = new HashSet<ComputedValue>();
      //TODO need to find some way of getting {beta-label, beta-value} into the computed value and displayed
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, position), getUniqueIdentifier()),
          regression.getAdjustedRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, position), getUniqueIdentifier()), alpha));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, position), getUniqueIdentifier()),
          regression.getBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, position), getUniqueIdentifier()),
          regression.getMeanSquareError()));
      result
          .add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, position), getUniqueIdentifier()), alphaPValue));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, position), getUniqueIdentifier()), regression
          .getPValues()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, position), getUniqueIdentifier()), regression
          .getRSquared()));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, position), getUniqueIdentifier()),
          alphaResidual));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, position), getUniqueIdentifier()), regression
          .getResiduals()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, position), getUniqueIdentifier()),
          alphaStdError));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, position), getUniqueIdentifier()),
          regression.getStandardErrorOfBetas()[1]));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, position), getUniqueIdentifier()), alphaTStat));
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, position), getUniqueIdentifier()), regression
          .getTStatistics()[1]));
      return result;
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition()));
      requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, target.getPosition()));
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      final Position position = target.getPosition();
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, position), getUniqueIdentifier()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, position), getUniqueIdentifier()));
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "CAPM_RegressionEquityModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
