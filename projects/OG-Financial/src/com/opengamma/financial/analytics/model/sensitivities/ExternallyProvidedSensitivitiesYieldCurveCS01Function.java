package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;

public class ExternallyProvidedSensitivitiesYieldCurveCS01Function extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesYieldCurveCS01Function.class);
  /**
   * The value name calculated by this function.
   */
  public static final String CREDIT_REQUIREMENT = ValueRequirementNames.CREDIT_SENSITIVITIES;
  public static final String CS01_REQUIREMENT = ValueRequirementNames.CS01;

  @Override
  public void init(final FunctionCompilationContext context) {

  }

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
    Currency ccy = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    return properties;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    Set<ValueRequirement> requirements = Sets.newHashSet();
    ValueProperties valueProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY).get();
        //.withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        //.withAny(ValuePropertyNames.CURVE_CURRENCY).get();
    requirements.add(new ValueRequirement(CREDIT_REQUIREMENT, target.toSpecification(), valueProperties));
    //s_logger.warn("getRequirements() returned " + requirements);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder externalProperties = createCurrencyValueProperties(target);
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(CS01_REQUIREMENT, target.toSpecification(), externalProperties.get()));
    s_logger.warn("getResults(1) = " + results);
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    ValueProperties valueProperties = createCurrencyValueProperties(target).get();
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(CS01_REQUIREMENT, targetSpec, valueProperties));
    s_logger.warn("getResults(2) returning " + results);
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    ComputationTargetSpecification specification = target.toSpecification();
    Object value = inputs.getValue(new ValueRequirement(CREDIT_REQUIREMENT, specification));
    if (!(value instanceof LabelledMatrix1D)) {
      s_logger.error("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
    }
    StringLabelledMatrix1D ycns = (StringLabelledMatrix1D) value;
    double result = sum(ycns.getValues());
    ValueProperties properties = createCurrencyValueProperties(target).get();
    ComputedValue computedValue = new ComputedValue(new ValueSpecification(CS01_REQUIREMENT, specification, properties), result);

    //s_logger.warn("execute, returning " + results);
    return Collections.singleton(computedValue);
  }

  private double sum(double[] values) {
    double total = 0d;
    for (double value : values) {
      total += value;
    }
    return total;
  }
}
