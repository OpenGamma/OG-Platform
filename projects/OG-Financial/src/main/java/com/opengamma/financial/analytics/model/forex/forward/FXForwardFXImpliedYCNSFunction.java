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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class FXForwardFXImpliedYCNSFunction extends FXForwardSingleValuedFunction {
  /** Property for the currency that underlies the FX-implied curve */
  private static final String UNDERLYING_CURRENCY = "UnderlyingCurrency";

  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardFXImpliedYCNSFunction.class);
  private static final MarketQuoteSensitivityCalculator CALCULATOR =
      new MarketQuoteSensitivityCalculator(new ParameterSensitivityCalculator(PresentValueCurveSensitivityIRSCalculator.getInstance()));

  public FXForwardFXImpliedYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
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
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBFXForwardCurveDefinitionSource definitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final String underlyingCurrency = Iterables.getOnlyElement(desiredValue.getConstraints().getValues(UNDERLYING_CURRENCY));
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(Currency.of(curveCurrency), Currency.of(underlyingCurrency));
    final FXForwardCurveDefinition definition = definitionSource.getDefinition(curveName, currencyPair.toString());
    final Tenor[] tenors = definition.getTenors();
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
    final String fullCurveName = curveName + "_" + curveCurrency;
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(curveCurrency)).getSensitivities();
    final YieldCurveBundle dataForCurrency = new YieldCurveBundle();
    dataForCurrency.setCurve(fullCurveName, data.getCurve(fullCurveName));
    return getResult(inputs, resultCurveConfigName, fullCurveName, dataForCurrency, sensitivitiesForCurrency, spec, tenors);
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
    final String resultCurrency, resultCurveConfigName;
    if (!(curveName.equals(payCurveName) || curveName.equals(receiveCurveName))) {
      s_logger.info("Curve name {} did not match either pay curve name {} or receive curve name {}", new Object[] {curveName, payCurveName, receiveCurveName });
      return null;
    }
    if (currency.equals(payCurrency.getCode())) {
      resultCurrency = payCurrency.getCode();
      resultCurveConfigName = payCurveCalculationConfig;
    } else if (currency.equals(receiveCurrency.getCode())) {
      resultCurrency = receiveCurrency.getCode();
      resultCurveConfigName = receiveCurveCalculationConfig;
    } else {
      return null;
    }
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig resultCurveCalculationConfig = curveCalculationConfigSource.getConfig(resultCurveConfigName);
    if (resultCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + resultCurveConfigName + " for currency " + resultCurrency);
      return null;
    }
    if (resultCurveCalculationConfig.getExogenousConfigData() == null) {
      s_logger.info("Curve calculation config does not contain exogenous data");
      return null;
    }
    final Map.Entry<String, String[]> underlyingCurveConfigNames = resultCurveCalculationConfig.getExogenousConfigData().entrySet().iterator().next();
    final MultiCurveCalculationConfig underlyingConfig = curveCalculationConfigSource.getConfig(underlyingCurveConfigNames.getKey());
    if (underlyingConfig == null) {
      s_logger.error("Underlying config was null; tried {}", underlyingCurveConfigNames.getKey());
      return null;
    }
    final ComputationTargetSpecification foreignCurrencySpec = underlyingConfig.getTarget();
    if (!foreignCurrencySpec.getType().isTargetType(ComputationTargetType.CURRENCY)) {
      s_logger.error("Can only handle curves with currencies as ids at the moment");
      return null;
    }
    final Currency underlyingCurrency = ComputationTargetType.CURRENCY.resolve(foreignCurrencySpec.getUniqueId());
    final String resultCurveCalculationMethod = resultCurveCalculationConfig.getCalculationMethod();
    requirements.add(getCurveSensitivitiesRequirement(payCurveName, payCurveCalculationConfig, receiveCurveName, receiveCurveCalculationConfig, target,
        currency, underlyingCurrency.getCode()));
    if (!resultCurveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      return null;
    }
    final Currency ccy = Currency.of(resultCurrency);
    requirements.add(getJacobianRequirement(ccy, resultCurveConfigName, resultCurveCalculationConfig.getCalculationMethod()));
    requirements.add(getFXImpliedTransitionMatrixRequirement(ccy, resultCurveConfigName, resultCurveCalculationConfig.getCalculationMethod()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    String underlyingCurrency = null;
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
        } else if (constraints.getProperties().contains(ValuePropertyNames.RECEIVE_CURVE)) {
          receiveCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        }
      } else if (requirement.getValueName().equals(ValueRequirementNames.FX_CURVE_SENSITIVITIES)) {
        underlyingCurrency = requirement.getConstraint(UNDERLYING_CURRENCY);
        currency = requirement.getConstraint(ValuePropertyNames.CURRENCY);
      }
    }
    assert currencyPairConfigName != null;
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(payCurrency, receiveCurrency);
    if (baseQuotePair == null) {
      s_logger.error("Could not get base/quote pair for currency pair (" + payCurrency + ", " + receiveCurrency + ")");
      return null;
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target,
        payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig, baseQuotePair, currency, underlyingCurrency).get());
    return Collections.singleton(resultSpec);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(UNDERLYING_CURRENCY)
        .withAny(ValuePropertyNames.CURRENCY);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String receiveCurve,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String receiveCurve,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair, final String currency,
      final String underlyingCurrency) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED)
        .with(ValuePropertyNames.PAY_CURVE, payCurve)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurve)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .with(UNDERLYING_CURRENCY, underlyingCurrency)
        .with(ValuePropertyNames.CURRENCY, currency);
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
    final String underlyingCurrency = desiredValue.getConstraint(UNDERLYING_CURRENCY);
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(UNDERLYING_CURRENCY, underlyingCurrency)
        .with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String payCurveName, final String payCurveCalculationConfig, final String receiveCurveName,
      final String receiveCurveCalculationConfig, final ComputationTarget target, final String currency, final String underlyingCurrency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(UNDERLYING_CURRENCY, underlyingCurrency).withOptional(UNDERLYING_CURRENCY)
        .with(ValuePropertyNames.CURRENCY, currency).withOptional(ValuePropertyNames.CURRENCY)
        .get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getFXImpliedTransitionMatrixRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, ComputationTargetSpecification.of(currency), properties);
  }

  private static Set<ComputedValue> getResult(final FunctionInputs inputs, final String curveCalculationConfig, final String fullCurveName,
      final YieldCurveBundle interpolatedCurveForCurrency, final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec,
      final Tenor[] tenors) {
    if (sensitivitiesForCurrency.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting one set of sensitivities");
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);

    final DoubleMatrix1D result = CALCULATOR.calculateFromParRate(sensitivitiesForCurrency, interpolatedCurveForCurrency, jacobian);
    if (result.getNumberOfElements() != tenors.length) {
      throw new OpenGammaRuntimeException("Did not have one sensitivity per tenor");
    }
    final int n = result.getNumberOfElements();
    final Double[] keys = new Double[n];
    final double[] values = new double[n];
    final Object[] labels = new Object[n];
    for (int i = 0; i < n; i++) {
      keys[i] = Double.valueOf(i);
      values[i] = result.getEntry(i);
      labels[i] = tenors[i].getPeriod().toString();
    }
    final DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);

    final Object fxImpliedTransitionMatrixObject = inputs.getValue(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX);
    if (fxImpliedTransitionMatrixObject == null) {
      throw new OpenGammaRuntimeException("Could not get foreign Jacobian");
    }
    final double[][] arrayFx = FunctionUtils.decodeJacobian(fxImpliedTransitionMatrixObject);
    final DoubleMatrix2D fxImpliedTransitionMatrix = new DoubleMatrix2D(arrayFx);

    @SuppressWarnings("unused")
    final DoubleMatrix1D resultFx = CALCULATOR.calculateFromParRateFromTransition(sensitivitiesForCurrency, interpolatedCurveForCurrency, fxImpliedTransitionMatrix);
    // TODO: use it! [PLAT-3555]

    return Collections.singleton(new ComputedValue(spec, labelledMatrix));
  }
}
