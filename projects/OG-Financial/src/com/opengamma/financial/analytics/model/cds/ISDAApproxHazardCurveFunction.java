/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Bootstrap the hazard rate function according to the ISDA model given market spreads as input
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPriceHazardCurveFunction
 */
public class ISDAApproxHazardCurveFunction extends NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE || target.getUniqueId() == null) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return null;
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
