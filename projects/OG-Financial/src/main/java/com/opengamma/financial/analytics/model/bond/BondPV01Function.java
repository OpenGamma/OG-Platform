/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;


/**
 * Calculates the PV01 for bonds.
 * @deprecated This function uses deprecated functionality.
 */
@Deprecated
public abstract class BondPV01Function extends BondFromCurvesFunction {

  @Override
  protected Set<ComputedValue> calculate(final ZonedDateTime date, final BondSecurity bondSecurity, final YieldCurveBundle data, final ComputationTarget target, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final String riskFreeCurveConfig = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE_CONFIG);
    final String creditCurveConfig = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE_CONFIG);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String currency = bondSecurity.getCurrency().getCode();
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig, target)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURRENCY, currency);
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bondSecurity.accept(getConverter());
    final BondFixedSecurity bond = definition.toDerivative(date, creditCurveName, riskFreeCurveName);
    final Map<String, Double> pv01 = bond.accept(PV01Calculator.getInstance(), data);
    if (!pv01.keySet().contains(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve called " + curveName);
    }
    return Sets.newHashSet(new ComputedValue(resultSpec, pv01.get(curveName)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = super.getResults(context, target, inputs);
    final Set<ValueSpecification> resultsWithCurve = new HashSet<>();
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    for (final ValueSpecification result : results) {
      final String creditCurveName = result.getProperty(PROPERTY_CREDIT_CURVE);
      final ValueProperties properties = result.getProperties().copy()
          .with(ValuePropertyNames.CURVE, creditCurveName)
          .with(ValuePropertyNames.CURRENCY, currency)
          .get();
      resultsWithCurve.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(), properties));
    }
    return resultsWithCurve;
  }

  @Override
  protected InstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.PV01;
  }

  @Override
  protected ValueProperties.Builder getResultProperties() {
    return super.getResultProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURRENCY);
  }
}
