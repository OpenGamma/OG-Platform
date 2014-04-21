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
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ExternallyProvidedSensitivitiesYieldCurveCS01Function extends AbstractFunction.NonCompiledInvoker {
  /** The value name for the credit sensitivities required by this function */
  public static final String CREDIT_REQUIREMENT = ValueRequirementNames.CREDIT_SENSITIVITIES;
  /** The value name for CS01 */
  public static final String CS01_REQUIREMENT = ValueRequirementNames.CS01;

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
      return createValueProperties(); //TODO is a problem for externally-provided securities 
    }
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    return properties;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    final ValueProperties valueProperties = ValueProperties.builder().withAny(ValuePropertyNames.CURRENCY).get();
    requirements.add(new ValueRequirement(CREDIT_REQUIREMENT, target.toSpecification(), valueProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder externalProperties = createCurrencyValueProperties(target);
    final Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(CS01_REQUIREMENT, target.toSpecification(), externalProperties.get()));
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties valueProperties = createCurrencyValueProperties(target).get();
    final Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(CS01_REQUIREMENT, targetSpec, valueProperties));
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputationTargetSpecification specification = target.toSpecification();
    final Object value = inputs.getValue(new ValueRequirement(CREDIT_REQUIREMENT, specification));
    if (!(value instanceof LabelledMatrix1D)) {
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
    }
    final StringLabelledMatrix1D ycns = (StringLabelledMatrix1D) value;
    final double result = sum(ycns.getValues());
    final ValueProperties properties = createCurrencyValueProperties(target).get();
    final ComputedValue computedValue = new ComputedValue(new ValueSpecification(CS01_REQUIREMENT, specification, properties), result);
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
