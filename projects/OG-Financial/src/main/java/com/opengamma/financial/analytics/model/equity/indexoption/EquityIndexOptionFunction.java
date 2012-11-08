/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.EquityIndexOptionConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public abstract class EquityIndexOptionFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityIndexOptionFunction.class);
  private final String _valueRequirementName;
  private EquityIndexOptionConverter _converter; // set in init(), not constructor

  // Get rid of this, refactor EquityIndexOptionFundingCurveSensitivitiesFunction
  protected final EquityIndexOptionConverter getConverter() {
    return _converter;
  }

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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final EquityIndexOptionSecurity security = getEquityIndexOptionSecurity(target);
    final ExternalId underlyingId = security.getUnderlyingId();
    final EquityIndexOptionDefinition defn = _converter.visitEquityIndexOptionSecurity(security);
    final EquityIndexOption derivative = (EquityIndexOption) defn.toDerivative(now);
    if (derivative.getTimeToSettlement() < 0.0) {
      throw new OpenGammaRuntimeException("EquityIndexOption with expiry, " + security.getExpiry().getExpiry().toString() + ", has already settled.");
    }

    // 2. Build up the market data bundle
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // 3. The Calculation - what we came here to do
    final Object results = computeValues(derivative, market);

    // 4. Create Result's Specification that matches the properties promised and Return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    //final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties(target, desiredValue, executionContext).get());
    return Collections.singleton(new ComputedValue(spec, results));
  }

  // This is re-used by EquityIndexVanillaBarrierOptionFunction, hence is available to call  */
  protected StaticReplicationDataBundle buildMarketBundle(final ExternalId underlyingId, final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    // a. The Spot Index
    final Object spotObject = inputs.getValue(getSpotRequirement(underlyingId));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    // b. The Funding Curve
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final Object fundingObject = inputs.getValue(getDiscountCurveRequirement(security, fundingCurveName));
    if (fundingObject == null) {
      throw new OpenGammaRuntimeException("Could not get Funding Curve");
    }
    if (!(fundingObject instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve fundingCurve = (YieldCurve) fundingObject;

    // c. The Vol Surface
    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String smileInterpolator = desiredValue.getConstraint(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    final Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext), security, volSurfaceName,
        smileInterpolator, fundingCurveName, underlyingId));
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

  protected abstract Object computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getSecurity() instanceof EquityIndexOptionSecurity;
  }

  protected EquityIndexOptionSecurity getEquityIndexOptionSecurity(final ComputationTarget target) {
    return (EquityIndexOptionSecurity) target.getSecurity();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties(target).get()));
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunction.BLACK_METHOD)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, ValueRequirement desiredValue, FunctionExecutionContext executionContext) {
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String smileInterpolatorName = desiredValue.getConstraint(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    ValueProperties.Builder builder = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunction.BLACK_METHOD)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, volSurfaceName)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, smileInterpolatorName)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    return builder;
  }

  @Override
  /**
   * Get the set of ValueRequirements
   * If null, engine will attempt to find a default, and call function again
   */
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {

    final ValueProperties constraints = desiredValue.getConstraints();
    // Get security and its underlying's ExternalId.
    final Security security = target.getSecurity();
    final ExternalId underlyingId;
    if (security instanceof EquityIndexOptionSecurity) {
      underlyingId = ((EquityIndexOptionSecurity) security).getUnderlyingId();
    } else if (security instanceof EquityBarrierOptionSecurity) {
      underlyingId = ((EquityBarrierOptionSecurity) security).getUnderlyingId();
    } else {
      throw new OpenGammaRuntimeException("EquityIndexOptionFunction does not handle this security type: " + security.getSecurityType());
    }

    // Spot Index Requirement
    final ValueRequirement spotReq = getSpotRequirement(underlyingId);
    // Funding Curve Requirement
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      return null;
    }
    final String fundingCurveName = fundingCurves.iterator().next();
    final ValueRequirement fundingReq = getDiscountCurveRequirement(security, fundingCurveName);
    // Volatility Surface Requirement
    // Surface Name
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SURFACE);
      return null;
    }
    final String volSurfaceName = surfaceNames.iterator().next();
    // Interpolator Name
    final Set<String> interpolators = constraints.getValues(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    if (interpolators == null || interpolators.size() != 1) {
      return null;
    }
    final String smileInterpolator = interpolators.iterator().next();
    // TimeSeriesSource, used to get Ticker, the Vol ComputationTarget, from ExternalIdBundle
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context), security, volSurfaceName, smileInterpolator, fundingCurveName,
        underlyingId);

    // Return the set
    return Sets.newHashSet(volReq, fundingReq, spotReq);
  }

  protected ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final Security security,
      final String surfaceName, final String smileInterpolator, final String fundingCurveName, final ExternalId underlyingBuid) {
    // Targets for equity vol surfaces are the underlying tickers
    String bbgTicker = getBloombergTicker(tsSource, underlyingBuid);
    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), bbgTicker);

    // Set Forward Curve Currency Property
    final String curveCurrency = FinancialSecurityUtils.getCurrency(security).toString();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, smileInterpolator)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  /*
   * Get the Ticker from the BUID via the HistoricalTimeSeriesSource.<p>
   * This might seem like a strange way to do it. It is. The reason is that only the tsSource appeared to contain the ExternalIdBundle!
   * TODO: Find a more appropriate way.
   */
  protected String getBloombergTicker(final HistoricalTimeSeriesSource tsSource, final ExternalId underlyingBuid) {
    if (tsSource == null || underlyingBuid == null) {
      throw new OpenGammaRuntimeException("Unable to find option underlyer's ticker from the ExternalIdBundle");
    } else {
      final HistoricalTimeSeries historicalTimeSeries = tsSource.getHistoricalTimeSeries("PX_LAST", ExternalIdBundle.of(underlyingBuid), null, null, true, null, true, 1);
      if (historicalTimeSeries == null) {
        throw new OpenGammaRuntimeException("We require a time series for " + underlyingBuid);
      }
      final ExternalIdBundle idBundle = tsSource.getExternalIdBundle(historicalTimeSeries.getUniqueId());
      String bbgTicker = (idBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER)).getValue();
      return bbgTicker;
    }
  }

  protected ValueRequirement getSpotRequirement(final ExternalId underlyingId) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of(underlyingId.getScheme().getName(), underlyingId.getValue()));
  }

  protected ValueRequirement getDiscountCurveRequirement(final Security security, final String fundingCurveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, FinancialSecurityUtils.getCurrency(security).getUniqueId(), properties);
  }

  protected final String getValueRequirementName() {
    return _valueRequirementName;
  }

}
