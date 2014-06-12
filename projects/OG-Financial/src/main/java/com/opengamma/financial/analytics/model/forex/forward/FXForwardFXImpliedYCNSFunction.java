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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingYCNSFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates yield curve node sensitivities for yield curves constructed using the FX implied method.
 * 
 * @deprecated There is no longer any need to treat FX-implied curves differently; see {@link DiscountingYCNSFunction}
 */
@Deprecated
public class FXForwardFXImpliedYCNSFunction extends FXForwardSingleValuedFunction {
  /** Property for the currency that underlies the FX-implied curve */
  private static final String UNDERLYING_CURRENCY = "UnderlyingCurrency";

  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardFXImpliedYCNSFunction.class);
  private static final MarketQuoteSensitivityCalculator CALCULATOR = new MarketQuoteSensitivityCalculator(new ParameterSensitivityCalculator(
      PresentValueCurveSensitivityIRSCalculator.getInstance()));

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  public FXForwardFXImpliedYCNSFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Object curveCalculationConfigObject = inputs.getValue(ValueRequirementNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve calculation configuration");
    }
    final MultiCurveCalculationConfig curveCalculationConfig = (MultiCurveCalculationConfig) curveCalculationConfigObject;
    final Object curveSensitivitiesObject = inputs.getValue(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    if (curveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get curve sensitivities");
    }
    final MultipleCurrencyInterestRateCurveSensitivity curveSensitivities = (MultipleCurrencyInterestRateCurveSensitivity) curveSensitivitiesObject;
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final String fullCurveName = curveName + "_" + curveCurrency;
    if (curveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(payCurrency, receiveCurrency);
      final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(curveName, currencyPair.toString());
      if (definition == null) {
        throw new OpenGammaRuntimeException("Could not get FX forward curve definition called " + curveName + " for currency pair " + currencyPair);
      }
      final Tenor[] tenors = definition.getTenorsArray();
      final YieldCurveBundle interpolatedCurveForCurrency = new YieldCurveBundle();
      interpolatedCurveForCurrency.setCurve(fullCurveName, data.getCurve(fullCurveName));
      final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(curveCurrency)).getSensitivities();
      return getFXImpliedSensitivities(inputs, tenors, interpolatedCurveForCurrency, sensitivitiesForCurrency, spec);
    }
    String otherCurveCurrency, otherCurveName;
    if (curveCurrency.equals(payCurrency.getCode())) {
      otherCurveCurrency = receiveCurrency.getCode();
      otherCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    } else {
      otherCurveCurrency = payCurrency.getCode();
      otherCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    }
    final YieldCurveBundle interpolatedCurveForCurrency = new YieldCurveBundle();
    final String fullOtherCurveName = otherCurveName + "_" + otherCurveCurrency;
    interpolatedCurveForCurrency.setCurve(fullOtherCurveName, data.getCurve(fullOtherCurveName));
    final Object curveSpecObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get interpolated yield curve specification for " + fullCurveName);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final Map<String, List<DoublesPair>> sensitivitiesForCurrency = curveSensitivities.getSensitivity(Currency.of(otherCurveCurrency)).getSensitivities();
    final ValueSpecification adjustedSpec = new ValueSpecification(spec.getValueName(), spec.getTargetSpecification(), desiredValue.getConstraints().copy().get());
    return getExogenousSensitivities(inputs, curveSpec, interpolatedCurveForCurrency, sensitivitiesForCurrency, adjustedSpec);
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
    final String payCurveName = Iterables.getOnlyElement(constraints.getValues(PAY_CURVE));
    final String receiveCurveName = Iterables.getOnlyElement(constraints.getValues(RECEIVE_CURVE));
    final String curveName = Iterables.getOnlyElement(curveNames);
    if (!(curveName.equals(payCurveName) || curveName.equals(receiveCurveName))) {
      s_logger.info("Curve name {} did not match either pay curve name {} or receive curve name {}", new Object[] {curveName, payCurveName, receiveCurveName });
      return null;
    }
    final String payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(PAY_CURVE_CALCULATION_CONFIG));
    final String receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(RECEIVE_CURVE_CALCULATION_CONFIG));
    final String curveCurrency = Iterables.getOnlyElement(curveCurrencies);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    final String curveCalculationConfigName;
    final String otherCurveCurrency, otherCurveCalculationConfigName;
    if (curveCurrency.equals(payCurrency)) {
      curveCalculationConfigName = payCurveCalculationConfig;
      otherCurveCurrency = receiveCurrency;
      otherCurveCalculationConfigName = receiveCurveCalculationConfig;
    } else {
      curveCalculationConfigName = receiveCurveCalculationConfig;
      otherCurveCurrency = payCurrency;
      otherCurveCalculationConfigName = payCurveCalculationConfig;
    }
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    requirements.add(getCurveCalculationConfigRequirement(curveCalculationConfigName));
    if (curveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      final String exogenousCurrency = otherCurveCurrency;
      requirements.add(getCurveSensitivitiesRequirement(payCurveName, payCurveCalculationConfig, receiveCurveName, receiveCurveCalculationConfig, target, curveCurrency, exogenousCurrency));
      requirements.add(getJacobianRequirement(curveCurrency, curveCalculationConfigName, FXImpliedYieldCurveFunction.FX_IMPLIED));
    } else {
      final String exogenousCurrency = curveCurrency;
      requirements.add(getCurveSensitivitiesRequirement(payCurveName, payCurveCalculationConfig, receiveCurveName, receiveCurveCalculationConfig, target, otherCurveCurrency,
          exogenousCurrency));
      final ComputationTargetSpecification exogenousSpec = ComputationTargetSpecification.of(Currency.of(exogenousCurrency));
      requirements.add(getFXImpliedTransitionMatrixRequirement(otherCurveCurrency, otherCurveCalculationConfigName, FXImpliedYieldCurveFunction.FX_IMPLIED));
      requirements.add(getCurveSpecRequirement(exogenousSpec, curveName));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    String underlyingCurrency = null;
    String currency = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement requirement = entry.getValue();
      if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
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
    final ValueSpecification result = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(payCurveName, receiveCurveName,
        payCurveCalculationConfig, receiveCurveCalculationConfig, currency, underlyingCurrency).get());
    return Collections.singleton(result);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED).withAny(ValuePropertyNames.PAY_CURVE).withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE).withAny(UNDERLYING_CURRENCY).withAny(ValuePropertyNames.CURRENCY);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String receiveCurve, final String payCurveCalculationConfig,
      final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    throw new UnsupportedOperationException();
  }

  protected ValueProperties.Builder getResultProperties(final String payCurve, final String receiveCurve, final String payCurveCalculationConfig, final String receiveCurveCalculationConfig,
      final String currency, final String underlyingCurrency) {
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED).with(ValuePropertyNames.PAY_CURVE, payCurve)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurve).with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig).withAny(ValuePropertyNames.CURVE_CURRENCY).withAny(ValuePropertyNames.CURVE)
        .with(UNDERLYING_CURRENCY, underlyingCurrency).with(ValuePropertyNames.CURRENCY, currency);
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
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXImpliedYieldCurveFunction.FX_IMPLIED).with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig).with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURVE, curveName)
        .with(UNDERLYING_CURRENCY, underlyingCurrency).with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  private static ValueRequirement getCurveSensitivitiesRequirement(final String payCurveName, final String payCurveCalculationConfig, final String receiveCurveName,
      final String receiveCurveCalculationConfig, final ComputationTarget target, final String currency, final String exogenousCurrency) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, payCurveName).with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig).with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING).with(UNDERLYING_CURRENCY, exogenousCurrency).withOptional(UNDERLYING_CURRENCY)
        .with(ValuePropertyNames.CURRENCY, currency).withOptional(ValuePropertyNames.CURRENCY).get();
    return new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private static ValueRequirement getJacobianRequirement(final String currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(Currency.of(currency)), properties);
  }

  private static ValueRequirement getFXImpliedTransitionMatrixRequirement(final String currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, ComputationTargetSpecification.of(Currency.of(currency)), properties);
  }

  private static ValueRequirement getCurveSpecRequirement(final ComputationTargetSpecification spec, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, spec, properties);
  }

  private static ValueRequirement getCurveCalculationConfigRequirement(final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
    return new ValueRequirement(ValueRequirementNames.CURVE_CALCULATION_CONFIG, ComputationTargetSpecification.NULL, properties);
  }

  private static Set<ComputedValue> getFXImpliedSensitivities(final FunctionInputs inputs, final Tenor[] tenors, final YieldCurveBundle interpolatedCurveForCurrency,
      final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
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
    return Collections.singleton(new ComputedValue(spec, labelledMatrix));
  }

  private static Set<ComputedValue> getExogenousSensitivities(final FunctionInputs inputs, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle interpolatedCurveForCurrency, final Map<String, List<DoublesPair>> sensitivitiesForCurrency, final ValueSpecification spec) {
    final Object fxImpliedTransitionMatrixObject = inputs.getValue(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX);
    if (fxImpliedTransitionMatrixObject == null) {
      throw new OpenGammaRuntimeException("Could not get foreign Jacobian");
    }
    final double[][] arrayFx = FunctionUtils.decodeJacobian(fxImpliedTransitionMatrixObject);
    final DoubleMatrix2D fxImpliedTransitionMatrix = new DoubleMatrix2D(arrayFx);
    final DoubleMatrix1D resultFx = CALCULATOR.calculateFromParRateFromTransition(sensitivitiesForCurrency, interpolatedCurveForCurrency, fxImpliedTransitionMatrix);
    if (curveSpec.getStrips().size() != resultFx.getNumberOfElements()) {
      throw new OpenGammaRuntimeException("Number of yield curve node sensitivities did not match the number of nodes in the curve specification");
    }
    // TODO: use it! [PLAT-3555]
    final int n = resultFx.getNumberOfElements();
    final Double[] keys = new Double[n];
    final double[] values = new double[n];
    final Object[] labels = new Object[n];
    final Iterator<FixedIncomeStripWithSecurity> iterator = curveSpec.getStrips().iterator();
    for (int i = 0; i < n; i++) {
      keys[i] = Double.valueOf(i);
      values[i] = resultFx.getEntry(i);
      final FixedIncomeStripWithSecurity strip = iterator.next();
      labels[i] = strip.getResolvedTenor().getPeriod().toString();
    }
    final DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    return Sets.newHashSet(new ComputedValue(spec, labelledMatrix));
  }

}
