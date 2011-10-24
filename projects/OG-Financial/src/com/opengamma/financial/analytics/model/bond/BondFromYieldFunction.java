/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class BondFromYieldFunction extends BondFunction<Double> {

  public BondFromYieldFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    this(Currency.of(currency), creditCurveName, riskFreeCurveName);
  }

  public BondFromYieldFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(creditCurveName, riskFreeCurveName);
    Validate.notNull(currency, "currency");
  }

  @Override
  protected abstract Set<ComputedValue> calculate(BondFixedSecurity bond, Double data, ComputationTarget target);

  @Override
  protected Double getData(final FunctionInputs inputs, final ComputationTarget target) {
    final Object yieldObject = inputs.getValue(getYieldRequirement(target));
    if (yieldObject == null) {
      throw new OpenGammaRuntimeException("Yield was null");
    }
    return (Double) yieldObject / 100;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(getYieldRequirement(target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  private ValueRequirement getYieldRequirement(final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.MARKET_YTM, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId());
  }

  protected abstract ValueSpecification getResultSpec(final ComputationTarget target);
}
