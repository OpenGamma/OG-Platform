/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.black.ConstantBlackDiscountingSwaptionFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for functions that return curve-specific values for swaptions using the basic Black model (i.e. using a security-specific volatility and not interpolating volatilities).
 * 
 * @deprecated Use descendants of {@link ConstantBlackDiscountingSwaptionFunction}
 */
@Deprecated
public abstract class SwaptionBasicBlackCurveSpecificFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBasicBlackCurveSpecificFunction.class);
  /** The value requirement that can be produced */
  private final String _valueRequirementName;
  /** Converter from {@link SwaptionSecurity} to an analytics object */
  private SwaptionSecurityConverterDeprecated _visitor;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirementName The value requirement name, not null
   */
  public SwaptionBasicBlackCurveSpecificFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    _visitor = new SwaptionSecurityConverterDeprecated(securitySource, swapConverter);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
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
    final Object volatilityObject = inputs.getValue(MarketDataRequirementNames.IMPLIED_VOLATILITY);
    if (volatilityObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility");
    }
    final double volatility = (Double) volatilityObject;
    final VolatilitySurface volatilitySurface = new VolatilitySurface(ConstantDoublesSurface.from(volatility));
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (curveNames.length == 1) {
      curveNames = new String[] {curveNames[0], curveNames[0] };
    }
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final InstrumentDerivative swaption = definition.toDerivative(now, fullCurveNames); //TODO
    final ValueProperties properties = getResultProperties(currency.getCode(), curveCalculationConfigName, curveName);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(volatilitySurface.getSurface(), SwaptionUtils.getSwapGenerator(security, definition, securitySource));
    final YieldCurveWithBlackSwaptionBundle data = new YieldCurveWithBlackSwaptionBundle(parameters, curves);
    return getResult(swaption, data, curveName, spec, curveCalculationConfigName, curveCalculationMethod, inputs, target);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), getResultProperties(currency)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Did not specify a curve name for requirement {}", desiredValue);
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curveNames);
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
    }
    final String[] configCurveNames = curveCalculationConfig.getYieldCurveNames();
    if (Arrays.binarySearch(configCurveNames, curveName) < 0) {
      s_logger.error("Curve named {} is not available in curve calculation configuration called {}", curveName, curveCalculationConfigName);
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(ComputationTargetSpecification.of(target.getSecurity())));
    return requirements;
  }

  /**
   * Calculates the results.
   * 
   * @param swaption The swaption
   * @param data The market data bundle
   * @param curveName The name of the curve
   * @param spec The result specification
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param curveCalculationMethod The curve calculation method name
   * @param inputs The function inputs
   * @param target The target
   * @return A set of results
   */
  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final String curveName, final ValueSpecification spec,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target);

  /**
   * Returns the converter.
   * 
   * @return The converter
   */
  protected SwaptionSecurityConverterDeprecated getVisitor() {
    return _visitor;
  }

  protected ConfigDBCurveCalculationConfigSource getCurveCalculationConfigSource() {
    return _curveCalculationConfigSource;
  }

  /**
   * Gets general result properties.
   * 
   * @param currency The currency
   * @return The result properties
   */
  protected ValueProperties getResultProperties(final String currency) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_BASIC_METHOD).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency).withAny(ValuePropertyNames.CURVE).get();
  }

  /**
   * Gets specfic result properties.
   * 
   * @param currency The currency
   * @param curveCalculationConfig The curve calculation configuration name
   * @param curveName The curve name
   * @return The result properties
   */
  protected ValueProperties getResultProperties(final String currency, final String curveCalculationConfig, final String curveName) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_BASIC_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName).get();
  }

  private static ValueRequirement getVolatilityRequirement(final ComputationTargetSpecification target) {
    return new ValueRequirement(MarketDataRequirementNames.IMPLIED_VOLATILITY, target, ValueProperties.builder().get());
  }
}
