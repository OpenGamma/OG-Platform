/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
public class BlackScholesModelCostOfCarryFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getSecurity() instanceof EquityOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURVE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, option.getCurrency(), ValueProperties.with(ValuePropertyNames.CURVE, curveName).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final String curveName = inputs.keySet().iterator().next().getProperty(ValuePropertyNames.CURVE);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURVE, curveName).get()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final Object curveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (curveObject == null) {
      throw new NullPointerException("Could not get yield curve for option");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = curve.getInterestRate(t);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURVE,
        desiredValues.iterator().next().getConstraint(ValuePropertyNames.CURVE)).get()), b));
  }

  @Override
  public String getShortName() {
    return "BlackScholesCostOfCarryFunction";
  }

}
