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
import com.opengamma.analytics.financial.calculator.MarketQuoteSensitivityCalculator;
import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
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
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.ImpliedDepositCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.model.discounting.DiscountingYCNSFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates yield curve node sensitivities for FX forwards.
 * 
 * @deprecated Use {@link DiscountingYCNSFunction}
 */
@Deprecated
public class FXForwardYCNSFunction extends FXForwardSingleValuedFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardYCNSFunction.class);
  private static final MarketQuoteSensitivityCalculator CALCULATOR = new MarketQuoteSensitivityCalculator(new ParameterSensitivityCalculator(
      PresentValueCurveSensitivityIRSCalculator.getInstance()));

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  public FXForwardYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String payCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    final String resultCurveConfigName;
    final String payCurrency = security.getPayCurrency().getCode();
    if (curveCurrency.equals(payCurrency)) {
      resultCurveConfigName = payCurveCalculationConfigName;
    } else {
      resultCurveConfigName = receiveCurveCalculationConfigName;
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
    final YieldCurveBundle dataForCurrency = new YieldCurveBundle();
    dataForCurrency.setCurve(fullCurveName, data.getCurve(fullCurveName));
    return getResult(inputs, calculationMethod, fullCurveName, dataForCurrency, curveSpec, sensitivitiesForCurrency, spec);
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
    final String resultCurrency, resultCurveName, resultCurveConfigName;
    if (!(curveName.equals(payCurveName) || curveName.equals(receiveCurveName))) {
      s_logger.info("Curve name {} did not match either pay curve name {} or receive curve name {}", new Object[] {curveName, payCurveName, receiveCurveName });
      return null;
    }
    if (currency.equals(payCurrency.getCode())) {
      resultCurrency = payCurrency.getCode();
      resultCurveName = payCurveName;
      resultCurveConfigName = payCurveCalculationConfig;
    } else if (currency.equals(receiveCurrency.getCode())) {
      resultCurrency = receiveCurrency.getCode();
      resultCurveName = receiveCurveName;
      resultCurveConfigName = receiveCurveCalculationConfig;
    } else {
      return null;
    }
    final MultiCurveCalculationConfig resultCurveCalculationConfig = _curveCalculationConfigSource.getConfig(resultCurveConfigName);
    if (resultCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + resultCurveConfigName + " for currency " + resultCurrency);
      return null;
    }
    final String resultCurveCalculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    requirements.add(getCurveSensitivitiesRequirement(payCurveName, payCurveCalculationConfig, receiveCurveName, receiveCurveCalculationConfig, target, currency));
    if (resultCurveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
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
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String payCurveCalculationMethod = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    String receiveCurveCalculationMethod = null;
    String currency = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification specification = entry.getKey();
      final ValueRequirement requirement = entry.getValue();
      if (specification.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = specification.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
      } else if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        final ValueProperties constraints = requirement.getConstraints();
        if (constraints.getProperties().contains(ValuePropertyNames.PAY_CURVE)) {
          payCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          payCurveCalculationMethod = specification.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
        } else if (constraints.getProperties().contains(ValuePropertyNames.RECEIVE_CURVE)) {
          receiveCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          receiveCurveCalculationMethod = specification.getProperty(ValuePropertyNames.CURVE_CALCULATION_METHOD);
        }
      } else if (requirement.getValueName().equals(ValueRequirementNames.FX_CURVE_SENSITIVITIES)) {
        currency = requirement.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
      }
    }
    if (currencyPairConfigName == null) {
      return null;
    }
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final String curve, curveCalculationMethod;
    if (payCurrency.getCode().equals(currency)) {
      curve = payCurveName;
      curveCalculationMethod = payCurveCalculationMethod;
    } else {
      curve = receiveCurveName;
      curveCalculationMethod = receiveCurveCalculationMethod;
    }
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(payCurrency, receiveCurrency);
    if (baseQuotePair == null) {
      s_logger.error("Could not get base/quote pair for currency pair (" + payCurrency + ", " + receiveCurrency + ")");
      return null;
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(payCurveName, receiveCurveName,
        payCurveCalculationConfig, receiveCurveCalculationConfig, currency, curve, curveCalculationMethod).get());
    return Collections.singleton(resultSpec);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING,
            ImpliedDepositCurveFunction.IMPLIED_DEPOSIT).withAny(ValuePropertyNames.PAY_CURVE).withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String receiveCurve, final String payCurveCalculationConfig,
      final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final String payCurve, final String receiveCurve, final String payCurveCalculationConfig, final String receiveCurveCalculationConfig,
      final String currency, final String curve, final String curveCalculationMethod) {
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).with(ValuePropertyNames.PAY_CURVE, payCurve).with(ValuePropertyNames.RECEIVE_CURVE, receiveCurve)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig).with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURVE, curve).with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).with(ValuePropertyNames.PAY_CURVE, payCurveName).with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig).with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  private static ValueRequirement getCurveSpecRequirement(final String currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(Currency.of(currency)), properties);
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String payCurveName, final String payCurveCalculationConfig, final String receiveCurveName,
      final String receiveCurveCalculationConfig, final ComputationTarget target, final String curveCurrency) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, payCurveName).with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig).with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING).with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .withOptional(ValuePropertyNames.CURVE_CURRENCY).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency), properties);
  }

  private static Set<ComputedValue> getResult(final FunctionInputs inputs, final String calculationMethod, final String fullCurveName, final YieldCurveBundle interpolatedCurveForCurrency,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    if (calculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING) || calculationMethod.equals(ImpliedDepositCurveFunction.IMPLIED_DEPOSIT)) {
      final DoubleMatrix1D result = CALCULATOR.calculateFromParRate(sensitivitiesForCurrency, interpolatedCurveForCurrency, jacobian);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurveForCurrency, result, curveSpec, spec);
    }
    final Object couponSensitivityObject = inputs.getValue(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    if (couponSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    }
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
    final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(sensitivitiesForCurrency, interpolatedCurveForCurrency, couponSensitivity, jacobian);
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, interpolatedCurveForCurrency, result, curveSpec, spec);
  }
}
