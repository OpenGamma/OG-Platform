/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
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
    final FinancialSecurityVisitor<InstrumentDefinition<?>> commodityFutureOption = new CommodityFutureOptionConverter(securitySource, holidaySource, conventionSource, regionSource);
    _converter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .commodityFutureOptionSecurityVisitor(commodityFutureOption).create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    final InstrumentDefinition<?> defn = security.accept(_converter);
    final InstrumentDerivative derivative = defn.toDerivative(now);
    final double timeToExpiry = derivative.accept(LastTimeCalculator.getInstance());
    if (timeToExpiry < 0.0) {
      throw new OpenGammaRuntimeException("Future option " + security + " has already settled.");
    }
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);
    return computeValues(derivative, market, desiredValues, target);
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

    // a. The Spot Index
    final Object spotObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    // b. The Funding Curve
    final Object fundingObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (fundingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Funding Curve");
    }
    if (!(fundingObject instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve fundingCurve = (YieldCurve) fundingObject;

    // c. The Vol Surface
    final Object volSurfaceObject = inputs.getValue(ValueRequirementNames.BLACK_VOLATILITY_SURFACE);
    if (volSurfaceObject == null || !(volSurfaceObject instanceof BlackVolatilitySurface)) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final BlackVolatilitySurface<?> blackVolSurf = (BlackVolatilitySurface<?>) volSurfaceObject;

    // d. Forward Curve
    final ForwardCurve forwardCurve;
    if (blackVolSurf instanceof BlackVolatilitySurfaceMoneyness) { // Use forwards tied to vols if available
      forwardCurve = ((BlackVolatilitySurfaceMoneyness) blackVolSurf).getForwardCurve();
    } else {
      forwardCurve = new ForwardCurve(spot, fundingCurve.getCurve()); // else build from spot and funding curve
    }
    final StaticReplicationDataBundle market = new StaticReplicationDataBundle(blackVolSurf, fundingCurve, forwardCurve);
    return market;
  }

  /**
   * Calculates the value
   * @param derivative The derivative
   * @param market The market data bundle
   * @param desiredValue The desired value
   * @param target The computation target
   * @return A set of values
   */
  protected abstract Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final Set<ValueRequirement> desiredValue,
      final ComputationTarget target);

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.COMMODITY_FUTURE_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String valueRequirementName : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirementName, target.toSpecification(), createValueProperties(target).get()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {

    final ValueProperties constraints = desiredValue.getConstraints();
    final CommodityFutureOptionSecurity security = (CommodityFutureOptionSecurity) target.getSecurity();
    final ExternalId underlyingId = security.getUnderlyingId();
    final ValueRequirement underlyingFutureReq = getUnderlyingFutureRequirement(underlyingId);
    final String fundingCurveName = getFundingCurveName(desiredValue);
    if (fundingCurveName == null) {
      return null;
    }
    final String curveConfigName = getCurveConfigName(desiredValue);
    if (curveConfigName == null) {
      return null;
    }
    final ValueRequirement fundingReq = getDiscountCurveRequirement(fundingCurveName, curveConfigName, security);
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String volSurfaceName = surfaceNames.iterator().next();
    final Set<String> interpolators = constraints.getValues(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    if (interpolators == null || interpolators.size() != 1) {
      return null;
    }
    final String smileInterpolator = interpolators.iterator().next();
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(security, volSurfaceName, smileInterpolator);
    return Sets.newHashSet(underlyingFutureReq, fundingReq, volReq);
  }

  /**
   * Creates general value properties
   * @param target The computation target
   * @return The value properties
   */
  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target);

  /**
   * Creates value properties with the values set
   * @param target The computation target
   * @param desiredValue The desired value of the calculation
   * @return The value properties
   */
  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  /**
   * Gets the funding curve name from the constraints on the desired value
   * @param desiredValue The desired value
   * @return The funding curve name
   */
  protected String getFundingCurveName(final ValueRequirement desiredValue) {
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final String fundingCurveName = Iterables.getOnlyElement(fundingCurves);
    return fundingCurveName;
  }

  /**
   * Gets the curve calculation configuration name from the constraints on the desired value
   * @param desiredValue The desired value
   * @return The curve calculation configuration name
   */
  protected String getCurveConfigName(final ValueRequirement desiredValue) {
    final Set<String> curveCalculationConfigNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);
    return curveConfigName;
  }

  /**
   * Constructs the funding curve requirement
   * @param fundingCurveName The funding curve name
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param security The security
   * @return The funding curve requirement
   */
  protected ValueRequirement getDiscountCurveRequirement(final String fundingCurveName, final String curveCalculationConfigName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, fundingCurveName)
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
      .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  /**
   * Constructs the volatility surface requirement
   * @param security The security
   * @param surfaceName The surface name
   * @param smileInterpolator The interpolation method used
   * @return The volatility surface requirement
   */
  protected abstract ValueRequirement getVolatilitySurfaceRequirement(FinancialSecurity security, final String surfaceName, final String smileInterpolator);

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
