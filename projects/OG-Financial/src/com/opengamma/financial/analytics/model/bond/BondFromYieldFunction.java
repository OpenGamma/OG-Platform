/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

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
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public abstract class BondFromYieldFunction extends BondFunction<Double> {

  @Override
  protected Set<ComputedValue> calculate(final ZonedDateTime date, final BondSecurity bondSecurity, final Double data, final ComputationTarget target, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final ValueProperties.Builder properties = createValueProperties();
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bondSecurity.accept(getConverter());
    final BondFixedSecurity bond = definition.toDerivative(date, creditCurveName, riskFreeCurveName);
    return Sets.newHashSet(new ComputedValue(resultSpec, 100 * getCalculator().visit(bond, data)));
  }

  @Override
  protected Double getData(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object yieldObject = inputs.getValue(getYieldRequirement(target));
    if (yieldObject == null) {
      throw new OpenGammaRuntimeException("Yield was null");
    }
    return (Double) yieldObject / 100.;
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
    return Sets.newHashSet(getYieldRequirement(target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_YIELD_METHOD);
    return Sets.newHashSet(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  protected abstract AbstractInstrumentDerivativeVisitor<Double, Double> getCalculator();

  protected abstract String getValueRequirementName();

  private ValueRequirement getYieldRequirement(final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.MARKET_YTM, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId());
  }

}
