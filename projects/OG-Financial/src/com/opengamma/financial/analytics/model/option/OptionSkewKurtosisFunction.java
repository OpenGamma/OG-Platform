/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 *
 */
public abstract class OptionSkewKurtosisFunction extends AbstractFunction implements FunctionInvoker {
  /** */
  public static final String SKEW = "Skew";
  /** */
  public static final String KURTOSIS = "Kurtosis";

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof OptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, option.getUniqueIdentifier())); //TODO is this right?
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final UniqueIdentifier uid = target.getSecurity().getUniqueIdentifier();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(new ValueSpecification(
          new ValueRequirement(SKEW, ComputationTargetType.SECURITY, uid),
          getUniqueIdentifier()));
      results.add(new ValueSpecification(
          new ValueRequirement(KURTOSIS, ComputationTargetType.SECURITY, uid),
          getUniqueIdentifier()));
      return results;
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
