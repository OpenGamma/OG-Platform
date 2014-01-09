/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.util.money.Currency;

/**
 * Bond related figures computed from the market yield.
 * @deprecated Use {@link com.opengamma.financial.analytics.model.bondyield.BondFromYieldFunction}
 */
@Deprecated
public abstract class BondFromYieldFunction extends BondFunction<Double> {

  @Override
  protected Set<ComputedValue> calculate(final ZonedDateTime date, final BondSecurity bondSecurity, final Double data, final ComputationTarget target, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String riskFreeCurveConfig = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE_CONFIG);
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final String creditCurveConfig = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE_CONFIG);
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig);
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bondSecurity.accept(getConverter());
    final BondFixedSecurity bond = definition.toDerivative(date, creditCurveName, riskFreeCurveName);
    return Sets.newHashSet(new ComputedValue(resultSpec, bond.accept(getCalculator(), data) * getScaleFactor()));
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
    final Set<String> riskFreeCurveConfigs = desiredValue.getConstraints().getValues(PROPERTY_RISK_FREE_CURVE_CONFIG);
    if (riskFreeCurveConfigs == null || riskFreeCurveConfigs.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final Set<String> creditCurveConfigs = desiredValue.getConstraints().getValues(PROPERTY_CREDIT_CURVE_CONFIG);
    if (creditCurveConfigs == null || creditCurveConfigs.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String creditCurveName = creditCurves.iterator().next();
    final String riskFreeCurveConfig = riskFreeCurveConfigs.iterator().next();
    final String creditCurveConfig = creditCurveConfigs.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties riskFreeCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, riskFreeCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, riskFreeCurveConfig)
        .withOptional(PROPERTY_RISK_FREE_CURVE).with(PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .withOptional(PROPERTY_RISK_FREE_CURVE_CONFIG).with(PROPERTY_RISK_FREE_CURVE_CONFIG, riskFreeCurveConfig).get();
    final ValueRequirement riskFreeCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), riskFreeCurveProperties);
    final ValueProperties creditCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, creditCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, creditCurveConfig)
        .withOptional(PROPERTY_CREDIT_CURVE).with(PROPERTY_CREDIT_CURVE, creditCurveName)
        .withOptional(PROPERTY_CREDIT_CURVE_CONFIG).with(PROPERTY_CREDIT_CURVE_CONFIG, creditCurveConfig).get();
    final ValueRequirement creditCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), creditCurveProperties);
    return Sets.newHashSet(riskFreeCurveRequirement, creditCurveRequirement, getYieldRequirement(target));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties();
    return Sets.newHashSet(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String riskFreeCurveName = null;
    String creditCurveName = null;
    String riskFreeCurveConfig = null;
    String creditCurveConfig = null;
    if (inputs.size() == 2) {
      String curveName = null;
      String curveConfig = null;
      for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
        final ValueRequirement requirement = input.getValue();
        if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
          curveName = requirement.getConstraint(ValuePropertyNames.CURVE);
          curveConfig = requirement.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
          break;
        }
      }
      riskFreeCurveName = curveName;
      riskFreeCurveConfig = curveConfig;
      creditCurveName = curveName;
      creditCurveConfig = curveConfig;
    } else {
      for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
        final ValueRequirement requirement = input.getValue();
        if (requirement.getConstraint(PROPERTY_RISK_FREE_CURVE) != null) {
          riskFreeCurveName = requirement.getConstraint(PROPERTY_RISK_FREE_CURVE);
          riskFreeCurveConfig = requirement.getConstraint(PROPERTY_RISK_FREE_CURVE_CONFIG);
        } else if (requirement.getConstraint(PROPERTY_CREDIT_CURVE) != null) {
          creditCurveName = requirement.getConstraint(PROPERTY_CREDIT_CURVE);
          creditCurveConfig = requirement.getConstraint(PROPERTY_CREDIT_CURVE_CONFIG);
        }
      }
    }
    assert riskFreeCurveName != null;
    assert creditCurveName != null;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  /**
   * Gets the calculator of the desired value.
   * @return The calculator
   */
  protected abstract InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator();

  /**
   * Gets the value requirement name.
   * @return The value requirement name
   */
  protected abstract String getValueRequirementName();

  /**
   * Gets the yield market data requirement.
   * @param target The target
   * @return The value requirement
   */
  private ValueRequirement getYieldRequirement(final ComputationTarget target) {
    return new ValueRequirement(ValueRequirementNames.MARKET_YTM, target.toSpecification(), ValueProperties.builder().get());
  }

  /**
   * Gets the result properties.
   * @return The result properties
   */
  private ValueProperties.Builder getResultProperties() {
    return createValueProperties()
        .withAny(PROPERTY_RISK_FREE_CURVE)
        .withAny(PROPERTY_CREDIT_CURVE)
        .withAny(PROPERTY_RISK_FREE_CURVE_CONFIG)
        .withAny(PROPERTY_CREDIT_CURVE_CONFIG)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_YIELD_METHOD);
  }

  /**
   * Gets the result properties.
   * @param riskFreeCurveName The risk-free curve name
   * @param creditCurveName The credit curve name
   * @param riskFreeCurveConfig The risk-free curve calculation configuration name
   * @param creditCurveConfig The credit curve calculation configuration name
   * @return The result properties
   */
  private ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String riskFreeCurveConfig,
      final String creditCurveConfig) {
    return createValueProperties()
        .with(PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(PROPERTY_RISK_FREE_CURVE_CONFIG, riskFreeCurveConfig)
        .with(PROPERTY_CREDIT_CURVE_CONFIG, creditCurveConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_YIELD_METHOD);
  }
}
