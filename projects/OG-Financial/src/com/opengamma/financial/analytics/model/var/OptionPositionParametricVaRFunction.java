/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTimeSeriesProvider;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.covariance.CovarianceCalculator;
import com.opengamma.financial.covariance.CovarianceMatrixCalculator;
import com.opengamma.financial.covariance.HistoricalCovarianceCalculator;
import com.opengamma.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.financial.var.NormalLinearVaRCalculator;
import com.opengamma.financial.var.parametric.DeltaCovarianceMatrixStandardDeviationCalculator;
import com.opengamma.financial.var.parametric.DeltaMeanCalculator;
import com.opengamma.financial.var.parametric.ParametricVaRDataBundle;
import com.opengamma.financial.var.parametric.VaRCovarianceMatrixCalculator;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class OptionPositionParametricVaRFunction {/*extends AbstractFunction.NonCompiledInvoker {
  private final String _resolutionKey;
  private final LocalDate _startDate;
  private final Set<ValueGreek> _valueGreeks;
  private final Set<String> _valueGreekRequirementNames;
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final Schedule _scheduleCalculator;
  private final TimeSeriesSamplingFunction _samplingCalculator;
  private final int _maxOrder;
  //TODO none of this should be hard-coded
  private final NormalLinearVaRCalculator<Map<Integer, ParametricVaRDataBundle>> _normalVaRCalculator;
  private final VaRCovarianceMatrixCalculator _covarianceMatrixCalculator;
  private final MatrixAlgebra _algebra = new ColtMatrixAlgebra();
  private final DeltaMeanCalculator _meanCalculator = new DeltaMeanCalculator(_algebra);
  private final DeltaCovarianceMatrixStandardDeviationCalculator _stdCalculator = new DeltaCovarianceMatrixStandardDeviationCalculator(_algebra);

  public OptionPositionParametricVaRFunction(final String resolutionKey, final String startDate, final String returnCalculatorName,
      final String scheduleName, final String samplingFunctionName, final String confidenceLevel, final String maxOrder,
      final String valueGreekRequirementNames) {
    this(resolutionKey, startDate, returnCalculatorName, scheduleName, samplingFunctionName, confidenceLevel, maxOrder,
        new String[] {valueGreekRequirementNames});
  }

  public OptionPositionParametricVaRFunction(final String resolutionKey, final String startDate, final String returnCalculatorName,
      final String scheduleName, final String samplingFunctionName, final String confidenceLevel, final String maxOrder,
      final String... valueGreekRequirementNames) {
    Validate.notNull(resolutionKey, "resolution key");
    Validate.notNull(startDate, "start date");
    _resolutionKey = resolutionKey;
    _startDate = LocalDate.parse(startDate);
    _valueGreeks = new HashSet<ValueGreek>();
    _valueGreekRequirementNames = new HashSet<String>();
    for (final String valueGreekRequirementName : valueGreekRequirementNames) {
      _valueGreekRequirementNames.add(valueGreekRequirementName);
      _valueGreeks.add(AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName));
    }
    _maxOrder = Integer.parseInt(maxOrder);
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    final CovarianceCalculator covarianceCalculator = new HistoricalCovarianceCalculator();
    _covarianceMatrixCalculator = new VaRCovarianceMatrixCalculator(new CovarianceMatrixCalculator(covarianceCalculator));
    _scheduleCalculator = ScheduleCalculatorFactory.getScheduleCalculator(scheduleName);
    _samplingCalculator = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
    _normalVaRCalculator = new NormalLinearVaRCalculator<Map<Integer, ParametricVaRDataBundle>>(1, 1, Double.valueOf(confidenceLevel), _meanCalculator, _stdCalculator); //TODO
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final SecuritySource securitySource = executionContext.getSecuritySource();
    final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PARAMETRIC_VAR, position), getUniqueId());
    final SensitivityAndReturnDataBundle[] dataBundleArray = new SensitivityAndReturnDataBundle[_valueGreekRequirementNames.size()];
    int i = 0;
    for (final String valueGreekRequirementName : _valueGreekRequirementNames) {
      final Object valueObj = inputs.getValue(valueGreekRequirementName);
      if (valueObj instanceof Double) {
        final Double value = (Double) valueObj;
        final ValueGreek valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName);
        final Sensitivity<?> sensitivity = new ValueGreekSensitivity(valueGreek, position.getUniqueId().toString());
        if (sensitivity.getUnderlying().getOrder() <= _maxOrder) {
          final Map<UnderlyingType, DoubleTimeSeries<?>> tsReturns = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
          for (final UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
            final DoubleTimeSeries<?> timeSeries = UnderlyingTimeSeriesProvider.getSeries(historicalSource, _resolutionKey, securitySource, underlyingType,
                position.getSecurity());
            final LocalDate[] schedule = _scheduleCalculator.getSchedule(_startDate, now, true, false);
            final DoubleTimeSeries<?> sampledTS = _samplingCalculator.getSampledTimeSeries(timeSeries, schedule);
            tsReturns.put(underlyingType, _returnCalculator.evaluate(sampledTS));
          }
          dataBundleArray[i++] = new SensitivityAndReturnDataBundle(sensitivity, value, tsReturns);
        }
      } else {
        throw new IllegalArgumentException("Got a value for greek " + valueObj + " that wasn't a Double");
      }
    }
    final Map<Integer, ParametricVaRDataBundle> data = _covarianceMatrixCalculator.evaluate(dataBundleArray);
    @SuppressWarnings("unchecked")
    final Double result = _normalVaRCalculator.evaluate(data);
    final ComputedValue resultValue = new ComputedValue(resultSpecification, result);
    return Collections.singleton(resultValue);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquityOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (final String valueGreekRequirementName : _valueGreekRequirementNames) {
        requirements.add(new ValueRequirement(valueGreekRequirementName, target.getPosition()));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PARAMETRIC_VAR, target.getPosition()), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PositionParametricVaRCalculatorFunction";
  }
*/
}
