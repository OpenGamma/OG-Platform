/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexForwardFunction extends AbstractFunction.NonCompiledInvoker {
  private ForexSecurityConverter _visitor;
  private SecuritySource _securitySource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _visitor = new ForexSecurityConverter(_securitySource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = _visitor.visitFXForwardSecurity(security);
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    final String fullPayCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullReceiveCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final Object payCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(payCurrency, payCurveName, null, null));
    if (payCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + payCurveName + " curve");
    }
    final YieldAndDiscountCurve payCurve = (YieldAndDiscountCurve) payCurveObject;
    final Object receiveCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveCurveName, null, null));
    if (receiveCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + receiveCurveName + " curve");
    }
    final YieldAndDiscountCurve receiveCurve = (YieldAndDiscountCurve) receiveCurveObject;
    final String[] curveNames = new String[] {fullPayCurveName, fullReceiveCurveName};
    final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {payCurve, receiveCurve};
    final Forex fxForward = (Forex) definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(curveNames, curves);
    return getResult(fxForward, yieldCurves, inputs, target, payCurveName, receiveCurveName);
  }

  protected abstract Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String payCurveName, final String receiveCurveName);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> payCurveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final String payCurveName = payCurveNames.iterator().next();
    final Set<String> receiveCurveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final ValueRequirement payCurve = YieldCurveFunction.getCurveRequirement(fxForward.getPayCurrency(), payCurveName, null, null);
    final ValueRequirement receiveCurve = YieldCurveFunction.getCurveRequirement(fxForward.getReceiveCurrency(), receiveCurveName, null, null);
    return Sets.newHashSet(payCurve, receiveCurve);
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }
}
