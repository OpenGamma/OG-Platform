/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * Bond function for results computed from the market clean price.
 */
public abstract class BondFromPriceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime date = ZonedDateTime.now(executionContext.getValuationClock());
    if (desiredValues.size() != 1) {
      throw new OpenGammaRuntimeException("This function " + getShortName() + " only provides a single output");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(BondFunction.PROPERTY_RISK_FREE_CURVE);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object riskFreeCurveObject = inputs.getValue(getCurveRequirement(target, riskFreeCurveName));
    if (riskFreeCurveObject == null) {
      throw new OpenGammaRuntimeException("Risk free curve was null");
    }
    final Object curveObject = inputs.getValue(getCurveRequirement(target, curveName));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Curve was null");
    }
    final Object cleanPriceObject = inputs.getValue(getCleanPriceRequirement(target, desiredValue));
    if (cleanPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get clean price requirement");
    }
    final Double cleanPrice = (Double) cleanPriceObject;
    final String creditCurveName = riskFreeCurveName;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, curveName);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties.get());
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final YieldAndDiscountCurve riskFreeCurve = (YieldAndDiscountCurve) riskFreeCurveObject;
    final YieldCurveBundle data = new YieldCurveBundle(new String[] {curveName, riskFreeCurveName }, new YieldAndDiscountCurve[] {curve, riskFreeCurve });
    return Sets.newHashSet(new ComputedValue(resultSpec, getValue(executionContext, date, riskFreeCurveName, creditCurveName, target, data, cleanPrice)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String curveName = curves.iterator().next();
    return Sets.newHashSet(getCurveRequirement(target, riskFreeCurveName), getCurveRequirement(target, curveName), getCleanPriceRequirement(target, desiredValue));
  }

  protected abstract ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue);

  protected abstract String getCalculationMethodName();

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  protected abstract ValueProperties.Builder getResultProperties();

  protected abstract ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String curveName);

  protected abstract double getValue(FunctionExecutionContext context, ZonedDateTime date, String riskFreeCurveName, String creditCurveName, ComputationTarget bond,
      YieldCurveBundle data, double price);
}
