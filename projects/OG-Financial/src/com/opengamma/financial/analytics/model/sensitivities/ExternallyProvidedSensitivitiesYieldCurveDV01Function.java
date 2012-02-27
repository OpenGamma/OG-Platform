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
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;

public class ExternallyProvidedSensitivitiesYieldCurveDV01Function extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesYieldCurveDV01Function.class);
  /**
   * The value name calculated by this function.
   */
  public static final String YCNS_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  public static final String DV01_REQUIREMENT = ValueRequirementNames.DV01;

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
    Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.warn("No curve name, returning null");
      return null;
    }
    String curveName = curveNames.iterator().next();
    s_logger.warn("Curve name is:" + curveName);
    ValueProperties valueProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURVE, curveName).get();
        //.withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        //.withAny(ValuePropertyNames.CURVE_CURRENCY).get();
    requirements.add(new ValueRequirement(YCNS_REQUIREMENT, target.toSpecification(), valueProperties));
    //s_logger.warn("getRequirements() returned " + requirements);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder externalProperties = createCurrencyValueProperties(target);
    externalProperties.withAny(ValuePropertyNames.CURVE);
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(DV01_REQUIREMENT, target.toSpecification(), externalProperties.get()));
    s_logger.warn("getResults(1) = " + results);
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveName = null;
    for (Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      if (entry.getKey().getValueName().equals(YCNS_REQUIREMENT)) {
        curveName = entry.getKey().getProperty(ValuePropertyNames.CURVE);
      }
    }
    assert curveName != null;
    ValueProperties valueProperties = createCurrencyValueProperties(target).with(ValuePropertyNames.CURVE, curveName).get();
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(DV01_REQUIREMENT, targetSpec, valueProperties));
    s_logger.warn("getResults(2) returning " + results);
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    ValueRequirement desiredValue = desiredValues.iterator().next();
    String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    ComputationTargetSpecification specification = target.toSpecification();
    Object value = inputs.getValue(new ValueRequirement(YCNS_REQUIREMENT, specification));
    if (!(value instanceof LabelledMatrix1D)) {
      s_logger.error("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivities result was not of type LabelledMatrix1D");
    }
    DoubleLabelledMatrix1D ycns = (DoubleLabelledMatrix1D) value;
    double result = sum(ycns.getValues());
    ValueProperties properties = createCurrencyValueProperties(target)
        .with(ValuePropertyNames.CURVE, curveName).get();
    ComputedValue computedValue = new ComputedValue(new ValueSpecification(DV01_REQUIREMENT, specification, properties), result);

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
