/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTypeToHistoricalTimeSeries;
import com.opengamma.financial.pnl.PnLDataBundle;
import com.opengamma.financial.pnl.SensitivityPnLCalculator;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Computes a Profit and Loss time series for a position based on value greeks.
 * Takes in a set of specified value greeks (which will be part of configuration),
 * converts to sensitivities, loads the underlying time series, and calculates
 * a series of P&L based on {@link SensitivityPnLCalculator}.
 * 
 */
public class PositionValueGreekSensitivityPnLFunction extends AbstractFunction implements FunctionInvoker {
  private final Set<ValueGreek> _valueGreeks;
  private final Set<String> _valueGreekRequirementNames;
  private final TimeSeriesReturnCalculator _returnCalculator = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  
  public PositionValueGreekSensitivityPnLFunction(String valueGreekRequirementName) {
    this(new String[]{valueGreekRequirementName});
  }
  
  public PositionValueGreekSensitivityPnLFunction(String valueGreekRequirementName1, String valueGreekRequirementName2) {
    this(new String[]{valueGreekRequirementName1, valueGreekRequirementName2});
  }
  
  public PositionValueGreekSensitivityPnLFunction(String... valueGreekRequirementNames) {
    _valueGreeks = new HashSet<ValueGreek>();
    _valueGreekRequirementNames = new HashSet<String>();
    for (String valueGreekRequirementName : valueGreekRequirementNames) {
      _valueGreekRequirementNames.add(valueGreekRequirementName);
      _valueGreeks.add(AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName));
    }
  }

  @Override
  public Set<ComputedValue> execute(
      final FunctionExecutionContext executionContext,
      final FunctionInputs inputs,
      final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    
    ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position));
    Map<Sensitivity<?>, RiskFactorResult> sensitivities = new HashMap<Sensitivity<?>, RiskFactorResult>();
    Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> tsReturns = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
    for (String valueGreekRequirementName : _valueGreekRequirementNames) {
       Object valueObj = inputs.getValue(valueGreekRequirementName);
       if (valueObj instanceof Double) {
         Double value = (Double) valueObj;
         ValueGreek valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName);
         Sensitivity<?> sensitivity = new ValueGreekSensitivity(valueGreek, position.getUniqueIdentifier().toString());
         RiskFactorResult riskFactorResult = new RiskFactorResult(value);
         sensitivities.put(sensitivity, riskFactorResult);
         Map<Object, DoubleTimeSeries<?>> underlyings = new HashMap<Object, DoubleTimeSeries<?>>();
         LocalDateDoubleTimeSeries intersection = null;
         for (UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
           
           LocalDateDoubleTimeSeries timeSeries = UnderlyingTypeToHistoricalTimeSeries.getSeries(executionContext.getHistoricalDataProvider(),
                                                                                                 executionContext.getSecurityMaster(), 
                                                                                                 underlyingType, position.getSecurity());
           if (intersection == null) {
             intersection = timeSeries;
           } else {
             intersection = (LocalDateDoubleTimeSeries) intersection.intersectionFirstValue(timeSeries);
           }
         }
         for (UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
           
           LocalDateDoubleTimeSeries timeSeries = UnderlyingTypeToHistoricalTimeSeries.getSeries(executionContext.getHistoricalDataProvider(),
                                                                                                 executionContext.getSecurityMaster(), 
                                                                                                 underlyingType, position.getSecurity());
           timeSeries = (LocalDateDoubleTimeSeries) timeSeries.intersectionFirstValue(intersection);
           underlyings.put(underlyingType, _returnCalculator.evaluate(timeSeries));
         }
         tsReturns.put(sensitivity, underlyings);
       } else {
         throw new IllegalArgumentException("Got a value for greek "+valueObj+" that wasn't a Double");
       }
    }
    PnLDataBundle dataBundle = new PnLDataBundle(sensitivities, tsReturns);
    SensitivityPnLCalculator calculator = new SensitivityPnLCalculator();
    DoubleTimeSeries<?> result = calculator.evaluate(dataBundle);
    ComputedValue resultValue = new ComputedValue(resultSpecification, result);
    return Collections.singleton(resultValue);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return (target.getType() == ComputationTargetType.POSITION);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (String valueGreekRequirementName : _valueGreekRequirementNames) {
      requirements.add(new ValueRequirement(valueGreekRequirementName, target.getPosition()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition())));
    return results;
  }

  @Override
  public String getShortName() {
    return "PositionValueGreekSensitivityPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
