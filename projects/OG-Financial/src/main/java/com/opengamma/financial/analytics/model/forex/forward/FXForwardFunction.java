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
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public abstract class FXForwardFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the pay curve calculation configuration */
  public static final String PAY_CURVE_CALC_CONFIG = "PayCurveCalculationConfig";
  /** Property name for the receive curve calculation configuration */
  public static final String RECEIVE_CURVE_CALC_CONFIG = "ReceiveCurveCalculationConfig";
  private final String _valueRequirementName;

  public FXForwardFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    if (now.isAfter(security.accept(ForexVisitors.getExpiryVisitor()))) {
      throw new OpenGammaRuntimeException("FX forward " + payCurrency.getCode() + "/" + receiveCurrency + " has expired");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(PAY_CURVE_CALC_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(RECEIVE_CURVE_CALC_CONFIG);
    final String fullPayCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullReceiveCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final YieldAndDiscountCurve payCurve = getPayCurve(inputs, payCurrency, payCurveName, payCurveConfig);
    final YieldAndDiscountCurve receiveCurve = getReceiveCurve(inputs, receiveCurrency, receiveCurveName, receiveCurveConfig);
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    curveCurrency.put(fullPayCurveName, payCurrency);
    curveCurrency.put(fullReceiveCurveName, receiveCurrency);
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final YieldAndDiscountCurve[] curves;
    final String[] allCurveNames;
    curves = new YieldAndDiscountCurve[] {payCurve, receiveCurve};
    allCurveNames = new String[] {fullPayCurveName, fullReceiveCurveName};
    // Implementation note: The ForexSecurityConverter create the Forex with currency order pay/receive. The curve are passed in the same order.
    final ForexSecurityConverter converter = new ForexSecurityConverter(baseQuotePairs);
    final InstrumentDefinition<?> definition = security.accept(converter);
    final Forex forex = (Forex) definition.toDerivative(now, allCurveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueProperties.Builder properties = getResultProperties(target, desiredValue);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
    return getResult(forex, yieldCurves, target, desiredValues, inputs, spec, executionContext);
  }

  //TODO clumsy. Push the execute() method down into the functions and have getForward() and getData() methods
  protected abstract Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext);

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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ValueRequirement payFundingCurve = getPayCurveRequirement(payCurveName, payCurrency, payCurveCalculationConfig);
    final ValueRequirement receiveFundingCurve = getReceiveCurveRequirement(receiveCurveName, receiveCurrency, receiveCurveCalculationConfig);
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(payCurrency, receiveCurrency);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId());
    return Sets.newHashSet(payFundingCurve, receiveFundingCurve, pairQuoteRequirement);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  protected static ValueRequirement getPayCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .withOptional(ValuePropertyNames.PAY_CURVE);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, currency.getUniqueId(), properties.get());
  }

  protected static YieldAndDiscountCurve getPayCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getPayCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }

  protected static ValueRequirement getReceiveCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .withOptional(ValuePropertyNames.RECEIVE_CURVE);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, currency.getUniqueId(), properties.get());
  }

  protected static YieldAndDiscountCurve getReceiveCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getReceiveCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
