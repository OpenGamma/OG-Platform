/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.calculator.MarketQuoteSensitivityCalculator;
import com.opengamma.analytics.financial.curve.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class FXOptionBlackYCNSFunction extends FXOptionBlackSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackYCNSFunction.class);
  private static final MarketQuoteSensitivityCalculator CALCULATOR =
      new MarketQuoteSensitivityCalculator(new ParameterSensitivityCalculator(PresentValueCurveSensitivityIRSCalculator.getInstance()));

  public FXOptionBlackYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String putCurveCalculationConfigName = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveCalculationConfigName = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String resultCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    final String resultCurveConfigName;
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.error("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName });
      return null;
    }
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    if (curveCurrency.equals(putCurrency)) {
      resultCurveConfigName = putCurveCalculationConfigName;
    } else {
      resultCurveConfigName = callCurveCalculationConfigName;
    }
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final Object curveSpecObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve spec");
    }
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig resultCurveCalculationConfig = curveCalculationConfigSource.getConfig(resultCurveConfigName);
    final String calculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    final String fullCurveName = curveName + "_" + curveCurrency;
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(resultCurrency)).getSensitivities();
    return getResult(inputs, calculationMethod, fullCurveName, data, curveSpec, sensitivitiesForCurrency, spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final Set<String> currencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      s_logger.error("Did not specify a currency for requirement {}", desiredValue);
      return null;
    }
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
      s_logger.error("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName });
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
    final String resultCurveCalculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    requirements.add(getCurveSensitivitiesRequirement(putCurveName, putCurveCalculationConfigName, callCurveName, callCurveCalculationConfigName, surfaceName,
        interpolatorName, leftExtrapolatorName, rightExtrapolatorName, currency, resultCurrency, resultCurveName, target));
    if (resultCurveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      s_logger.error("Cannot handle curves calculated using the FX implied method");
      return null;
    }
    requirements.add(getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName));
    requirements.add(security.accept(ForexVisitors.getSpotIdentifierVisitor()));
    requirements.add(getCurveSpecRequirement(resultCurrency, resultCurveName));
    requirements.add(getJacobianRequirement(Currency.of(resultCurrency), resultCurveConfigName, resultCurveCalculationConfig.getCalculationMethod()));
    if (resultCurveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(Currency.of(resultCurrency), resultCurveConfigName));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currency = null;
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
        final ValueProperties constraints = key.getProperties();
        currency = key.getTargetSpecification().getUniqueId().getValue();
        curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
        break;
      }
    }
    assert currency != null;
    assert curveName != null;
    final String resultCurrency = getResultCurrency(target);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(),
        getResultProperties(resultCurrency, currency, curveName).get());
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
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target))
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE);
  }

  private ValueProperties.Builder getResultProperties(final String resultCurrency, final String currency, final String curveName) {
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
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String putCurveCalculationConfig = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveCalculationConfig = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String resultCurrency = getResultCurrency(target);
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

  private static ValueRequirement getCurveSpecRequirement(final String currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, Currency.of(currency).getUniqueId(), properties);
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

  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, currency, properties);
  }

  private static ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, currency, properties);
  }

  private static Set<ComputedValue> getResult(final FunctionInputs inputs, final String calculationMethod,
      final String fullCurveName, final YieldCurveBundle interpolatedCurves, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    if (calculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING)) {
      final DoubleMatrix1D result = CALCULATOR.calculateFromParRate(sensitivitiesForCurrency, interpolatedCurves, jacobian);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurves, result, curveSpec, spec);
    }
    final Object couponSensitivityObject = inputs.getValue(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    if (couponSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    }
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
    final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(sensitivitiesForCurrency, interpolatedCurves, couponSensitivity, jacobian);
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurves, result, curveSpec, spec);
  }
}
