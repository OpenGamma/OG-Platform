/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.calculator.MarketQuoteSensitivityCalculator;
import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertiesUtils;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.black.BlackDiscountingYCNSFXOptionFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates yield curve node sensitivities for FX options.
 * 
 * @deprecated Use {@link BlackDiscountingYCNSFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackYCNSFunction extends FXOptionBlackSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackYCNSFunction.class);
  private static final MarketQuoteSensitivityCalculator CALCULATOR = new MarketQuoteSensitivityCalculator(new ParameterSensitivityCalculator(
      PresentValueCurveSensitivityIRSCalculator.getInstance()));

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  public FXOptionBlackYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String putCurveCalculationConfigName = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveCalculationConfigName = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    final String resultCurveConfigName;
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
    final MultiCurveCalculationConfig resultCurveCalculationConfig = _curveCalculationConfigSource.getConfig(resultCurveConfigName);
    final String calculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    final String fullCurveName = curveName + "_" + curveCurrency;
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(curveCurrency)).getSensitivities();
    return getResult(inputs, calculationMethod, fullCurveName, data, curveSpec, sensitivitiesForCurrency, spec);
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
    final Set<String> curveCurrencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrencies == null || curveCurrencies.size() != 1) {
      s_logger.error("Did not specify a curve currency for requirement {}", desiredValue);
      return null;
    }
    final String callCurveName = Iterables.getOnlyElement(constraints.getValues(CALL_CURVE));
    final String putCurveName = Iterables.getOnlyElement(constraints.getValues(PUT_CURVE));
    final String curveName = Iterables.getOnlyElement(curveNames);
    if (!(curveName.equals(putCurveName) || curveName.equals(callCurveName))) {
      s_logger.info("Curve name {} did not match either put curve name {} or call curve name {}", new Object[] {curveName, putCurveName, callCurveName });
      return null;
    }
    final String callCurveCalculationConfigName = Iterables.getOnlyElement(constraints.getValues(CALL_CURVE_CALC_CONFIG));
    final String putCurveCalculationConfigName = Iterables.getOnlyElement(constraints.getValues(PUT_CURVE_CALC_CONFIG));
    final String surfaceName = Iterables.getOnlyElement(constraints.getValues(SURFACE));
    final String interpolatorName = Iterables.getOnlyElement(constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME));
    final String leftExtrapolatorName = Iterables.getOnlyElement(constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME));
    final String rightExtrapolatorName = Iterables.getOnlyElement(constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME));
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final String curveCurrency = Iterables.getOnlyElement(curveCurrencies);
    final String resultCurrency, resultCurveName, resultCurveConfigName;
    if (curveCurrency.equals(putCurrency.getCode())) {
      resultCurrency = putCurrency.getCode();
      resultCurveName = putCurveName;
      resultCurveConfigName = putCurveCalculationConfigName;
    } else if (curveCurrency.equals(callCurrency.getCode())) {
      resultCurrency = callCurrency.getCode();
      resultCurveName = callCurveName;
      resultCurveConfigName = callCurveCalculationConfigName;
    } else {
      return null;
    }
    final MultiCurveCalculationConfig resultCurveCalculationConfig = _curveCalculationConfigSource.getConfig(resultCurveConfigName);
    if (resultCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + resultCurveConfigName + " for currency " + resultCurrency);
      return null;
    }
    final String resultCurveCalculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    requirements.add(getCurveSensitivitiesRequirement(putCurveName, putCurveCalculationConfigName, callCurveName, callCurveCalculationConfigName, surfaceName, interpolatorName,
        leftExtrapolatorName, rightExtrapolatorName, curveCurrency, target));
    if (resultCurveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      s_logger.error("Cannot handle curves calculated using the FX implied method");
      return null;
    }
    requirements.add(getCurveSpecRequirement(resultCurrency, resultCurveName));
    requirements.add(getJacobianRequirement(Currency.of(resultCurrency), resultCurveConfigName, resultCurveCalculationConfig.getCalculationMethod()));
    if (resultCurveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(Currency.of(resultCurrency), resultCurveConfigName));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    String putCurveName = null;
    String putCurveCalculationConfig = null;
    String callCurveName = null;
    String callCurveCalculationConfig = null;
    String curveCurrency = null;
    final ValueProperties.Builder optionalProperties = ValueProperties.builder();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification specification = entry.getKey();
      final ValueRequirement requirement = entry.getValue();
      final ValueProperties constraints = requirement.getConstraints();
      if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        if (constraints.getProperties().contains(PUT_CURVE)) {
          putCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          putCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          final ValueProperties properties = ValuePropertiesUtils.removeAll(constraints.copy().get(), ValuePropertyNames.CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
          ValuePropertiesUtils.withAllOptional(optionalProperties, properties);
        } else if (constraints.getProperties().contains(CALL_CURVE)) {
          callCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          callCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          final ValueProperties properties = ValuePropertiesUtils.removeAll(constraints.copy().get(), ValuePropertyNames.CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
          ValuePropertiesUtils.withAllOptional(optionalProperties, properties);
        }
      } else if (specification.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = specification.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
      } else if (requirement.getValueName().equals(ValueRequirementNames.FX_CURVE_SENSITIVITIES)) {
        if (constraints.getProperties().contains(ValuePropertyNames.CURVE_CURRENCY)) {
          curveCurrency = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CURRENCY));
        }
      }
    }
    if (putCurveName == null || callCurveName == null || currencyPairConfigName == null || curveCurrency == null) {
      return null;
    }
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      s_logger.error("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
      return null;
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target, putCurveName, putCurveCalculationConfig,
        callCurveName, callCurveCalculationConfig, curveCurrency, baseQuotePair, optionalProperties.get()).get());
    return Collections.singleton(resultSpec);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target).withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig, final String callCurve,
      final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig, final String callCurve,
      final String callCurveCalculationConfig, final String curveCurrency, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    final ValueProperties.Builder properties = super
        .getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair, optionalProperties).withoutAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURRENCY, curveCurrency).withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair).withoutAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.CURRENCY, curveCurrency).with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency).with(ValuePropertyNames.CURVE, curveName);
    return properties;
  }

  private static ValueRequirement getCurveSpecRequirement(final String currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.CURRENCY.specification(Currency.of(currency)), properties);
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String putCurveName, final String putCurveCalculationConfig, final String callCurveName,
      final String callCurveCalculationConfig, final String surfaceName, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName,
      final String curveCurrency, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder().with(PUT_CURVE, putCurveName).with(CALL_CURVE, callCurveName).with(PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig).with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency).withOptional(ValuePropertyNames.CURVE_CURRENCY).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private static ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private static Set<ComputedValue> getResult(final FunctionInputs inputs, final String calculationMethod, final String fullCurveName, final YieldCurveBundle interpolatedCurves,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
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
