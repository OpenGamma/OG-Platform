/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Set;

import com.google.common.collect.Sets;
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
import com.opengamma.financial.equity.CAPMBetaCalculator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.statistics.descriptive.SampleCovarianceCalculator;
import com.opengamma.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class CAPMBetaModelEquityFunction extends AbstractFunction implements FunctionInvoker {
  private final CAPMBetaCalculator _model;
  private static final DoubleTimeSeriesStatisticsCalculator COVARIANCE_CALCULATOR = new DoubleTimeSeriesStatisticsCalculator(new SampleCovarianceCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator VARIANCE_CALCULATOR = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator());
  private static final String MARKET_REFERENCE_TICKER = "IBM US Equity"; //TODO should not be hard-coded

  //TODO pass in covariance and variance calculator names
  //TODO need to use schedule for price series
  public CAPMBetaModelEquityFunction(final String returnCalculatorName) {
    _model = new CAPMBetaCalculator(TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName), COVARIANCE_CALCULATOR, VARIANCE_CALCULATOR);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> marketTSObject = historicalDataSource.getHistoricalData(IdentifierBundle.of(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER,
        MARKET_REFERENCE_TICKER)), "BLOOMBERG", null, "PX_LAST");
    final Object assetTSObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()));
    if (marketTSObject != null && assetTSObject != null) {
      DoubleTimeSeries<?> marketTS = marketTSObject.getSecond();
      DoubleTimeSeries<?> assetTS = (DoubleTimeSeries<?>) assetTSObject;
      assetTS = assetTS.intersectionFirstValue(marketTS);
      marketTS = marketTS.intersectionFirstValue(assetTS);
      final double beta = _model.evaluate(assetTS, marketTS);
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_BETA, position), getUniqueIdentifier()), beta));
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
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CAPM_BETA, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "CAPM_BetaEquityModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
