/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
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
import com.opengamma.financial.analytics.equity.EquityIndexOptionConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.future.FuturePriceCurveFunction;
import com.opengamma.financial.analytics.model.forex.option.blackold.ForexOptionBlackFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class EquityIndexOptionFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _valueRequirementName;
  private EquityIndexOptionConverter _converter; // set in init(), not constructor

  public EquityIndexOptionFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _converter = new EquityIndexOptionConverter(holidaySource, conventionSource, regionSource);
  }
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityIndexOptionSecurity security = (EquityIndexOptionSecurity) target.getSecurity();
    final EquityIndexOptionDefinition defn = _converter.visitEquityIndexOptionSecurity(security);
    final EquityIndexOption option = (EquityIndexOption) defn.toDerivative(now); // TODO Review why a cast is needed?

    // 2. Build up the market data bundle
    // a. The Vol Surface
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(security, volSurfaceName));
    if (volSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final VolatilitySurface volSurface = (VolatilitySurface) volSurfaceObject;
    //TODO no choice of other surfaces
    final BlackVolatilitySurface<?> blackVolSurf = new BlackVolatilitySurfaceStrike(volSurface.getSurface());

    // b. The Funding Curve
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final Object fundingObject = inputs.getValue(getDiscountCurveRequirement(security, fundingCurveName));
    if (fundingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Funding Curve");
    }
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingObject;

    final double expiry = TimeCalculator.getTimeBetween(executionContext.getValuationClock().zonedDateTime(), security.getExpiry().getExpiry());
    final double discountFactor = fundingCurve.getDiscountFactor(expiry);
    Validate.isTrue(discountFactor != 0, "The discount curve has returned a zero value for a discount bond. Check rates.");

    // c. The Spot Index
    final Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    final ForwardCurve forwardCurve = new ForwardCurve(spot, fundingCurve.getCurve()); //TODO change this
    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(blackVolSurf, fundingCurve, forwardCurve);

    // 3. The Calculation - what we came here to do
    final double pv = 19770209.;

    // 4. Create Result's Specification that matches the properties promised and Return
    ValueProperties resultProps = getValueProperties(fundingCurveName, volSurfaceName);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), resultProps);
    return Collections.singleton(new ComputedValue(spec, pv));

  }

  // protected abstract Set<ComputedValue> computeValues(final ComputationTarget target, final FunctionInputs inputs, final IndexOption derivative, final VarianceSwapDataBundle market);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof EquityIndexOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getValueProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  /**
   * @param target For Equity Index Options, the ComputationTarget is the Index name
   * @return The properties (ValueRequirements) that the Function promises to deliver
   */
  private ValueProperties getValueProperties(ComputationTarget target) {
    return createValueProperties()
      .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
      .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
      .withAny(ValuePropertyNames.SURFACE)
      .get();
  }

  private ValueProperties getValueProperties(String fundingCurveName, String volSurfaceName) {
    return createValueProperties()
      .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
      .with(ValuePropertyNames.SURFACE, volSurfaceName)
      .get();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final EquityIndexOptionSecurity security = (EquityIndexOptionSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();

    // Get Volatility Surface Requirement - if null, engine will attempt to find a default, and call function again
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SURFACE);
      return null;
    }
    final String volSurfaceName = surfaceNames.iterator().next();
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(security, volSurfaceName);


    return Sets.newHashSet(volReq); // getDiscountCurveRequirement(security), getSpotRequirement(security),
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final EquityIndexOptionSecurity security, final String surfaceName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, "EQUITY_OPTION")
        .get();
    final ExternalId id = security.getUnderlyingId();
    final UniqueId newId = id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER) ? UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), id.getValue()) :
      UniqueId.of(id.getScheme().getName(), id.getValue());
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  private ValueRequirement getSpotRequirement(final EquityIndexOptionSecurity security) {
    final ExternalId id = security.getUnderlyingId();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of(id.getScheme().getName(), id.getValue()));
  }

  // Note that createValueProperties is _not_ used - use will mean the engine can't find the requirement
  private ValueRequirement getDiscountCurveRequirement(final EquityIndexOptionSecurity security, final String fundingCurveName) {
    final ValueProperties properties = ValueProperties.builder().with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }
  private static final Logger s_logger = LoggerFactory.getLogger(FuturePriceCurveFunction.class);
}
