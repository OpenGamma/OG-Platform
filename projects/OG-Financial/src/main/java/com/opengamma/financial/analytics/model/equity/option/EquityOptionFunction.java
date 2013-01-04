/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.EquityIndexOptionConverter;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public abstract class EquityOptionFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionFunction.class);
  /** The value requirement name */
  private final String[] _valueRequirementNames;
  /** Converts the security to the form used in analytics */
  private EquityIndexOptionConverter _converter; // set in init(), not constructor

  /**
   * @param valueRequirementNames A list of value requirement names, not null or empty
   */
  public EquityOptionFunction(final String... valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _converter = new EquityIndexOptionConverter(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    final InstrumentDefinition<EquityIndexOption> defn = security.accept(_converter);
    final EquityIndexOption derivative = defn.toDerivative(now);
    if (derivative.getTimeToSettlement() < 0.0) {
      throw new OpenGammaRuntimeException("Equity option " + security.toString() + ", has already settled.");
    }

    // 2. Build up the market data bundle
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // 3. Create result properties
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties resultProperties = createValueProperties(target, desiredValue).get();
    // 4. The Calculation - what we came here to do
    return computeValues(derivative, market, inputs, desiredValues, target.toSpecification(), resultProperties);
  }

  /**
   * Constructs a market data bundle
   * @param underlyingId The underlying id of the index option
   * @param executionContext The execution context
   * @param inputs The market data inputs
   * @param target The target
   * @param desiredValues The desired values of the function
   * @return The market data bundle used in pricing
   */
  // buildMarketBundle is re-used by EquityIndexVanillaBarrierOptionFunction, hence is available to call  */
  protected StaticReplicationDataBundle buildMarketBundle(final ExternalId underlyingId, final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    // a. The Spot Index
    final HistoricalTimeSeriesSource tsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Object spotObject = inputs.getValue(getSpotRequirement(underlyingId, tsSource));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    // b. The Funding Curve
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Object discountingObject = inputs.getValue(getDiscountCurveRequirement(fundingCurveName, curveConfigName, security));
    if (discountingObject == null) {
      throw new OpenGammaRuntimeException("Could not get discounting Curve");
    }
    if (!(discountingObject instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve discountingCurve = (YieldCurve) discountingObject;

    // c. The Vol Surface
    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String smileInterpolator = desiredValue.getConstraint(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    final Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(tsSource, security, volSurfaceName,
        smileInterpolator, curveConfigName, fundingCurveName, underlyingId));
    if (volSurfaceObject == null || !(volSurfaceObject instanceof BlackVolatilitySurface)) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final BlackVolatilitySurface<?> blackVolSurf = (BlackVolatilitySurface<?>) volSurfaceObject;

    // d. Forward Curve
    final ForwardCurve forwardCurve = new ForwardCurve(spot, discountingCurve.getCurve()); // TODO: REFACTOR - Better estimates may be taken from Futures. There are numerous cases to consider.

    final StaticReplicationDataBundle market = new StaticReplicationDataBundle(blackVolSurf, discountingCurve, forwardCurve);
    return market;
  }

  /**
   * Calculates the result
   * @param derivative The derivative
   * @param market The market data bundle
   * @param inputs The market data inputs
   * @param desiredValues The desired values
   * @param targetSpec The target specification of the result
   * @param resultProperties The result properties
   * @return The result of the calculation
   */
  protected abstract Set<ComputedValue> computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties);

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties(target).get();
    final Set<ValueSpecification> result = Sets.newHashSetWithExpectedSize(_valueRequirementNames.length);
    for (final String valueRequirementName : _valueRequirementNames) {
      result.add(new ValueSpecification(valueRequirementName, target.toSpecification(), properties));
    }
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {

    final ValueProperties constraints = desiredValue.getConstraints();
    // Get security and its underlying's ExternalId.
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);

    // TODO: REVIEW THIS - TimeSeriesSource, used to get Ticker, the Vol ComputationTarget, from ExternalIdBundle
    // We are now also using the ticker for the Spot / Market_Value Requirement
    final HistoricalTimeSeriesSource tsSource = OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context);

    // 1. Spot Index Requirement
    final ValueRequirement spotReq = getSpotRequirement(underlyingId, tsSource);

    // 2. Funding Curve Requirement
    // Funding curve
    final String fundingCurveName = getFundingCurveName(desiredValue);
    if (fundingCurveName == null) {
      return null;
    }
    // Curve configuration
    final String curveConfigName = getCurveConfigName(desiredValue);
    if (curveConfigName == null) {
      return null;
    }
    final ValueRequirement fundingReq = getDiscountCurveRequirement(fundingCurveName, curveConfigName, security);

    // 3. Volatility Surface Requirement
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

    final ValueRequirement volReq = getVolatilitySurfaceRequirement(tsSource, security, volSurfaceName, smileInterpolator, curveConfigName, fundingCurveName, underlyingId);
    // Return the set
    return Sets.newHashSet(fundingReq, spotReq, volReq);
  }

  /**
   * Creates general result properties
   * @param target The target
   * @return The value properties of the result
   */
  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target);

  /**
   * Creates result properties with the values set
   * @param target The target
   * @param desiredValue The desired value
   * @return The value properties of the result
   */
  protected abstract ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  private String getFundingCurveName(final ValueRequirement desiredValue) {
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.CURVE);
      return null;
    }
    final String fundingCurveName = fundingCurves.iterator().next();
    return fundingCurveName;
  }

  private String getCurveConfigName(final ValueRequirement desiredValue) {
    final Set<String> curveConfigNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveConfigNames == null || curveConfigNames.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      return null;
    }
    final String curveConfigName = curveConfigNames.iterator().next();
    return curveConfigName;
  }

  private ValueRequirement getDiscountCurveRequirement(final String fundingCurveName, final String curveCalculationConfigName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, fundingCurveName)
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
      .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  // TODO: One should not be required to pass the FundingCurve and CurveConfig names, so that the VolatilitySurface can build an EquityForwardCurve
  private ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final Security security,
      final String surfaceName, final String smileInterpolator, final String curveConfig, final String fundingCurveName, final ExternalId underlyingBuid) {
    // Targets for equity vol surfaces are the underlying tickers
    final String bbgTicker = getBloombergTicker(tsSource, underlyingBuid);
    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), bbgTicker); // FIXME: WEAK Tickers mean stale data. Also, this should NOT be hardcoded

    // Set Forward Curve Currency Property
    final String curveCurrency = FinancialSecurityUtils.getCurrency(security).toString();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, smileInterpolator)
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveConfig)
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  /**
   * Get the Ticker from the BUID via the HistoricalTimeSeriesSource.<p>
   * This might seem like a strange way to do it. It is. The reason is that only the tsSource appeared to contain the ExternalIdBundle!
   * @param tsSource The time series source
   * @param underlyingBuid The underlying BUID
   * @return The Bloomberg ticker
   */
  // TODO: Find a more appropriate way.
  // TODO: handle other data sources
  protected String getBloombergTicker(final HistoricalTimeSeriesSource tsSource, final ExternalId underlyingBuid) {
    if (tsSource == null || underlyingBuid == null) {
      throw new OpenGammaRuntimeException("Unable to find option underlyer's ticker from the ExternalIdBundle");
    }
    final HistoricalTimeSeries historicalTimeSeries = tsSource.getHistoricalTimeSeries("PX_LAST", ExternalIdBundle.of(underlyingBuid), null, null, true, null, true, 1);
    if (historicalTimeSeries == null) {
      throw new OpenGammaRuntimeException("We require a time series for " + underlyingBuid);
    }
    final ExternalIdBundle idBundle = tsSource.getExternalIdBundle(historicalTimeSeries.getUniqueId());
    final String bbgTicker = (idBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER)).getValue();
    return bbgTicker;
  }

  private ValueRequirement getSpotRequirement(final ExternalId underlyingId, final HistoricalTimeSeriesSource tsSource) {
    final String bbgTicker = getBloombergTicker(tsSource, underlyingId);
//    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), bbgTicker);  // FIXME: Using WEAK Ticker gives stale data,
    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), bbgTicker); //FIXME: NOT using WEAK Ticker means the spot may be out of line with VolSurface
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, newId);
    // return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of(underlyingId.getScheme().getName(), underlyingId.getValue()));

  }

  /**
   * Gets the value requirement names
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

}
