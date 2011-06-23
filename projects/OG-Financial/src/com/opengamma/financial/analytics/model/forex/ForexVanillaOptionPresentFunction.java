/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.forex.ForexOptionConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexVanillaOptionPresentFunction extends AbstractFunction.NonCompiledInvoker {
  private static final PresentValueForexCalculator CALCULATOR = PresentValueForexCalculator.getInstance();
  private final String _putCurveName;
  private final String _callCurveName;
  private final ForexOptionConverter _visitor;

  public ForexVanillaOptionPresentFunction(final String putCurveName, final String callCurveName) {
    Validate.notNull(putCurveName, "put curve name");
    Validate.notNull(callCurveName, "call curve name");
    _putCurveName = putCurveName;
    _callCurveName = callCurveName;
    _visitor = new ForexOptionConverter();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FXOptionSecurity security = (FXOptionSecurity) target.getSecurity();
    final ForexConverter<?> definition = _visitor.visitFXOptionSecurity(security);
    final ForexDerivative fxOption = definition.toDerivative(now, _putCurveName, _callCurveName);
    final MultipleCurrencyAmount presentValue = CALCULATOR.visit(fxOption);
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _putCurveName).with(ValuePropertyNames.CURVE, _callCurveName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, presentValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final ValueRequirement putCurve = YieldCurveFunction.getCurveRequirement(fxOption.getPutCurrency(), _putCurveName, _putCurveName, _putCurveName);
    final ValueRequirement callCurve = YieldCurveFunction.getCurveRequirement(fxOption.getCallCurrency(), _callCurveName, _callCurveName, _callCurveName);
    return Sets.newHashSet(putCurve, callCurve);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _putCurveName).with(ValuePropertyNames.CURVE, _callCurveName).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        properties));
  }

}
