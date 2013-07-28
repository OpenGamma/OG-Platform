/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * Calculates the value delta of an {@link IRFutureOptionSecurity} using the Black Delta as input. <p>
 * See {@link InterestRateFutureOptionBlackPositionDeltaFunction}
 */
public class InterestRateFutureOptionBlackValueDeltaFunction extends InterestRateFutureOptionBlackFunction {

  public InterestRateFutureOptionBlackValueDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA);
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    requirements.add(new ValueRequirement(ValueRequirementNames.POSITION_DELTA, target.toSpecification(), desiredValue.getConstraints()));
    requirements.add(new ValueRequirement(ValueRequirementNames.FORWARD, target.toSpecification()));   
    return requirements;
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    // Build output specification. 
    // TODO This is going to be a copy of the spec of the delta!!!
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final ValueProperties properties = getResultProperties(currency.getCode(), curveCalculationConfigName, surfaceName);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), properties);
    
    // Get inputs and compute output
    Object deltaObject = inputs.getValue(ValueRequirementNames.POSITION_DELTA);
    if (deltaObject == null) {
      throw new OpenGammaRuntimeException("Could not get PositionDelta for " + security.getUnderlyingId());
    }
    final Double positionDelta = (Double) deltaObject;

    Object futureObject = inputs.getValue(ValueRequirementNames.FORWARD);
    if (futureObject == null) {
      throw new OpenGammaRuntimeException("Could not get Forward for " + security.getUnderlyingId());
    }
    final Double futurePrice = (Double) futureObject;
      
    final Double valueDelta = futurePrice * positionDelta;
    return Collections.singleton(new ComputedValue(spec, valueDelta)); 
  }

  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative irFutureOption, YieldCurveWithBlackCubeBundle data, ValueSpecification spec) {
    return Collections.singleton(new ComputedValue(spec, "THIS WILL NOT GET CALLED"));
  }
  

}
