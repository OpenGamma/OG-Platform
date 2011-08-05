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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class BondFromYieldFunction extends BondFunction<Double> {
  private final String _requirementName;
  private String _calculationType;

  public BondFromYieldFunction(final String currency, final String creditCurveName, final String riskFreeCurveName, final String requirementName, final String calculationType) {
    this(Currency.of(currency), creditCurveName, riskFreeCurveName, requirementName, calculationType);
  }

  public BondFromYieldFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName, final String requirementName, final String calculationType) {
    super(creditCurveName, riskFreeCurveName);
    Validate.notNull(requirementName, "requirement name");
    Validate.notNull(currency, "currency");
    Validate.notNull(calculationType, "calculation type");
    _requirementName = requirementName;
    _calculationType = calculationType;
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

  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, _calculationType).get();
    return new ValueSpecification(_requirementName, target.toSpecification(), properties);
  }
}
