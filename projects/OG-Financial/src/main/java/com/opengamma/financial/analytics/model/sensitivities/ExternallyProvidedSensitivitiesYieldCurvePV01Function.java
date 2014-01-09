/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ExternallyProvidedSensitivitiesYieldCurvePV01Function extends AbstractFunction.NonCompiledInvoker {
  /** The value name for the yield curve node sensitivities required by this function */
  public static final String YCNS_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  /** The value name for the DV01 produced by this function */
  public static final String PV01_REQUIREMENT = ValueRequirementNames.PV01;

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getPosition().getSecurity() instanceof FinancialSecurity) {
      return true;
    }
    if (!(target.getPosition().getSecurity() instanceof RawSecurity)) {
      return false;
    }
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    return security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }

  private ValueProperties.Builder createCurrencyValueProperties(final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    if (FXUtils.isFXSecurity(security)) {
      return createValueProperties(); //TODO what to do in this case?
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    if (ccy == null) {
      return createValueProperties(); //TODO a problem when using externally-provided securities
    }
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    return properties;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    final String curveName = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE);
    if (curveName == null) {
      return null;
    }
    final String curveCurrency = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrency == null) {
      return null;
    }
    final String curveCalculationConfig = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfig == null) {
      return null;
    }
    final ValueProperties valueProperties = ValueProperties.builder().withAny(ValuePropertyNames.CURRENCY).with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).get();
    requirements.add(new ValueRequirement(YCNS_REQUIREMENT, target.toSpecification(), valueProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties externalProperties = createCurrencyValueProperties(target).withAny(ValuePropertyNames.CURRENCY).withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
    final Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(PV01_REQUIREMENT, target.toSpecification(), externalProperties));
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveName = null;
    String curveCurrencyName = null;
    String curveCalculationConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      if (entry.getKey().getValueName().equals(YCNS_REQUIREMENT)) {
        curveName = entry.getKey().getProperty(ValuePropertyNames.CURVE);
        curveCurrencyName = entry.getKey().getProperty(ValuePropertyNames.CURVE_CURRENCY);
        curveCalculationConfigName = entry.getKey().getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      }
    }
    assert curveName != null;
    final ValueProperties valueProperties = createCurrencyValueProperties(target).with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CURRENCY, curveCurrencyName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
    final Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(PV01_REQUIREMENT, targetSpec, valueProperties));
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ComputationTargetSpecification specification = target.toSpecification();
    final Object value = inputs.getValue(new ValueRequirement(YCNS_REQUIREMENT, specification));
    if (!(value instanceof LabelledMatrix1D)) {
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
    }
    final DoubleLabelledMatrix1D ycns = (DoubleLabelledMatrix1D) value;
    final double result = sum(ycns.getValues()) / 10000d;
    final ValueProperties properties = createCurrencyValueProperties(target).with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).get();
    final ComputedValue computedValue = new ComputedValue(new ValueSpecification(PV01_REQUIREMENT, specification, properties), result);
    return Collections.singleton(computedValue);
  }

  private double sum(final double[] values) {
    double total = 0d;
    for (final double value : values) {
      total += value;
    }
    return total;
  }
}
