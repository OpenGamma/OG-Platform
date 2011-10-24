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
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class BondFromCurvesFunction extends BondFunction<YieldCurveBundle> {
  private final Currency _currency;

  public BondFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    this(Currency.of(currency), creditCurveName, riskFreeCurveName);
  }

  public BondFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(creditCurveName, riskFreeCurveName);
    Validate.notNull(currency, "currency");
    _currency = currency;
  }

  @Override
  protected abstract Set<ComputedValue> calculate(BondFixedSecurity bond, YieldCurveBundle data, ComputationTarget target);

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

  protected abstract ValueSpecification getResultSpec(final ComputationTarget target);
}
