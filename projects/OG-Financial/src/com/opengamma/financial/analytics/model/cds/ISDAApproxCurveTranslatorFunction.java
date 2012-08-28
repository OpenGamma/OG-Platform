/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
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
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

public class ISDAApproxCurveTranslatorFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.PRIMITIVE;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    
    if (!canApplyTo(context, target)) {
      return null;
    }
    
    final Set<String> currency = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    
    if (currency == null || currency.size() != 1) {
      throw new OpenGammaRuntimeException("ISDA discount curves must describe exactly one currency");
    }
    
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    
    requirements.add(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE,
      ComputationTargetType.PRIMITIVE,
      Currency.of(currency.iterator().next()).getUniqueId(),
      ValueProperties
        .with("Curve", "SECONDARY")
        .with("FundingCurve", "SECONDARY")
        .with("ForwardCurve", "SECONDARY")
        .with("CurveCalculationMethod", "ParRate")
        .get()));
    
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return null;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    return null;
  }

}
