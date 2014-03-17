/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.EquityOptionsConverter;
import com.opengamma.financial.analytics.conversion.FutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Abstract base class for Equity and Equity Index Options
 */
public abstract class EquityOptionFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionFunction.class);
  //TODO the next three properties should be moved from this class after checking that there's no others that match
  /** Property name for the discounting curve */
  public static final String PROPERTY_DISCOUNTING_CURVE_NAME = "DiscountingCurveName";
  /** Property name for the discounting curve configuration */
  public static final String PROPERTY_DISCOUNTING_CURVE_CONFIG = "DiscountingCurveConfig";
  /** The value requirement name */
  private final String[] _valueRequirementNames;
  /** Converts the security to the form used in analytics */
  private EquityOptionsConverter _converter; // set in init(), not constructor
  /** The type this function operates on */
  private static final ComputationTargetType TYPE = FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY);

  /**
   * @param valueRequirementNames A list of value requirement names, not null or empty
   */
  public EquityOptionFunction(final String... valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    final FutureSecurityConverterDeprecated futureSecurityConverter = new FutureSecurityConverterDeprecated(irFutureConverter, bondFutureConverter);
    _converter = new EquityOptionsConverter(futureSecurityConverter, securitySource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Build the analytic derivative to be priced
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    final InstrumentDefinition<?> defn = security.accept(_converter);
    final InstrumentDerivative derivative = defn.toDerivative(now);
    if (derivative.accept(LastTimeCalculator.getInstance()) < 0.0) {
      throw new OpenGammaRuntimeException("Equity option has already settled; " + security.toString());
    }

    // 2. Build up the market data bundle
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // 3. Create result properties
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties resultProperties = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    // 4. The Calculation - what we came here to do
    return computeValues(derivative, market, inputs, desiredValues, target.toSpecification(), resultProperties);
  }

  /**
   * Constructs a market data bundle
   *
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

    // 1. The Funding Curve
    final Object discountingObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (discountingObject == null) {
      throw new OpenGammaRuntimeException("Could not get discounting curve");
    }
    if (!(discountingObject instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve discountingCurve = (YieldCurve) discountingObject;

    // 2. The Vol Surface
    final Object volSurfaceObject = inputs.getValue(ValueRequirementNames.BLACK_VOLATILITY_SURFACE);
    if (volSurfaceObject == null || !(volSurfaceObject instanceof BlackVolatilitySurface)) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
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
   * Calculates the result
   *
   * @param derivative The derivative
   * @param market The market data bundle
   * @param inputs The market data inputs
   * @param desiredValues The desired values
   * @param targetSpec The target specification of the result
   * @param resultProperties The result properties
   * @return The result of the calculation
   */
  protected abstract Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties);

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> result = new HashSet<>();
    for (final String valueRequirementName : _valueRequirementNames) {
      result.add(new ValueSpecification(valueRequirementName, target.toSpecification(), properties));
    }
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    String discountingCurveName = null;
    String discountingCurveConfig = null;
    String surfaceName = null;
    String surfaceCalculationMethod = null;
    String surfaceSmileInterpolator = null;
    String forwardCurveName = null;
    String forwardCurveCalculationMethod = null;
    int check = 8; // the number of properties we're looking out for below
    ValueProperties.Builder additionalConstraintsBuilder = null;
    if ((constraints.getProperties() == null) || (constraints.getProperties().size() < check)) {
      return null;
    }
    for (final String property : constraints.getProperties()) {
      switch (property) {
        case ValuePropertyNames.CALCULATION_METHOD:
          if (constraints.getStrictValue(property) == null) {
            return null;
          }
          check--;
          break;
        case PROPERTY_DISCOUNTING_CURVE_NAME:
          discountingCurveName = constraints.getStrictValue(property);
          if (discountingCurveName == null) {
            return null;
          }
          check--;
          break;
        case PROPERTY_DISCOUNTING_CURVE_CONFIG:
          discountingCurveConfig = constraints.getStrictValue(property);
          if (discountingCurveConfig == null) {
            return null;
          }
          check--;
          break;
        case ValuePropertyNames.SURFACE:
          surfaceName = constraints.getStrictValue(property);
          if (surfaceName == null) {
            return null;
          }
          check--;
          break;
        case ValuePropertyNames.SURFACE_CALCULATION_METHOD:
          surfaceCalculationMethod = constraints.getStrictValue(property);
          if (surfaceCalculationMethod == null) {
            return null;
          }
          check--;
          break;
        case BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR:
          surfaceSmileInterpolator = constraints.getStrictValue(property);
          if (surfaceSmileInterpolator == null) {
            return null;
          }
          check--;
          break;
        case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME:
          forwardCurveName = constraints.getStrictValue(property);
          if (forwardCurveName == null) {
            return null;
          }
          check--;
          break;
        case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
          forwardCurveCalculationMethod = constraints.getStrictValue(property);
          if (forwardCurveCalculationMethod == null) {
            return null;
          }
          check--;
          break;
        default:
          if (additionalConstraintsBuilder == null) {
            additionalConstraintsBuilder = ValueProperties.builder();
          }
          final Set<String> values = constraints.getValues(property);
          if (values.isEmpty()) {
            additionalConstraintsBuilder.withAny(property);
          } else {
            additionalConstraintsBuilder.with(property, values);
          }
          break;
      }
    }
    if (check > 0) {
      // One or more of the properties we were looking for was unconstrained
      return null;
    }
    final ValueProperties additionalConstraints = (additionalConstraintsBuilder != null) ? additionalConstraintsBuilder.get() : ValueProperties.none();

    // Get security and its underlying's ExternalId.
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final HistoricalTimeSeriesSource tsSource = OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context); // TODO: Do we still require tsSource? Was used to access id bundles
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ExternalId underlyingId = getWeakUnderlyingId(FinancialSecurityUtils.getUnderlyingId(security), tsSource, securitySource, context.getComputationTargetResolver().getVersionCorrection(),
        surfaceName);
    if (underlyingId == null) {
      return null;
    }
    // Discounting curve
    final ValueRequirement discountingReq = getDiscountCurveRequirement(discountingCurveName, discountingCurveConfig, security, additionalConstraints);
    // Forward curve
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(forwardCurveName, forwardCurveCalculationMethod, underlyingId, additionalConstraints);
    if (forwardCurveReq == null) {
      return null;
    }
    // Volatility Surface
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(tsSource, securitySource, desiredValue, security, surfaceName, forwardCurveName,
        surfaceCalculationMethod, underlyingId, additionalConstraints); // FIXME: Change signature: Remove desireValue - Add surfaceSmileInterpolator
    if (volReq == null) {
      return null;
    }
    // Return the set
    return Sets.newHashSet(discountingReq, volReq, forwardCurveReq);
  }

  /**
   * Adjusts the properties of the function inputs: <p>
   * <ul>
   *   <li> {@link ValueRequirementNames#YIELD_CURVE} - removes the {@link ValuePropertyNames#FUNCTION} and {@link ValuePropertyNames#CURRENCY}
   *   properties and remaps {@link ValuePropertyNames#CURVE} -> {@link EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_NAME} and
   *   {@link ValuePropertyNames#CURVE_CALCULATION_CONFIG} -> {@link EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_CONFIG}.
   *   <li> {@link ValueRequirementNames#BLACK_VOLATILITY_SURFACE} removes the {@link ValuePropertyNames#FUNCTION} and
   *   {@link InstrumentTypeProperties#PROPERTY_SURFACE_INSTRUMENT_TYPE} properties.
   *   <li> {@link ValueRequirementNames#FORWARD_CURVE} - removes the {@link ValuePropertyNames#FUNCTION} and {@link ValuePropertyNames#CURRENCY}
   *   properties and remaps {@link ValuePropertyNames#CURVE} -> {@link ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_NAME}.
   * </ul>
   * <p>
   * @param input The resolved input
   * @param properties The properties to be adjusted
   */
  protected void extractInputProperties(final ValueSpecification input, final ValueProperties.Builder properties) {
    final String inputName = input.getValueName();
    if (ValueRequirementNames.YIELD_CURVE.equals(inputName)) {
      final ValueProperties curveProperties = input.getProperties().copy()
          .withoutAny(ValuePropertyNames.FUNCTION)
          .withoutAny(ValuePropertyNames.CURVE)
          .withoutAny(ValuePropertyNames.CURRENCY)
          .get();
      properties
          .with(PROPERTY_DISCOUNTING_CURVE_NAME, input.getProperty(ValuePropertyNames.CURVE))
          .with(PROPERTY_DISCOUNTING_CURVE_CONFIG, input.getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
      for (final String property : curveProperties.getProperties()) {
        properties.with(property, curveProperties.getValues(property));
      }
      return;
    }
    if (inputName.equals(ValueRequirementNames.BLACK_VOLATILITY_SURFACE)) {
      final ValueProperties surfaceProperties = input.getProperties().copy()
          .withoutAny(ValuePropertyNames.FUNCTION)
          .withoutAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE)
          .get();
      for (final String property : surfaceProperties.getProperties()) {
        properties.with(property, surfaceProperties.getValues(property));
      }
      return;
    }
    if (inputName.equals(ValueRequirementNames.FORWARD_CURVE)) {
      final ValueProperties forwardCurveProperties = input.getProperties().copy()
          .withoutAny(ValuePropertyNames.FUNCTION)
          .withoutAny(ValuePropertyNames.CURVE)
          .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
          .get();
      properties.with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME, input.getProperty(ValuePropertyNames.CURVE));
      for (final String property : forwardCurveProperties.getProperties()) {
        properties.with(property, forwardCurveProperties.getValues(property));
      }
      return;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethod())
        .with(CalculationPropertyNamesAndValues.PROPERTY_MODEL_TYPE, getModelType())
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      extractInputProperties(entry.getKey(), properties);
    }
    final ValueProperties propertiesImpl = properties.get();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), propertiesImpl));
    }
    return results;
  }

  /**
   * Converts result properties with a currency property to one without.
   *
   * @param resultsWithCurrency The set of results with the currency property set
   * @return A set of results without a currency property
   */
  protected Set<ValueSpecification> getResultsWithoutCurrency(final Set<ValueSpecification> resultsWithCurrency) {
    final Set<ValueSpecification> resultsWithoutCurrency = Sets.newHashSetWithExpectedSize(resultsWithCurrency.size());
    for (final ValueSpecification spec : resultsWithCurrency) {
      final String name = spec.getValueName();
      final ComputationTargetSpecification targetSpec = spec.getTargetSpecification();
      final ValueProperties properties = spec.getProperties().copy()
          .withoutAny(ValuePropertyNames.CURRENCY)
          .get();
      resultsWithoutCurrency.add(new ValueSpecification(name, targetSpec, properties));
    }
    return resultsWithoutCurrency;
  }

  /**
   * Gets the discounting curve requirement
   * @param fundingCurveName The discounting curve name
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param security The security
   * @param additionalConstraints The additional constraints
   * @return The discounting curve requirement
   */
  private static ValueRequirement getDiscountCurveRequirement(final String fundingCurveName, final String curveCalculationConfigName, final Security security,
      final ValueProperties additionalConstraints) {
    final ValueProperties properties = ValueProperties.builder() // TODO: Update to this => additionalConstraints.copy() [PLAT-5524]
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  /**
   * Gets the forward curve requirement
   * @param forwardCurveName The forward curve name
   * @param forwardCurveCalculationMethod The curve calculation method
   * @param underlyingBuid The underlying id of the security
   * @param additionalConstraints The additional constraints
   * @return The forward curve requirement
   */
  private static ValueRequirement getForwardCurveRequirement(final String forwardCurveName, final String forwardCurveCalculationMethod, final ExternalId underlyingBuid,
      final ValueProperties additionalConstraints) {
    final ValueProperties properties = ValueProperties.builder() // TODO: Update to this => additionalConstraints.copy() [PLAT-5524]
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    // REVIEW Andrew 2012-01-17 -- Why can't we just use the underlyingBuid external identifier directly here, with a target type of SECURITY, and shift the logic into the reference resolver?
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, underlyingBuid, properties);
  }

  /**
   * Gets the volatility surface requirement
   * @param tsSource The time series source
   * @param securitySource The security source
   * @param desiredValue The desired value
   * @param security The security
   * @param surfaceName The volatility surface name
   * @param forwardCurveName The forward curve name
   * @param surfaceCalculationMethod The surface calculation method
   * @param underlyingBuid The underlying id of the seucirty
   * @param additionalConstraints The additional requirements
   * @return The volatility surface requirement
   */
  protected ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource,
      final ValueRequirement desiredValue, final Security security, final String surfaceName, final String forwardCurveName,
      final String surfaceCalculationMethod, final ExternalId underlyingBuid, final ValueProperties additionalConstraints) {
    // REVIEW Andrew 2012-01-17 -- Could we pass a CTRef to the getSurfaceRequirement and use the underlyingBuid external identifier directly with a target type of SECURITY
    // TODO Casey - Replace desiredValue with smileInterpolatorName in BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement
    return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, ValueProperties.none(), surfaceName, forwardCurveName,
        InstrumentTypeProperties.EQUITY_OPTION, ComputationTargetType.PRIMITIVE, underlyingBuid);
    // TODO Casey - Replace above with below - ie pass additional constraints [PLAT-5524]
    //return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, additionalConstraints, surfaceName, forwardCurveName,
    //InstrumentTypeProperties.EQUITY_OPTION, ComputationTargetType.PRIMITIVE, underlyingBuid);
  }

  /**
   * Remaps the scheme of the underlying (e .g. from a Bloomberg ticker to a Bloomberg weak ticker) to match that
   * of the volatility surface
   * @param underlyingId The underlying id
   * @param tsSource The time series source
   * @param securitySource The security source
   * @param versionCorrection The version correction
   * @param surfaceName The surface name
   * @return The weak underlying id.
   */
  private static ExternalId getWeakUnderlyingId(final ExternalId underlyingId, final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource,
      final VersionCorrection versionCorrection, final String surfaceName) {
    /* scheme we return i.e. BBG_WEAK */
    final ExternalScheme desiredScheme = EquitySecurityUtils.getTargetType(surfaceName);
    /* scheme we look for i.e. BBG */
    final ExternalScheme sourceScheme = EquitySecurityUtils.getRemappedScheme(desiredScheme);
    if (desiredScheme == null) { // surface name is unknown
      return null;
    }
    if (underlyingId.isScheme(desiredScheme)) {
      return underlyingId;
    }
    if (underlyingId.isScheme(sourceScheme)) {
      return ExternalId.of(desiredScheme, underlyingId.getValue());
    }
    // load underlying and search its ids for the right one
    final Security underlyingSecurity = securitySource.getSingle(ExternalIdBundle.of(underlyingId), versionCorrection);
    if (underlyingSecurity == null || underlyingSecurity.getExternalIdBundle().getExternalId(desiredScheme) == null) {
      // no underlying in db (or lacks desired scheme) - get from timeseries
      final HistoricalTimeSeries historicalTimeSeries = tsSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, ExternalIdBundle.of(underlyingId), null, null, true, null, true, 1);
      if (historicalTimeSeries == null) {
        s_logger.error("Require a time series for " + underlyingId);
        return null;
      }
      final ExternalIdBundle idBundle = tsSource.getExternalIdBundle(historicalTimeSeries.getUniqueId());
      if (idBundle.getExternalId(sourceScheme) != null) {
        return ExternalId.of(desiredScheme, idBundle.getExternalId(sourceScheme).getValue());
      }
    }
    if (underlyingSecurity != null && underlyingSecurity.getExternalIdBundle().getExternalId(sourceScheme) != null) {
      return ExternalId.of(desiredScheme, underlyingSecurity.getExternalIdBundle().getExternalId(sourceScheme).getValue());
    }
    s_logger.error("Couldn't get ticker of type " + sourceScheme + " only have " + underlyingId);
    return null;
  }

  /**
   * Gets the value requirement names
   *
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

  /**
   * Gets the calculation method.
   *
   * @return The calculation method
   */
  protected abstract String getCalculationMethod();

  /**
   * Gets the model type.
   *
   * @return The model type
   */
  protected abstract String getModelType();

}
