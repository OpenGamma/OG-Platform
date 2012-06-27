/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexForwardFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the pay curve calculation configuration */
  public static final String PAY_CURVE_CALC_CONFIG = "PayCurveCalculationConfig";
  /** Property name for the receive curve calculation configuration */
  public static final String RECEIVE_CURVE_CALC_CONFIG = "ReceiveCurveCalculationConfig";
  private static final ForexSecurityConverter VISITOR = new ForexSecurityConverter();
  private final String _valueRequirementName;

  public ForexForwardFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency, receiveCurrency;
    if (security instanceof FXForwardSecurity) {
      final FXForwardSecurity forward = (FXForwardSecurity) security;
      payCurrency = forward.getPayCurrency();
      receiveCurrency = forward.getReceiveCurrency();
    } else {
      final NonDeliverableFXForwardSecurity ndf = (NonDeliverableFXForwardSecurity) security;
      payCurrency = ndf.getPayCurrency();
      receiveCurrency = ndf.getReceiveCurrency();
    }
    final InstrumentDefinition<?> definition = security.accept(VISITOR);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(PAY_CURVE_CALC_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(RECEIVE_CURVE_CALC_CONFIG);
    final String fullPutCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullCallCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final YieldAndDiscountCurve payFundingCurve = getCurve(inputs, payCurrency, payCurveName, payCurveConfig);
    final YieldAndDiscountCurve receiveFundingCurve = getCurve(inputs, receiveCurrency, receiveCurveName, receiveCurveConfig);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    curveCurrency.put(fullPutCurveName, payCurrency);
    curveCurrency.put(fullCallCurveName, receiveCurrency);
    final String[] allCurveNames;
    if (FXUtils.isInBaseQuoteOrder(payCurrency, receiveCurrency)) { // To get Base/quote in market standard order.
      curves = new YieldAndDiscountCurve[] {payFundingCurve, receiveFundingCurve};
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {receiveFundingCurve, payFundingCurve};
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName};
    }
    try {
      final Forex forex = (Forex) definition.toDerivative(now, allCurveNames);
      final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
      final ValueProperties.Builder properties = getResultProperties(payCurveName, receiveCurveName, payCurveConfig, receiveCurveConfig, target);
      final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
      return getResult(forex, yieldCurves, spec);
    } catch (final IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Exception for security " + security + "; reason: " + e.getMessage());
    }
  }

  protected abstract Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity || target.getSecurity() instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveConfigNames = constraints.getValues(PAY_CURVE_CALC_CONFIG);
    if (payCurveConfigNames == null || payCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveConfigNames = constraints.getValues(RECEIVE_CURVE_CALC_CONFIG);
    if (receiveCurveConfigNames == null || receiveCurveConfigNames.size() != 1) {
      return null;
    }
    final String payCurveName = payCurveNames.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final String payCurveCalculationConfig = payCurveConfigNames.iterator().next();
    final String receiveCurveCalculationConfig = receiveCurveConfigNames.iterator().next();
    final Currency payCurrency, receiveCurrency;
    final Security security = target.getSecurity();
    if (security instanceof FXForwardSecurity) {
      final FXForwardSecurity forward = (FXForwardSecurity) security;
      payCurrency = forward.getPayCurrency();
      receiveCurrency = forward.getReceiveCurrency();
    } else {
      final NonDeliverableFXForwardSecurity ndf = (NonDeliverableFXForwardSecurity) security;
      payCurrency = ndf.getPayCurrency();
      receiveCurrency = ndf.getReceiveCurrency();
    }
    final ValueRequirement payFundingCurve = getCurveRequirement(payCurveName, payCurrency, payCurveCalculationConfig);
    final ValueRequirement receiveFundingCurve = getCurveRequirement(receiveCurveName, receiveCurrency, receiveCurveCalculationConfig);
    return Sets.newHashSet(payFundingCurve, receiveFundingCurve);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final String payCurveName, final String receiveCurveName, final String payCurveCalculationConfig,
      final String receiveCurveCalculationConfig, final ComputationTarget target);

  private static ValueRequirement getCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected static YieldAndDiscountCurve getCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
