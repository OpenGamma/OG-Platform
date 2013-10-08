/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.black.BlackDiscountingYCNSSwaptionFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * Calculates yield curve node sensitivities for swaptions using the Black method.
 *
 * @deprecated Use {@link BlackDiscountingYCNSSwaptionFunction}
 */
@Deprecated
public class SwaptionBlackYieldCurveNodeSensitivitiesFunction extends SwaptionBlackCurveSpecificFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBlackYieldCurveNodeSensitivitiesFunction.class);
  /** The node sensitivities calculator */
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivityBlackCalculator.getInstance());
  /** The instrument sensitivity calculator */
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   */
  public SwaptionBlackYieldCurveNodeSensitivitiesFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final SwaptionSecurity security = (SwaptionSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String fullCurveName = curveName + "_" + currency.getCode();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceName, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    if (curveNames.length == 1) {
      curveNames = new String[] {curveNames[0], curveNames[0] };
    }
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    if (curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      throw new UnsupportedOperationException("Cannot handle FX implied curves");
    }
    final InstrumentDefinition<?> definition = security.accept(getSecurityConverter());
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDerivative swaption = getDefinitionConverter().convert(security, definition, now, fullCurveNames, timeSeries);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final ValueProperties properties = getResultProperties(currency.getCode(), curveCalculationConfigName, surfaceName, curveName);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(volatilitySurface.getSurface(),
        SwaptionUtils.getSwapGenerator(security, definition, securitySource));
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final YieldCurveBundle knownCurves = YieldCurveFunctionUtils.getFixedCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final YieldCurveWithBlackSwaptionBundle data = new YieldCurveWithBlackSwaptionBundle(parameters, curves);
    final YieldCurveWithBlackSwaptionBundle knownData = knownCurves == null ? null : new YieldCurveWithBlackSwaptionBundle(parameters, knownCurves);
    if (curveCalculationMethod.equals(InterpolatedDataProperties.CALCULATION_METHOD_NAME)) {
      final DoubleMatrix1D sensitivities = CALCULATOR.calculateFromSimpleInterpolatedCurve(swaption, data, NSC);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, data, sensitivities, curveSpec, spec);
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivities = CALCULATOR.calculateFromPresentValue(swaption, knownData, data, couponSensitivity, jacobian, NSC);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(swaption, knownData, data, jacobian, NSC);
    }
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, data, sensitivities, curveSpec, spec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String[] configCurveNames = curveCalculationConfig.getYieldCurveNames();
    if (Arrays.binarySearch(configCurveNames, curveName) < 0) {
      s_logger.error("Curve named {} is not available in curve calculation configuration called {}", curveName, curveCalculationConfigName);
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource));
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(currency, curveName));
    }
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    try {
      final Set<ValueRequirement> timeSeriesRequirements = getDefinitionConverter().getConversionTimeSeriesRequirements(security, security.accept(getSecurityConverter()));
      if (timeSeriesRequirements == null) {
        return null;
      }
      requirements.addAll(timeSeriesRequirements);
      return requirements;
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
  }

  /**
   * Gets the Jacobian requirement.
   * @param currency The currency
   * @param curveCalculationConfigName The curve calculation configuration name.
   * @param curveCalculationMethod The curve calculation method.
   * @return The Jacobian requirement
   */
  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency), properties);
  }

  /**
   * Gets the coupon sensitivities requirement.
   * @param currency The currency
   * @param curveCalculationConfigName The curve calculation configuration name.
   * @return The coupon sensitivities requirement
   */
  private static ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency), properties);
  }

  /**
   * Gets the curve specification requirement.
   * @param currency The currency
   * @param curveName The curve name
   * @return The curve specification requirement
   */
  private static ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final String curveName, final ValueSpecification spec,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target) {
    throw new UnsupportedOperationException();
  }
}
