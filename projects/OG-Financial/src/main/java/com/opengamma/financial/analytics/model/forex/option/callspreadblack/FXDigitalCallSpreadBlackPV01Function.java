/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
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
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class FXDigitalCallSpreadBlackPV01Function extends FXDigitalCallSpreadBlackSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXDigitalCallSpreadBlackPV01Function.class);
  private static final PV01ForexCalculator CALCULATOR = PV01ForexCalculator.getInstance();

  public FXDigitalCallSpreadBlackPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String putCurveName = desiredValue.getConstraint(FXOptionBlackFunction.PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(FXOptionBlackFunction.CALL_CURVE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String resultCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.error("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName });
      return null;
    }
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final String fullCurveName = curveName + "_" + currency;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(resultCurrency)).getSensitivities();
    final Map<String, Double> pv01 = fxDigital.accept(CALCULATOR, sensitivitiesForCurrency);
    if (!pv01.containsKey(fullCurveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + fullCurveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(fullCurveName)));
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
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String putCurveName = desiredValue.getConstraint(FXOptionBlackFunction.PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(FXOptionBlackFunction.CALL_CURVE);
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.info("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName });
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final String currency = Iterables.getOnlyElement(currencies);
    if (!(currency.equals(putCurrency.getCode())) || currency.equals(callCurrency.getCode())) {
      s_logger.info("Currency {} did not match either put currency {} or call currency {}", new Object[] {currency, putCurrency, callCurrency });
      return null;
    }
    requirements.add(getCurveSensitivitiesRequirement(currency, currency, curveName, desiredValue, target));
    requirements.add(new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL));
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    return desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String currency, final String resultCurrency, final String resultCurveName,
      final ValueRequirement desiredValue, final ComputationTarget target) {
    final String putCurveName = desiredValue.getConstraint(FXOptionBlackFunction.PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(FXOptionBlackFunction.CALL_CURVE);
    final String putCurveCalculationConfig = desiredValue.getConstraint(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    final String callCurveCalculationConfig = desiredValue.getConstraint(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String callSpread = desiredValue.getConstraint(PROPERTY_CALL_SPREAD_VALUE);
    final ValueProperties properties = ValueProperties.builder()
        .with(FXOptionBlackFunction.PUT_CURVE, putCurveName)
        .with(FXOptionBlackFunction.CALL_CURVE, callCurveName)
        .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, CALL_SPREAD_BLACK_METHOD)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(PROPERTY_CALL_SPREAD_VALUE, callSpread)
        .with(ValuePropertyNames.CURRENCY, resultCurrency).withOptional(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).withOptional(ValuePropertyNames.CURVE_CURRENCY)
        .with(ValuePropertyNames.CURVE, resultCurveName).withOptional(ValuePropertyNames.CURVE).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

}
