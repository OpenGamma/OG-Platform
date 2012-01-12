/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class BondFromCurvesFunction extends BondFunction<YieldCurveBundle> {

  @Override
  protected Set<ComputedValue> calculate(final ZonedDateTime date, final BondSecurity bondSecurity, final YieldCurveBundle data, final ComputationTarget target, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName);
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bondSecurity.accept(getConverter());
    final BondFixedSecurity bond = definition.toDerivative(date, creditCurveName, riskFreeCurveName);
    return Sets.newHashSet(new ComputedValue(resultSpec, 100 * getCalculator().visit(bond, data)));
  }

  @Override
  protected YieldCurveBundle getData(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    if (desiredValues.size() != 1) {
      throw new OpenGammaRuntimeException("This function " + getShortName() + " only provides a single output");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder riskFreeCurveProperties = ValueProperties
        .with(ValuePropertyNames.CURVE, riskFreeCurveName);
    final ValueRequirement riskFreeCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), riskFreeCurveProperties.get());
    final Object riskFreeCurveObject = inputs.getValue(riskFreeCurveRequirement);
    if (riskFreeCurveObject == null) {
      throw new OpenGammaRuntimeException("Risk free curve was null");
    }
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final ValueProperties.Builder creditCurveProperties = ValueProperties
        .with(ValuePropertyNames.CURVE, creditCurveName);
    final ValueRequirement creditCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), creditCurveProperties.get());
    final Object creditCurveObject = inputs.getValue(creditCurveRequirement);
    if (creditCurveObject == null) {
      throw new OpenGammaRuntimeException("Credit curve was null");
    }
    final YieldAndDiscountCurve creditCurve = (YieldAndDiscountCurve) creditCurveObject;
    final YieldAndDiscountCurve riskFreeCurve = (YieldAndDiscountCurve) riskFreeCurveObject;
    return new YieldCurveBundle(new String[] {creditCurveName, riskFreeCurveName}, new YieldAndDiscountCurve[] {creditCurve, riskFreeCurve});
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String creditCurveName = creditCurves.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder riskFreeCurveProperties = ValueProperties
        .with(ValuePropertyNames.CURVE, riskFreeCurveName)
        .withOptional(PROPERTY_RISK_FREE_CURVE);
    final ValueRequirement riskFreeCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), riskFreeCurveProperties.get());
    final ValueProperties.Builder creditCurveProperties = ValueProperties
        .with(ValuePropertyNames.CURVE, creditCurveName)
        .withOptional(PROPERTY_CREDIT_CURVE);
    final ValueRequirement creditCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), creditCurveProperties.get());
    return Sets.newHashSet(riskFreeCurveRequirement, creditCurveRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String riskFreeCurveName = null;
    String creditCurveName = null;
    if (inputs.size() == 1) {
      final String curveName = inputs.keySet().iterator().next().getProperty(ValuePropertyNames.CURVE);
      if (curveName == null) {
        throw new OpenGammaRuntimeException("Missing or non-unique curve name");
      }
      riskFreeCurveName = curveName;
      creditCurveName = curveName;
    } else {
      assert inputs.size() == 2;
      for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
        if (input.getValue().getConstraints().getValues(PROPERTY_RISK_FREE_CURVE) != null) {
          final Set<String> riskFreeCurves = input.getValue().getConstraints().getValues(ValuePropertyNames.CURVE);
          if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique risk-free curve name");
          } else {
            riskFreeCurveName = riskFreeCurves.iterator().next();
          }
        } else if (input.getValue().getConstraints().getValues(PROPERTY_CREDIT_CURVE) != null) {
          final Set<String> creditCurves = input.getValue().getConstraints().getValues(ValuePropertyNames.CURVE);
          if (creditCurves == null || creditCurves.size() != 1) {
            throw new OpenGammaRuntimeException("Missing or non-unique credit curve name");
          } else {
            creditCurveName = creditCurves.iterator().next();
          }
        }
      }
    }
    assert riskFreeCurveName != null;
    assert creditCurveName != null;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  protected abstract AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator();

  protected abstract String getValueRequirementName();

  private ValueProperties.Builder getResultProperties() {
    return createValueProperties()
        .withAny(PROPERTY_RISK_FREE_CURVE)
        .withAny(PROPERTY_CREDIT_CURVE)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD);
  }

  private ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName) {
    return createValueProperties()
        .with(PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD);
  }
}
