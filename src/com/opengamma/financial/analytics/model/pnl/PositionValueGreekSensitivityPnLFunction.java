/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
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
import com.opengamma.financial.pnl.SensitivityPnLCalculator;
import com.opengamma.financial.sensitivity.ValueGreek;

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
    
    for (String valueGreekRequirementName : _valueGreekRequirementNames) {
      Object valueGreekValue = inputs.getValue(new ValueRequirement(valueGreekRequirementName, position));
      System.out.println("Got value greek value " + valueGreekValue + " for target " + target);
    }
    
    ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position));
    ComputedValue resultValue = new ComputedValue(resultSpecification, 55.);
    return Collections.singleton(resultValue);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    return true;
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
