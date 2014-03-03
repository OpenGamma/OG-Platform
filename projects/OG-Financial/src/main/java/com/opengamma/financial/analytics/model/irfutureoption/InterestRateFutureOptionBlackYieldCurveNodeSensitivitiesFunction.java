/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverterDeprecated;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.black.BlackDiscountingYCNSIRFutureOptionFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use {@link BlackDiscountingYCNSIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackYieldCurveNodeSensitivitiesFunction.class);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivityBlackCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private FixedIncomeConverterDataProvider _dataConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;
  private ConfigDBFXForwardCurveSpecificationSource _fxForwardCurveSpecificationSource;
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
    _fxForwardCurveSpecificationSource = ConfigDBFXForwardCurveSpecificationSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final LocalDate localNow = now.toLocalDate();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) trade.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String fullCurveName = curveName + "_" + currency.getCode();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    // To enable standard and midcurve options to share the same default name
    final String surfaceNameWithPrefix = surfaceName + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceNameWithPrefix, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final YieldCurveBundle fixedCurves = YieldCurveFunctionUtils.getFixedCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final InstrumentDefinition<?> irFutureOptionDefinition = getConverter(executionContext).convert(trade);
    final InstrumentDerivative irFutureOption = _dataConverter.convert(security, irFutureOptionDefinition, now, fullCurveNames, timeSeries);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final YieldCurveWithBlackCubeBundle data = new YieldCurveWithBlackCubeBundle(volatilitySurface.getSurface(), curves);
    final YieldCurveWithBlackCubeBundle fixedData = fixedCurves == null ? null : new YieldCurveWithBlackCubeBundle(volatilitySurface.getSurface(), fixedCurves);
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivitiesObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
      if (couponSensitivitiesObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = new DoubleMatrix1D(FunctionUtils.decodeCouponSensitivities(couponSensitivitiesObject));
      sensitivities = CALCULATOR.calculateFromPresentValue(irFutureOption, fixedData, data, couponSensitivity, jacobian, NSC);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(irFutureOption, fixedData, data, jacobian, NSC);
    }
    if (curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      final Currency domesticCurrency = ComputationTargetType.CURRENCY.resolve(curveCalculationConfig.getTarget().getUniqueId());
      final Currency foreignCurrency = ComputationTargetType.CURRENCY.resolve(_curveCalculationConfigSource
          .getConfig(curveCalculationConfig.getExogenousConfigData().keySet().iterator().next()).getTarget().getUniqueId());
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(sensitivities, domesticCurrency, foreignCurrency, fullCurveNames, curves,
          _fxForwardCurveSpecificationSource, _fxForwardCurveDefinitionSource, localNow, getResultSpec(target, currency.getCode(), curveCalculationConfigName, surfaceName, fullCurveName));
    }
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(fullCurveName, data, sensitivities, curveSpec,
        getResultSpec(target, currency.getCode(), curveCalculationConfigName, surfaceName, curveName));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return Collections.singleton(getResultSpec(target, currency));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Must specify a single curve name; have {}", curveNames);
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
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
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String[] yieldCurveNames = curveCalculationConfig.getYieldCurveNames();
    final String curve = curveNames.iterator().next();
    if (Arrays.binarySearch(yieldCurveNames, curve) < 0) {
      s_logger.info("Curve named {} is not available in curve calculation configuration called {}", curve, curveCalculationConfigName);
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next() + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(currency, curve));
    }
    final Set<ValueRequirement> tsRequirements = _dataConverter.getConversionTimeSeriesRequirements(target.getTrade().getSecurity(), getConverter(context).convert(target.getTrade()));
    if (tsRequirements == null) {
      return null;
    }
    requirements.addAll(tsRequirements);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    String calculationConfig = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (input.getKey().getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        calculationConfig = input.getKey().getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      }
    }
    assert calculationConfig != null;
    return Collections.singleton(getResultSpec(target, ccy.getCode(), calculationConfig));
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String currency) {
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties(currency));
  }

  private ValueProperties getResultProperties(final String currency) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE).with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency).withAny(ValuePropertyNames.CURVE).get();
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String currency, final String curveCalculationConfig) {
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties(currency, curveCalculationConfig));
  }

  private ValueProperties getResultProperties(final String currency, final String curveCalculationConfig) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).withAny(ValuePropertyNames.SURFACE).with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).withAny(ValuePropertyNames.CURVE).get();
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String currency, final String curveCalculationConfig, final String surfaceName, final String curveName) {
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties(currency, curveCalculationConfig, surfaceName,
        curveName));
  }

  private ValueProperties getResultProperties(final String currency, final String curveCalculationConfig, final String surfaceName, final String curveName) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).with(ValuePropertyNames.SURFACE, surfaceName).with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency).with(ValuePropertyNames.CURVE, curveName).get();
  }

  private static ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
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
}
