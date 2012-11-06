/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PV01ForexCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class FXOptionBlackPV01Function extends FXOptionBlackSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackPV01Function.class);
  private static final PV01ForexCalculator CALCULATOR = PV01ForexCalculator.getInstance();

  public FXOptionBlackPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String resultCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.error("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName});
      return null;
    }
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final String fullCurveName = curveName + "_" + currency;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(resultCurrency)).getSensitivities();
    final Map<String, Double> pv01 = forex.accept(CALCULATOR, sensitivitiesForCurrency);
    if (!pv01.containsKey(fullCurveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + fullCurveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(fullCurveName)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveConfigNames = constraints.getValues(PUT_CURVE_CALC_CONFIG);
    if (putCurveConfigNames == null || putCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveConfigNames = constraints.getValues(CALL_CURVE_CALC_CONFIG);
    if (callCurveConfigNames == null || callCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final Set<String> currencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      s_logger.error("Did not specify a curve currency for requirement {}", desiredValue);
      return null;
    }
    final String putCurveName = putCurveNames.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    final String putCurveCalculationConfigName = putCurveConfigNames.iterator().next();
    final String callCurveCalculationConfigName = callCurveConfigNames.iterator().next();
    final String currency = currencies.iterator().next();
    final String curveName = curveNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final String interpolatorName = interpolatorNames.iterator().next();
    final String leftExtrapolatorName = leftExtrapolatorNames.iterator().next();
    final String rightExtrapolatorName = rightExtrapolatorNames.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(putCurveName, putCurrency, putCurveCalculationConfigName);
    final ValueRequirement callFundingCurve = getCurveRequirement(callCurveName, callCurrency, callCurveCalculationConfigName);
    final String resultCurrency, resultCurveName, resultCurveConfigName;
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.info("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName});
      return null;
    }
    if (currency.equals(putCurrency.getCode())) {
      resultCurrency = putCurrency.getCode();
      resultCurveName = putCurveName;
      resultCurveConfigName = putCurveCalculationConfigName;
    } else if (currency.equals(callCurrency.getCode())) {
      resultCurrency = callCurrency.getCode();
      resultCurveName = callCurveName;
      resultCurveConfigName = callCurveCalculationConfigName;
    } else {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(putFundingCurve);
    requirements.add(callFundingCurve);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig resultCurveCalculationConfig = curveCalculationConfigSource.getConfig(resultCurveConfigName);
    if (resultCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + resultCurveConfigName + " for currency " + resultCurrency);
      return null;
    }
    requirements.add(getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName));
    requirements.add(getCurveSensitivitiesRequirement(putCurveName, putCurveCalculationConfigName, callCurveName, callCurveCalculationConfigName, surfaceName,
        interpolatorName, leftExtrapolatorName, rightExtrapolatorName, currency, resultCurrency, resultCurveName, target));
    requirements.add(new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    String currency = null;
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(ValueRequirementNames.FX_CURVE_SENSITIVITIES)) {
        final ValueProperties constraints = value.getConstraints();
        currency = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY).iterator().next();
        curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
      }
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = key.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
      }
    }
    assert currency != null;
    assert curveName != null;
    assert currencyPairConfigName != null;
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(), getResultProperties(target, baseQuotePair, curveName).get());
    return Collections.singleton(resultSpec);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PUT_CURVE)
        .withAny(CALL_CURVE)
        .withAny(PUT_CURVE_CALC_CONFIG)
        .withAny(CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE);
  }

  private ValueProperties.Builder getResultProperties(final ComputationTarget target, final CurrencyPair baseQuotePair, final String curveName) {
    final String resultCurrency = getResultCurrency(target, baseQuotePair);
    return createValueProperties()
        .withAny(PUT_CURVE)
        .withAny(CALL_CURVE)
        .withAny(PUT_CURVE_CALC_CONFIG)
        .withAny(CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, resultCurrency)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .with(ValuePropertyNames.CURVE, curveName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String putCurveCalculationConfig = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveCalculationConfig = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String resultCurrency = getResultCurrency(target, baseQuotePair);
    return createValueProperties()
        .with(PUT_CURVE, putCurveName)
        .with(CALL_CURVE, callCurveName)
        .with(PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(ValuePropertyNames.CURRENCY, resultCurrency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName);
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String putCurveName, final String putCurveCalculationConfig, final String callCurveName,
      final String callCurveCalculationConfig, final String surfaceName, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName, final String currency, final String resultCurrency, final String resultCurveName, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder()
        .with(PUT_CURVE, putCurveName)
        .with(CALL_CURVE, callCurveName)
        .with(PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(ValuePropertyNames.CURRENCY, resultCurrency).withOptional(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).withOptional(ValuePropertyNames.CURVE_CURRENCY)
        .with(ValuePropertyNames.CURVE, resultCurveName).withOptional(ValuePropertyNames.CURVE).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
