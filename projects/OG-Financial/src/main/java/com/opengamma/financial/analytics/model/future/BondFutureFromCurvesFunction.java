/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class BondFutureFromCurvesFunction extends BondFutureFunction<YieldCurveBundle> {
  private final String _requirementName;
  private final Currency _currency;
  private final String _calculationType;

  public BondFutureFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName, final String requirementName, final String calculationType) {
    this(Currency.of(currency), creditCurveName, riskFreeCurveName, requirementName, calculationType);
  }

  public BondFutureFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName, final String requirementName, final String calculationType) {
    super(creditCurveName, riskFreeCurveName);
    Validate.notNull(requirementName, "requirement name");
    Validate.notNull(currency, "currency");
    Validate.notNull(calculationType, "calculation type");
    _requirementName = requirementName;
    _currency = currency;
    _calculationType = calculationType;
  }

  @Override
  protected abstract Set<ComputedValue> calculate(com.opengamma.financial.security.future.BondFutureSecurity security, BondFuture bondFuture, YieldCurveBundle data, ComputationTarget target);

  @Override
  protected YieldCurveBundle getData(final FunctionInputs inputs, final ComputationTarget target) {
    final Object creditCurveObject = inputs.getValue(getCreditCurveRequirement());
    if (creditCurveObject == null) {
      throw new OpenGammaRuntimeException("Credit curve was null");
    }
    final Object riskFreeCurveObject = inputs.getValue(getRiskFreeCurveRequirement());
    if (riskFreeCurveObject == null) {
      throw new OpenGammaRuntimeException("Risk free curve was null");
    }
    final YieldAndDiscountCurve creditCurve = (YieldAndDiscountCurve) creditCurveObject;
    final YieldAndDiscountCurve riskFreeCurve = (YieldAndDiscountCurve) riskFreeCurveObject;
    return new YieldCurveBundle(new String[] {getCreditCurveName(), getRiskFreeCurveName()}, new YieldAndDiscountCurve[] {creditCurve, riskFreeCurve});
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet(getCreditCurveRequirement(), getRiskFreeCurveRequirement());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  private ValueRequirement getCreditCurveRequirement() {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, getCreditCurveName()).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, _currency.getUniqueId(), properties);
  }

  private ValueRequirement getRiskFreeCurveRequirement() {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, getRiskFreeCurveName()).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, _currency.getUniqueId(), properties);
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, _calculationType).get();
    return new ValueSpecification(_requirementName, target.toSpecification(), properties);
  }

}
