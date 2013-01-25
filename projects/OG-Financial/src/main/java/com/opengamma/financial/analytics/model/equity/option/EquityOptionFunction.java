/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
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
import com.opengamma.financial.analytics.conversion.EquityIndexOptionConverter;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
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
      throw new OpenGammaRuntimeException("Could not get discounting Curve");
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
    return FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY);
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
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod != null && calculationMethod.size() == 1) {
      if (!getCalculationMethod().equals(Iterables.getOnlyElement(calculationMethod))) {
        return null;
      }
    }
    // Get security and its underlying's ExternalId.
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();

    // 1. Funding Curve Requirement
    // Funding curve
    final Set<String> discountingCurveNames = constraints.getValues(PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final String discountingCurveName = Iterables.getOnlyElement(discountingCurveNames);
    final Set<String> discountingCurveConfigs = constraints.getValues(PROPERTY_DISCOUNTING_CURVE_CONFIG);
    if (discountingCurveConfigs == null || discountingCurveConfigs.size() != 1) {
      return null;
    }
    final String discountingCurveConfig = Iterables.getOnlyElement(discountingCurveConfigs);
    final ValueRequirement discountingReq = getDiscountCurveRequirement(discountingCurveName, discountingCurveConfig, security);

    // 2. Volatility Surface Requirement
    // Surface Name
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String volSurfaceName = Iterables.getOnlyElement(surfaceNames);
    // Surface calculation method
    final Set<String> surfaceCalculationMethods = constraints.getValues(ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    if (surfaceCalculationMethods == null || surfaceCalculationMethods.size() != 1) {
      return null;
    }
    final String surfaceCalculationMethod = Iterables.getOnlyElement(surfaceCalculationMethods);

    // 3. Forward curve requirement
    final Set<String> forwardCurveNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
    final String forwardCurveCalculationMethod = Iterables.getOnlyElement(forwardCurveCalculationMethods);
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    final HistoricalTimeSeriesSource tsSource = OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(tsSource, securitySource, desiredValue, security, volSurfaceName, forwardCurveName,
        surfaceCalculationMethod, underlyingId);
    if (volReq == null) {
      return null;
    }
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(tsSource, securitySource, forwardCurveName, forwardCurveCalculationMethod, security, underlyingId);
    // Return the set
    return Sets.newHashSet(discountingReq, volReq, forwardCurveReq);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    boolean discountCurvePropertiesSet = false;
    boolean forwardCurvePropertiesSet = false;
    boolean surfacePropertiesSet = false;
    String forwardCurveName = null;
    String discountingCurveName = null;
    String discountingCurveConfig = null;
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethod())
        .with(CalculationPropertyNamesAndValues.PROPERTY_MODEL_TYPE, getModelType())
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification value = entry.getKey();
      final String inputName = value.getValueName();
      if (inputName.equals(ValueRequirementNames.YIELD_CURVE) && !discountCurvePropertiesSet) {
        final ValueProperties curveProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(ValuePropertyNames.CURVE)
            .withoutAny(ValuePropertyNames.CURRENCY)
            .get();
        discountingCurveName = value.getProperty(ValuePropertyNames.CURVE);
        discountingCurveConfig = value.getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        for (final String property : curveProperties.getProperties()) {
          properties.with(property, curveProperties.getValues(property));
        }
        discountCurvePropertiesSet = true;
      } else if (inputName.equals(ValueRequirementNames.BLACK_VOLATILITY_SURFACE) && !surfacePropertiesSet) {
        final ValueProperties surfaceProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE)
            .get();
        for (final String property : surfaceProperties.getProperties()) {
          properties.with(property, surfaceProperties.getValues(property));
        }
        surfacePropertiesSet = true;
      } else if (inputName.equals(ValueRequirementNames.FORWARD_CURVE) && !forwardCurvePropertiesSet) {
        final ValueProperties forwardCurveProperties = value.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .withoutAny(ValuePropertyNames.CURVE)
            .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
            .get();
        forwardCurveName = value.getProperty(ValuePropertyNames.CURVE);
        for (final String property : forwardCurveProperties.getProperties()) {
          properties.with(property, forwardCurveProperties.getValues(property));
        }
        forwardCurvePropertiesSet = true;
      }
    }
    assert discountCurvePropertiesSet;
    assert forwardCurvePropertiesSet;
    assert surfacePropertiesSet;
    properties
      .with(PROPERTY_DISCOUNTING_CURVE_NAME, discountingCurveName)
      .with(PROPERTY_DISCOUNTING_CURVE_CONFIG, discountingCurveConfig)
      .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME, forwardCurveName);
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirementNames) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties.get()));
    }
    return results;
  }

  private ValueRequirement getDiscountCurveRequirement(final String fundingCurveName, final String curveCalculationConfigName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  private ValueRequirement getForwardCurveRequirement(final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource,
      final String forwardCurveName, final String forwardCurveCalculationMethod, final Security security, final ExternalId underlyingBuid) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    // REVIEW Andrew 2012-01-17 -- Why can't we just use the underlyingBuid external identifier directly here, with a target type of SECURITY, and shift the logic into the reference resolver?
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, getWeakUnderlyingId(underlyingBuid, tsSource, securitySource), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource,
      final ValueRequirement desiredValue, final Security security, final String surfaceName, final String forwardCurveName,
      final String surfaceCalculationMethod, final ExternalId underlyingBuid) {
    // REVIEW Andrew 2012-01-17 -- Could we pass a CTRef to the getSurfaceRequirement and use the underlyingBuid external identifier directly with a target type of SECURITY
    return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, surfaceName, forwardCurveName, InstrumentTypeProperties.EQUITY_OPTION,
        ComputationTargetType.PRIMITIVE, getWeakUnderlyingId(underlyingBuid, tsSource, securitySource));
  }

  private ExternalId getWeakUnderlyingId(final ExternalId underlyingId, final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource) {
    if (ExternalSchemes.BLOOMBERG_BUID.equals(underlyingId.getScheme())) {
      // this is a hack so it doesn't hammer the db.
      final Instant futureHour = Instant.ofEpochMillis(((System.currentTimeMillis() / 3600_000) * 3600_000) + 3600_000);
      final Security underlyingSecurity = securitySource.getSingle(ExternalIdBundle.of(underlyingId), VersionCorrection.of(futureHour, futureHour));
      if (underlyingSecurity == null) {
        final HistoricalTimeSeries historicalTimeSeries = tsSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, ExternalIdBundle.of(underlyingId), null, null, true, null, true, 1);
        if (historicalTimeSeries == null) {
          s_logger.error("Require a time series for " + underlyingId);
          return null;
        }
        final ExternalIdBundle idBundle = tsSource.getExternalIdBundle(historicalTimeSeries.getUniqueId());
        return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, idBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER).getValue());
      }
      // REVIEW Andrew -- Is this line correct; the use of the unique id will give BLOOMBERG_TICKER_WEAK~12345 since the unique Id from the
      // sec master might be an arbitrary long from the database?
      return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, underlyingSecurity.getUniqueId().getValue());
    }
    if (ExternalSchemes.BLOOMBERG_TICKER.equals(underlyingId.getScheme())) {
      return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, underlyingId.getValue());
    }
    return underlyingId;
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
