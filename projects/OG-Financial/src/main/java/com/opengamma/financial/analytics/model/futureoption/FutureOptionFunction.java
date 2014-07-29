/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CommodityFutureOptionConverter;
import com.opengamma.financial.analytics.conversion.EquityOptionsConverter;
import com.opengamma.financial.analytics.conversion.FutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Base class for futures option pricing and analytics
 */
public abstract class FutureOptionFunction extends AbstractFunction.NonCompiledInvoker {
  /** The values that the function can calculate */
  private final String[] _valueRequirementNames;
  /** Converts securities into a form that analytics can use */
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _converter;

  /**
   * @param valueRequirementNames The value requirement names, not null or empty
   */
  public FutureOptionFunction(final String[] valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  @Override
  /**
   * {@inheritDoc}
   * Pass all conventions required to function to convert security to definition
   */
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final FutureSecurityConverterDeprecated futureSecurityConverter = new FutureSecurityConverterDeprecated(null);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> commodityFutureOption = new CommodityFutureOptionConverter(securitySource, holidaySource, conventionSource, regionSource);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> equityFutureOption = new EquityOptionsConverter(futureSecurityConverter, securitySource);
    _converter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .commodityFutureOptionSecurityVisitor(commodityFutureOption)
        .equityIndexFutureOptionVisitor(equityFutureOption).create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    final InstrumentDefinition<?> defn = security.accept(_converter);
    final InstrumentDerivative derivative = defn.toDerivative(now);
    final double timeToExpiry = derivative.accept(LastTimeCalculator.getInstance());
    if (timeToExpiry < 0.0) {
      throw new OpenGammaRuntimeException("Future option " + security + " has already settled.");
    }
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties resultProperties = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    return computeValues(derivative, market, inputs, desiredValues, target.toSpecification(), resultProperties);
  }

  /**
   * Constructs a market data bundle for use in the analytics library.
   * @param underlyingId The id of the underlying
   * @param executionContext The execution context
   * @param inputs The market data inputs
   * @param target The computation target
   * @param desiredValues The desired values
   * @return The market data in a form that the analytics library can use
   */
  protected StaticReplicationDataBundle buildMarketBundle(final ExternalId underlyingId, final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // 1. The Discounting Curve
    final Object discountingObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (discountingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Discounting Curve");
    }
    if (!(discountingObject instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve discountingCurve = (YieldCurve) discountingObject;

    // 2. The Vol Surface
    final Object volSurfaceObject = inputs.getValue(ValueRequirementNames.BLACK_VOLATILITY_SURFACE);
    if (volSurfaceObject == null || !(volSurfaceObject instanceof BlackVolatilitySurface)) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final BlackVolatilitySurface<?> blackVolSurf = (BlackVolatilitySurface<?>) volSurfaceObject;

    // 3. Forward Curve
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final StaticReplicationDataBundle market = new StaticReplicationDataBundle(blackVolSurf, discountingCurve, forwardCurve);
    return market;
  }

  /**
   * Calculates the value
   * @param derivative The derivative
   * @param market The market data bundle
   * @param inputs The function inputs
   * @param desiredValues The desired values
   * @param targetSpec The computation target specification
   * @param resultProperties The result properties
   * @return A set of values
   */
  protected abstract Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirementName : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirementName, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod != null && calculationMethod.size() == 1) {
      if (!getCalculationMethod().equals(Iterables.getOnlyElement(calculationMethod))) {
        return null;
      }
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Set<String> discountingCurveNames = constraints.getValues(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveConfigs = constraints.getValues(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG);
    if (discountingCurveConfigs == null || discountingCurveConfigs.size() != 1) {
      return null;
    }
    final String discountingCurveName = Iterables.getOnlyElement(discountingCurveNames);
    final String discountingCurveConfig = Iterables.getOnlyElement(discountingCurveConfigs);
    final ValueRequirement discountingReq = getDiscountCurveRequirement(discountingCurveName, discountingCurveConfig, security);
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String volSurfaceName = Iterables.getOnlyElement(surfaceNames);
    final Set<String> surfaceCalculationMethods = constraints.getValues(ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    if (surfaceCalculationMethods == null || surfaceCalculationMethods.size() != 1) {
      return null;
    }
    final String surfaceCalculationMethod = Iterables.getOnlyElement(surfaceCalculationMethods);
    final Set<String> forwardCurveNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String forwardCurveCalculationMethod = Iterables.getOnlyElement(forwardCurveCalculationMethods);
    final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(desiredValue, security, volSurfaceName, forwardCurveName, surfaceCalculationMethod);
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(security, forwardCurveName, forwardCurveCalculationMethod);
    return Sets.newHashSet(discountingReq, forwardCurveReq, volReq);
  }

  /**
   * Allows us to set which ValueSpecifications contain ValuePropertyNames.CURRENCY <p>
   * PresentValue and ValueGamma will, while Delta and PositionDelta will not.
   * @return true if Function's specification contains ValuePropertyNames.CURRENCY, else false.
   */
  protected boolean getFunctionIncludesCurrencyProperty() {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    boolean discountCurvePropertiesSet = false;
    boolean forwardCurvePropertiesSet = false;
    boolean surfacePropertiesSet = false;
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethod())
        .with(CalculationPropertyNamesAndValues.PROPERTY_MODEL_TYPE, getModelType());
    if (getFunctionIncludesCurrencyProperty()) {
      properties.with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    }
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification value = entry.getKey();
      final String inputName = value.getValueName();
      if (inputName.equals(ValueRequirementNames.YIELD_CURVE) && !discountCurvePropertiesSet) {
        final ValueProperties curveProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(ValuePropertyNames.CURVE)
            .withoutAny(ValuePropertyNames.CURRENCY)
            .withoutAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
            .withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
            .get();
        final String discountingCurveName = value.getProperty(ValuePropertyNames.CURVE);
        final String discountingCurveConfig = value.getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        for (final String property : curveProperties.getProperties()) {
          properties.with(property, curveProperties.getValues(property));
        }
        properties
          .with(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME, discountingCurveName)
          .with(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG, discountingCurveConfig);
        discountCurvePropertiesSet = true;
      } else if (inputName.equals(ValueRequirementNames.BLACK_VOLATILITY_SURFACE) && !surfacePropertiesSet) {
        final ValueProperties surfaceProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE)
            .withoutAny(ValuePropertyNames.SURFACE)
            .get();
        final String surfaceName = value.getProperty(ValuePropertyNames.SURFACE);
        for (final String property : surfaceProperties.getProperties()) {
          properties.with(property, surfaceProperties.getValues(property));
        }
        properties.with(ValuePropertyNames.SURFACE, getSurfaceName(security, surfaceName));
        surfacePropertiesSet = true;
      } else if (inputName.equals(ValueRequirementNames.FORWARD_CURVE) && !forwardCurvePropertiesSet) {
        final ValueProperties forwardCurveProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(ValuePropertyNames.CURVE)
            .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
            .get();
        final String forwardCurveName = value.getProperty(ValuePropertyNames.CURVE);
        for (final String property : forwardCurveProperties.getProperties()) {
          properties.with(property, forwardCurveProperties.getValues(property));
        }
        properties.with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME, getSurfaceName(security, forwardCurveName));
        forwardCurvePropertiesSet = true;
      }
    }
    assert discountCurvePropertiesSet;
    assert forwardCurvePropertiesSet;
    assert surfacePropertiesSet;
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties.get()));
    }
    return results;
  }

  /**
   * Constructs the discounting curve requirement
   * @param discountingCurveName The discounting curve name
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param security The security
   * @return The discounting curve requirement
   */
  protected ValueRequirement getDiscountCurveRequirement(final String discountingCurveName, final String curveCalculationConfigName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, discountingCurveName)
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
      .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  /**
   * Constructs the volatility surface requirement
   * @param desiredValue The desired value
   * @param security The security
   * @param surfaceName The surface name
   * @param forwardCurveName The forward curve name
   * @param surfaceCalculationMethod The surface calculation method
   * @return The volatility surface requirement
   */
  protected abstract ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final FinancialSecurity security, final String surfaceName,
      final String forwardCurveName, final String surfaceCalculationMethod);

  protected abstract ValueRequirement getForwardCurveRequirement(FinancialSecurity security, String forwardCurveName, String forwardCurveCalculationMethod);

  protected abstract String getCalculationMethod();

  protected abstract String getModelType();

  protected abstract String getSurfaceName(FinancialSecurity security, String surfaceName);

  /**
   * Constructs the underlying future price requirement
   * @param underlyingId The id of the underlying future
   * @return The underlying future price requirement
   */
  protected ValueRequirement getUnderlyingFutureRequirement(final ExternalId underlyingId) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, underlyingId);
  }

  /**
   * Gets the value requirement names
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

  /**
   * Gets the security converter
   * @return The security converter
   */
  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getSecurityConverter() {
    return _converter;
  }

  /**
   * Copies the constraints, removing the function property and replacing it with the appropriate one for the function
   * @param constraints The constraints
   * @return The properties
   */
  protected ValueProperties createResultProperties(final ValueProperties constraints) {
    return constraints.copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

}
