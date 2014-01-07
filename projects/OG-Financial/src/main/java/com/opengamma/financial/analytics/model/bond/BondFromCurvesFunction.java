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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
 * Bond related figures computed from the yield curves.
 * @deprecated This class uses deprecated analytics functions.
 */
@Deprecated
public abstract class BondFromCurvesFunction extends BondFunction<YieldCurveBundle> {

  @Override
  protected Set<ComputedValue> calculate(final ZonedDateTime date, final BondSecurity bondSecurity, final YieldCurveBundle data, final ComputationTarget target, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final String riskFreeCurveConfig = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE_CONFIG);
    final String creditCurveConfig = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE_CONFIG);
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig, target);
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) bondSecurity.accept(getConverter());
    final BondFixedSecurity bond = definition.toDerivative(date, creditCurveName, riskFreeCurveName);
    return Sets.newHashSet(new ComputedValue(resultSpec, bond.accept(getCalculator(), data) * getScaleFactor()));
  }

  @Override
  protected YieldCurveBundle getData(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    if (desiredValues.size() != 1) {
      throw new OpenGammaRuntimeException("This function " + getShortName() + " only provides a single output");
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String riskFreeCurveName = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    final String riskFreeConfig = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE_CONFIG);
    final ValueProperties riskFreeCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, riskFreeCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, riskFreeConfig).get();
    final ValueRequirement riskFreeCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), riskFreeCurveProperties);
    final Object riskFreeCurveObject = inputs.getValue(riskFreeCurveRequirement);
    if (riskFreeCurveObject == null) {
      throw new OpenGammaRuntimeException("Risk free curve was null");
    }
    final String creditCurveName = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    final String creditConfig = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE_CONFIG);
    final ValueProperties creditCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, creditCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, creditConfig).get();
    final ValueRequirement creditCurveRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), creditCurveProperties);
    final Object creditCurveObject = inputs.getValue(creditCurveRequirement);
    if (creditCurveObject == null) {
      throw new OpenGammaRuntimeException("Credit curve was null");
    }
    final YieldAndDiscountCurve creditCurve = (YieldAndDiscountCurve) creditCurveObject;
    final YieldAndDiscountCurve riskFreeCurve = (YieldAndDiscountCurve) riskFreeCurveObject;
    return new YieldCurveBundle(new String[] {creditCurveName, riskFreeCurveName }, new YieldAndDiscountCurve[] {creditCurve, riskFreeCurve });
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> riskFreeCurves = constraints.getValues(PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = constraints.getValues(PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final Set<String> riskFreeCurveConfigs = constraints.getValues(PROPERTY_RISK_FREE_CURVE_CONFIG);
    if (riskFreeCurveConfigs == null || riskFreeCurveConfigs.size() != 1) {
      return null;
    }
    final Set<String> creditCurveConfigs = constraints.getValues(PROPERTY_CREDIT_CURVE_CONFIG);
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
    String riskFreeCurveConfig = null;
    String creditCurveConfig = null;
    if (inputs.size() == 1) {
      final ValueSpecification spec = Iterables.getOnlyElement(inputs.keySet());
      final String curveName = spec.getProperty(ValuePropertyNames.CURVE);
      final String curveConfig = spec.getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      if (curveName == null) {
        throw new OpenGammaRuntimeException("Missing or non-unique curve name");
      }
      if (curveConfig == null) {
        throw new OpenGammaRuntimeException("Missing or non-unique curve calculation configuration name");
      }
      riskFreeCurveName = curveName;
      creditCurveName = curveName;
      riskFreeCurveConfig = curveConfig;
      creditCurveConfig = curveConfig;
    } else {
      assert inputs.size() == 2;
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
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig,
        target);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  /**
   * Gets the calculator of the desired value.
   * @return The calculator
   */
  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator();

  /**
   * Gets the value requirement name.
   * @return The value requirement name
   */
  protected abstract String getValueRequirementName();

  /**
   * Gets the result properties.
   * @return The result properties
   */
  protected ValueProperties.Builder getResultProperties() {
    return createValueProperties()
        .withAny(PROPERTY_RISK_FREE_CURVE)
        .withAny(PROPERTY_CREDIT_CURVE)
        .withAny(PROPERTY_RISK_FREE_CURVE_CONFIG)
        .withAny(PROPERTY_CREDIT_CURVE_CONFIG)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD);
  }

  /**
   * Gets the result properties.
   * @param riskFreeCurveName The risk-free curve name
   * @param creditCurveName The credit curve name
   * @param riskFreeCurveConfig The risk-free curve calculation configuration name
   * @param creditCurveConfig The credit curve calculation configuration name
   * @param target The target
   * @return The result properties
   */
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String riskFreeCurveConfig,
      final String creditCurveConfig, final ComputationTarget target) {
    return createValueProperties()
        .with(PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(PROPERTY_RISK_FREE_CURVE_CONFIG, riskFreeCurveConfig)
        .with(PROPERTY_CREDIT_CURVE_CONFIG, creditCurveConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD);
  }
}
