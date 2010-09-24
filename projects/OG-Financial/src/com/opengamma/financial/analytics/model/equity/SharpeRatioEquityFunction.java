/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Set;

import org.apache.commons.lang.Validate;

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
import com.opengamma.financial.riskreward.SharpeRatioCalculator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SharpeRatioEquityFunction extends AbstractFunction implements FunctionInvoker {
  private static final String RISK_FREE_RETURN_REFERENCE_TICKER = "US0003m Curncy";
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final SharpeRatioCalculator _calculator;

  //TODO consider schedule for market reference
  public SharpeRatioEquityFunction(final String returnCalculatorName, final String expectedReturnCalculatorName, final String standardDeviationCalculatorName) {
    Validate.notNull(returnCalculatorName, "return calculator name");
    Validate.notNull(expectedReturnCalculatorName, "expected excess return calculator name");
    Validate.notNull(standardDeviationCalculatorName, "standard deviation calculator name");
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    final Function<double[], Double> expectedExcessReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedReturnCalculatorName);
    final Function<double[], Double> standardDeviationCalculator = StatisticsCalculatorFactory.getCalculator(standardDeviationCalculatorName);
    _calculator = new SharpeRatioCalculator(new DoubleTimeSeriesStatisticsCalculator(expectedExcessReturnCalculator), new DoubleTimeSeriesStatisticsCalculator(standardDeviationCalculator));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalDataSource dataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> riskFreeTSObject = dataSource.getHistoricalData(IdentifierBundle.of(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER,
        RISK_FREE_RETURN_REFERENCE_TICKER)), "BLOOMBERG", null, "PX_LAST"); //TODO data provider etc should be passed in
    final Object assetTSObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()));
    if (riskFreeTSObject != null && assetTSObject != null) {
      final DoubleTimeSeries<?> assetPriceTS = (DoubleTimeSeries<?>) assetTSObject;
      DoubleTimeSeries<?> riskFreeRateTS = riskFreeTSObject.getSecond();
      DoubleTimeSeries<?> assetReturnTS = _returnCalculator.evaluate(assetPriceTS);
      assetReturnTS = assetReturnTS.intersectionFirstValue(riskFreeRateTS);
      riskFreeRateTS = riskFreeRateTS.intersectionFirstValue(assetReturnTS);
      final double ratio = _calculator.evaluate(assetReturnTS, riskFreeRateTS);
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, position), getUniqueIdentifier()), ratio));
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
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getSecurity()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, target.getPosition().getSecurity()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SharpeRatioFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
