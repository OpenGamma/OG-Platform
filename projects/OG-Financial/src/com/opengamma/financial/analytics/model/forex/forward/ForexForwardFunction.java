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
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexForwardFunction extends AbstractFunction.NonCompiledInvoker {
  /** The advisory forward pay curve property */
  public static final String PROPERTY_PAY_FORWARD_CURVE = "PayForwardCurve";
  /** The pay curve calculation method property */
  public static final String PROPERTY_PAY_CURVE_CALCULATION_METHOD = "PayCurveCalculationMethod";
  /** The advisory forward receive curve property */
  public static final String PROPERTY_RECEIVE_FORWARD_CURVE = "ReceiveForwardCurve";
  /** The receive curve calculation method property */
  public static final String PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD = "ReceiveCurveCalculationMethod";
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
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(VISITOR);
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payForwardCurveName = desiredValue.getConstraint(PROPERTY_PAY_FORWARD_CURVE);
    final String receiveForwardCurveName = desiredValue.getConstraint(PROPERTY_RECEIVE_FORWARD_CURVE);
    final String payCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_PAY_CURVE_CALCULATION_METHOD);
    final String receiveCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    final String fullPutCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullCallCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final String[] curveNames;
    if (FXUtils.isInBaseQuoteOrder(payCurrency, receiveCurrency)) { // To get Base/quote in market standard order.
      curveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curveNames = new String[] {fullCallCurveName, fullPutCurveName};
    }
    final YieldAndDiscountCurve payFundingCurve = getCurve(inputs, payCurrency, payCurveName);
    final YieldAndDiscountCurve receiveFundingCurve = getCurve(inputs, receiveCurrency, receiveCurveName);
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
    final Forex forex = (Forex) definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueProperties.Builder properties = getResultProperties(payCurveName, payForwardCurveName, payCurveCalculationMethod, receiveCurveName, receiveForwardCurveName,
        receiveCurveCalculationMethod, target);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
    return getResult(forex, yieldCurves, spec);
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
    return target.getSecurity() instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payForwardCurveNames = constraints.getValues(PROPERTY_PAY_FORWARD_CURVE);
    if (payForwardCurveNames == null || payForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveForwardCurveNames = constraints.getValues(PROPERTY_RECEIVE_FORWARD_CURVE);
    if (receiveForwardCurveNames == null || receiveForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationMethods = constraints.getValues(PROPERTY_PAY_CURVE_CALCULATION_METHOD);
    if (payCurveCalculationMethods == null || payCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationMethods = constraints.getValues(PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    if (receiveCurveCalculationMethods == null || receiveCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String payCurveName = payCurveNames.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final String payForwardCurveName = payForwardCurveNames.iterator().next();
    final String receiveForwardCurveName = receiveForwardCurveNames.iterator().next();
    final String payCurveCalculationMethod = payCurveCalculationMethods.iterator().next();
    final String receiveCurveCalculationMethod = receiveCurveCalculationMethods.iterator().next();
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    final ValueRequirement payFundingCurve = getCurveRequirement(payCurveName, payForwardCurveName, payCurveName, payCurveCalculationMethod, payCurrency);
    final ValueRequirement receiveFundingCurve = getCurveRequirement(receiveCurveName, receiveForwardCurveName, receiveCurveName, receiveCurveCalculationMethod, receiveCurrency);
    return Sets.newHashSet(payFundingCurve, receiveFundingCurve);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final String payCurveName, final String payForwardCurveName, final String payCurveCalculationMethod,
      final String receiveCurveName, final String receiveForwardCurveName, final String receiveCurveCalculationMethod, final ComputationTarget target);

  private static ValueRequirement getCurveRequirement(final String curveName, final String forwardCurveName, final String fundingCurveName, final String calculationMethod, final Currency currency) {
    final ValueProperties.Builder properties;
    if (calculationMethod == InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME)
          .withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
          .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  private static YieldAndDiscountCurve getCurve(final FunctionInputs inputs, final Currency currency, final String curveName) {
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    final Object curveObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get()));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
