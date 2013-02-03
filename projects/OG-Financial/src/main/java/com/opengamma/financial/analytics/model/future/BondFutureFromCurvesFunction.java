/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import static com.opengamma.financial.analytics.model.bond.BondFunction.PROPERTY_CREDIT_CURVE;
import static com.opengamma.financial.analytics.model.bond.BondFunction.PROPERTY_CREDIT_CURVE_CONFIG;
import static com.opengamma.financial.analytics.model.bond.BondFunction.PROPERTY_RISK_FREE_CURVE;
import static com.opengamma.financial.analytics.model.bond.BondFunction.PROPERTY_RISK_FREE_CURVE_CONFIG;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class BondFutureFromCurvesFunction extends BondFutureFunction<YieldCurveBundle> {
  private final String _requirementName;
  private final String _calculationType;

  public BondFutureFromCurvesFunction(final String requirementName, final String calculationType) {
    super();
    ArgumentChecker.notNull(requirementName, "requirement name");
    ArgumentChecker.notNull(calculationType, "calculation type");
    _requirementName = requirementName;
    _calculationType = calculationType;
  }

  @Override
  protected abstract Set<ComputedValue> calculate(com.opengamma.financial.security.future.BondFutureSecurity security, BondFuture bondFuture, YieldCurveBundle data, ComputationTarget target);

  @Override
  protected YieldCurveBundle getData(final ValueRequirement desiredValue, final FunctionInputs inputs, final ComputationTarget target) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
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
    return new YieldCurveBundle(new String[] {creditCurveName, riskFreeCurveName}, new YieldAndDiscountCurve[] {creditCurve, riskFreeCurve});
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> riskFreeCurves = constraints.getValues(PROPERTY_RISK_FREE_CURVE);
    final Set<String> creditCurves = constraints.getValues(PROPERTY_CREDIT_CURVE);
    final Set<String> riskFreeCurveConfigs = constraints.getValues(PROPERTY_RISK_FREE_CURVE_CONFIG);
    final Set<String> creditCurveConfigs = constraints.getValues(PROPERTY_CREDIT_CURVE_CONFIG);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder riskFreeCurveConstraints = ValueProperties.with(ValuePropertyNames.OUTPUT_RESERVED_PREFIX, "risk-free");
    if ((riskFreeCurves == null) || riskFreeCurves.isEmpty()) {
      riskFreeCurveConstraints.withAny(ValuePropertyNames.CURVE);
    } else {
      riskFreeCurveConstraints.with(ValuePropertyNames.CURVE, riskFreeCurves);
    }
    if ((riskFreeCurveConfigs == null) || riskFreeCurveConfigs.isEmpty()) {
      riskFreeCurveConstraints.withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    } else {
      riskFreeCurveConstraints.with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, riskFreeCurveConfigs);
    }
    final ValueRequirement riskFreeCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), riskFreeCurveConstraints.get());
    final ValueProperties.Builder creditCurveConstraints = ValueProperties.with(ValuePropertyNames.OUTPUT_RESERVED_PREFIX, "credit");
    if ((creditCurves == null) || creditCurves.isEmpty()) {
      creditCurveConstraints.withAny(ValuePropertyNames.CURVE);
    } else {
      creditCurveConstraints.with(ValuePropertyNames.CURVE, creditCurves);
    }
    if ((creditCurveConfigs == null) || creditCurveConfigs.isEmpty()) {
      creditCurveConstraints.withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    } else {
      creditCurveConstraints.with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, creditCurveConfigs);
    }
    final ValueRequirement creditCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), creditCurveConstraints.get());
    return Sets.newHashSet(riskFreeCurveRequirement, creditCurveRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String riskFreeCurveName = null;
    String creditCurveName = null;
    String riskFreeCurveConfig = null;
    String creditCurveConfig = null;
    if (inputs.size() == 1) {
      final ValueSpecification spec = Iterables.getOnlyElement(inputs.keySet());
      Set<String> values = spec.getProperties().getValues(ValuePropertyNames.CURVE);
      if ((values == null) || values.isEmpty()) {
        return null;
      }
      riskFreeCurveName = values.iterator().next();
      creditCurveName = riskFreeCurveName;
      values = spec.getProperties().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      if ((values == null) || values.isEmpty()) {
        return null;
      }
      riskFreeCurveConfig = values.iterator().next();
      creditCurveConfig = riskFreeCurveConfig;
    } else {
      assert inputs.size() == 2;
      for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
        final ValueSpecification spec = input.getKey();
        final Set<String> curveNames = spec.getProperties().getValues(ValuePropertyNames.CURVE);
        if ((curveNames == null) || curveNames.isEmpty()) {
          return null;
        }
        final Set<String> curveConfigs = spec.getProperties().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        if ((curveConfigs == null) || curveConfigs.isEmpty()) {
          return null;
        }
        final ValueRequirement requirement = input.getValue();
        final String type = requirement.getConstraint(ValuePropertyNames.OUTPUT_RESERVED_PREFIX);
        if ("risk-free".equals(type)) {
          riskFreeCurveName = curveNames.iterator().next();
          riskFreeCurveConfig = curveConfigs.iterator().next();
        } else {
          assert "credit".equals(type);
          creditCurveName = curveNames.iterator().next();
          creditCurveConfig = curveConfigs.iterator().next();
        }
      }
    }
    assert riskFreeCurveName != null;
    assert creditCurveName != null;
    return Collections.singleton(getResultSpec(target, riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig));
  }

  @Override
  protected String[] getCurveNames(final ValueRequirement desiredValue) {
    final String[] curveNames = new String[2];
    curveNames[0] = desiredValue.getConstraint(PROPERTY_RISK_FREE_CURVE);
    curveNames[1] = desiredValue.getConstraint(PROPERTY_CREDIT_CURVE);
    return curveNames;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.with(ValuePropertyNames.CALCULATION_METHOD, _calculationType);
    return properties;
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_RISK_FREE_CURVE)
        .withAny(PROPERTY_CREDIT_CURVE)
        .withAny(PROPERTY_RISK_FREE_CURVE_CONFIG)
        .withAny(PROPERTY_CREDIT_CURVE_CONFIG).get();
    return new ValueSpecification(_requirementName, target.toSpecification(), properties);
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String riskFreeCurveName, final String creditCurveName, final String riskFreeCurveConfig,
      final String creditCurveConfig) {
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(PROPERTY_RISK_FREE_CURVE_CONFIG, riskFreeCurveConfig)
        .with(PROPERTY_CREDIT_CURVE_CONFIG, creditCurveConfig).get();
    return new ValueSpecification(_requirementName, target.toSpecification(), properties);
  }
}
