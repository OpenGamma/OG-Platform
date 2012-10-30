/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public abstract class BondMarketDataFunction extends NonCompiledInvoker {
  private final String _requirementName;

  public BondMarketDataFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirementName");
    _requirementName = requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY,
        security.getUniqueId());
    final Object value = inputs.getValue(requirement);
    if (value == null) {
      throw new OpenGammaRuntimeException("Could not get " + requirement);
    }
    return getComputedValues(executionContext, (Double) value, security, target);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
      final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, target
        .getSecurity().getUniqueId()));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      final Security security = target.getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected abstract Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final double value, final BondSecurity security, final ComputationTarget target);

}
