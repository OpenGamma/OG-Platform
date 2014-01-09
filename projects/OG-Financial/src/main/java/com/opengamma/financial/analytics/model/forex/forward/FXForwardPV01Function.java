/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import static com.opengamma.engine.value.ValuePropertyNames.PAY_CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.RECEIVE_CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PV01ForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.discounting.DiscountingPV01Function;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the PV01 of an FX forward
 * 
 * @deprecated Use {@link DiscountingPV01Function}
 */
@Deprecated
public class FXForwardPV01Function extends FXForwardSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPV01Function.class);
  private static final PV01ForexCalculator CALCULATOR = PV01ForexCalculator.getInstance();

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  public FXForwardPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final String fullCurveName = curveName + "_" + currency;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(currency)).getSensitivities();
    final Map<String, Double> pv01 = fxForward.accept(CALCULATOR, sensitivitiesForCurrency);
    if (!pv01.containsKey(fullCurveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + fullCurveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(fullCurveName)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_FORWARD_SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
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
    final String payCurveName = Iterables.getOnlyElement(constraints.getValues(PAY_CURVE));
    final String receiveCurveName = Iterables.getOnlyElement(constraints.getValues(RECEIVE_CURVE));
    final String payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(PAY_CURVE_CALCULATION_CONFIG));
    final String receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(RECEIVE_CURVE_CALCULATION_CONFIG));
    final String currency = Iterables.getOnlyElement(currencies);
    final String curveName = Iterables.getOnlyElement(curveNames);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final String resultCurveConfigName;
    if (!(curveName.equals(payCurveName) || curveName.equals(receiveCurveName))) {
      s_logger.info("Curve name {} did not match either pay curve name {} or receive curve name {}", new Object[] {curveName, payCurveName, receiveCurveName });
      return null;
    }
    if (currency.equals(payCurrency.getCode())) {
      resultCurveConfigName = payCurveCalculationConfig;
    } else if (currency.equals(receiveCurrency.getCode())) {
      resultCurveConfigName = receiveCurveCalculationConfig;
    } else {
      return null;
    }
    final MultiCurveCalculationConfig resultCurveCalculationConfig = _curveCalculationConfigSource.getConfig(resultCurveConfigName);
    if (resultCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + resultCurveConfigName + " for currency " + currency);
      return null;
    }
    requirements.add(getCurveSensitivitiesRequirement(payCurveName, payCurveCalculationConfig, receiveCurveName, receiveCurveCalculationConfig, target));
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target).withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String receiveCurve, final String payCurveCalculationConfig,
      final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    final ValueProperties.Builder properties = super.getResultProperties(target, payCurve, receiveCurve, payCurveCalculationConfig, receiveCurveCalculationConfig, baseQuotePair)
        .withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue).with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURVE, curveName);
    return properties;
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String payCurveName, final String payCurveCalculationConfig, final String receiveCurveName,
      final String receiveCurveCalculationConfig, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, payCurveName).with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig).with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }
}
