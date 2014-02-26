/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CapFloorCMSSpreadSecurityConverter;
import com.opengamma.financial.analytics.conversion.CapFloorSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * Base class for the calculation of yield curve node sensitivities of instruments priced using the SABR model.
 * 
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public abstract class SABRYCNSFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRYCNSFunction.class);
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String PROPERTY_REQUESTED_CURVE = ValuePropertyNames.OUTPUT_RESERVED_PREFIX + "RequestedCurve";

  private FinancialSecurityVisitor<InstrumentDefinition<?>> _securityVisitor;
  private SecuritySource _securitySource;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    final SwaptionSecurityConverterDeprecated swaptionConverter = new SwaptionSecurityConverterDeprecated(_securitySource, swapConverter);
    final CapFloorSecurityConverterDeprecated capFloorVisitor = new CapFloorSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final CapFloorCMSSpreadSecurityConverter capFloorCMSSpreadSecurityVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource, regionSource);
    _securityVisitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).swaptionVisitor(swaptionConverter)
        .capFloorVisitor(capFloorVisitor).capFloorCMSSpreadVisitor(capFloorCMSSpreadSecurityVisitor).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final PresentValueNodeSensitivityCalculator nodeCalculator = getNodeSensitivityCalculator(desiredValue);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InstrumentDefinition<?> definition = security.accept(_securityVisitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final String conventionName = currency.getCode() + "_SWAP";
    final ConventionBundle convention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention named " + conventionName);
    }
    final DayCount dayCount = convention.getSwapFloatingLegDayCount();
    if (dayCount == null) {
      throw new OpenGammaRuntimeException("Could not get daycount");
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final int numCurveNames = curveNames.length;
    final String[] fullCurveNames = new String[numCurveNames];
    for (int i = 0; i < numCurveNames; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final YieldCurveBundle knownCurves = YieldCurveFunctionUtils.getFixedCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, currency, dayCount, curves, desiredValue);
    final SABRInterestRateDataBundle knownData = knownCurves == null ? null : getModelParameters(target, inputs, currency, dayCount, knownCurves, desiredValue);
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, fullCurveNames, timeSeries); //TODO
    final ValueProperties properties = createValueProperties(target, desiredValue).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivities = CALCULATOR.calculateFromPresentValue(derivative, knownData, data, couponSensitivity, jacobian, nodeCalculator);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(derivative, knownData, data, jacobian, nodeCalculator);
    }
    final String fullCurveName = curveName + "_" + currency.getCode();
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, data, sensitivities, curveSpec, spec);

  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties properties = createValueProperties(ccy).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createValueProperties(FinancialSecurityUtils.getCurrency(target.getSecurity()));
    if (OpenGammaCompilationContext.isPermissive(context)) {
      for (final ValueRequirement input : inputs.values()) {
        final String curve = input.getConstraint(PROPERTY_REQUESTED_CURVE);
        if (curve != null) {
          properties.withoutAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE, curve);
          break;
        }
      }
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> requestedCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    final boolean permissive = OpenGammaCompilationContext.isPermissive(context);
    if (!permissive && ((requestedCurveNames == null) || requestedCurveNames.isEmpty())) {
      s_logger.error("Must ask for a single named curve");
      return null;
    }
    final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
    if (cubeNames == null || cubeNames.size() != 1) {
      return null;
    }
    final Set<String> fittingMethods = constraints.getValues(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    if (fittingMethods == null || fittingMethods.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String[] availableCurveNames = curveCalculationConfig.getYieldCurveNames();
    if ((requestedCurveNames == null) || requestedCurveNames.isEmpty()) {
      requestedCurveNames = Sets.newHashSet(availableCurveNames);
    } else {
      final Set<String> intersection = YieldCurveFunctionUtils.intersection(requestedCurveNames, availableCurveNames);
      if (intersection.isEmpty()) {
        s_logger.error("None of the requested curves {} are available in curve calculation configuration called {}", requestedCurveNames, curveCalculationConfigName);
        return null;
      }
      requestedCurveNames = intersection;
    }
    if (!permissive && (requestedCurveNames.size() != 1)) {
      s_logger.error("Must specify single curve name constraint, got {}", requestedCurveNames);
      return null;
    }
    final String curveName = requestedCurveNames.iterator().next();
    final String cubeName = cubeNames.iterator().next();
    final String fittingMethod = fittingMethods.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Set<ValueRequirement> curveRequirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource);
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(curveRequirements.size() + 3);
    for (final ValueRequirement curveRequirement : curveRequirements) {
      final ValueProperties.Builder properties = curveRequirement.getConstraints().copy();
      properties.with(PROPERTY_REQUESTED_CURVE, curveName).withOptional(PROPERTY_REQUESTED_CURVE);
      requirements.add(new ValueRequirement(curveRequirement.getValueName(), curveRequirement.getTargetReference(), properties.get()));
    }
    requirements.add(getCubeRequirement(cubeName, currency, fittingMethod));
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(currency, curveName));
    }
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(security, security.accept(_securityVisitor));
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    return requirements;
  }

  protected abstract SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency, final DayCount dayCount,
      final YieldCurveBundle curves, final ValueRequirement desiredValue);

  protected ValueRequirement getCubeRequirement(final String cubeName, final Currency currency, final String fittingMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CUBE, cubeName).with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, ComputationTargetSpecification.of(currency), properties);
  }

  protected abstract ValueProperties.Builder createValueProperties(final Currency currency);

  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  protected abstract PresentValueNodeSensitivityCalculator getNodeSensitivityCalculator(final ValueRequirement desiredValue);

  private static ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency), properties);
  }

}
