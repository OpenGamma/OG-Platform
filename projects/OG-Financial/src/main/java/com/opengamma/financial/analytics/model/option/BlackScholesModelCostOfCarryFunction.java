/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
@Deprecated
public class BlackScholesModelCostOfCarryFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveName = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE);
    if (curveName == null) {
      return null;
    }
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final ValueProperties.Builder constraints = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    final Set<String> curveCalculationMethods = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods != null) {
      if (curveCalculationMethods.isEmpty()) {
        constraints.withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      } else {
        constraints.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethods);
      }
      if (desiredValue.getConstraints().isOptional(ValuePropertyNames.CURVE_CALCULATION_METHOD)) {
        constraints.withOptional(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      }
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(option.getCurrency()), constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueProperties.Builder properties = createValueProperties();
    final String curveName = input.getProperties().getStrictValue(ValuePropertyNames.CURVE);
    properties.with(ValuePropertyNames.CURVE, curveName);
    final String curveCalculationMethod = input.getProperties().getStrictValue(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethod != null) {
      properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod);
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final Object curveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (curveObject == null) {
      throw new NullPointerException("Could not get yield curve for option");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry());
    final double b = curve.getInterestRate(t);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.COST_OF_CARRY, target.toSpecification(), desiredValue.getConstraints()), b));
  }

  @Override
  public String getShortName() {
    return "BlackScholesCostOfCarryFunction";
  }

}
