/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fxforwards.discounting;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;

/**
 *
 */
public abstract class FXForwardDiscountingFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return null;
  }

}
