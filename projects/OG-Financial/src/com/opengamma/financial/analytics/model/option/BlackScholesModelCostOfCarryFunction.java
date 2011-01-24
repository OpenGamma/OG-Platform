/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
public class BlackScholesModelCostOfCarryFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getSnapshotClock().zonedDateTime();
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final Object curveObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, option.getCurrency().getUniqueId()));
    if (curveObject == null) {
      throw new NullPointerException("Could not get yield curve for option");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = curve.getInterestRate(t);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.COST_OF_CARRY, option), getUniqueId()), b));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof OptionSecurity) {
      return ((OptionSecurity) target.getSecurity()) instanceof EquityOptionSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final OptionSecurity option = (OptionSecurity) target.getSecurity();
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, option.getCurrency().getUniqueId()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Security security = target.getSecurity();
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.COST_OF_CARRY, security), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BlackScholesCostOfCarryFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
