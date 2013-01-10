/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.conversion.EquityIndexOptionConverter;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
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
  //TODO the next three properties should be moved from this class after checking that there's no others that match
  /** Property name for the discounting curve */
  public static final String PROPERTY_DISCOUNTING_CURVE_NAME = "DiscountingCurveName";
  /** Property name for the discounting curve configuration */
  public static final String PROPERTY_DISCOUNTING_CURVE_CONFIG = "DiscountingCurveConfig";
  /** Property name for the forward curve name */
  public static final String PROPERTY_FORWARD_CURVE_NAME = "ForwardCurveName";
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
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof EquityIndexOptionSecurity || security instanceof EquityOptionSecurity;
  }


  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> result = Sets.newHashSetWithExpectedSize(_valueRequirementNames.length);
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
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);

    // TODO: REVIEW THIS - TimeSeriesSource, used to get Ticker, the Vol ComputationTarget, from ExternalIdBundle
    // We are now also using the ticker for the Spot / Market_Value Requirement
    final HistoricalTimeSeriesSource tsSource = OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context);

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
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(tsSource, desiredValue, security, volSurfaceName, surfaceCalculationMethod, underlyingId);
    if (volReq == null) {
      return null;
    }

    // 3. Forward curve requirement
    final Set<String> forwardCurveNames = constraints.getValues(PROPERTY_FORWARD_CURVE_NAME);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String forwardCurveCalculationMethod = Iterables.getOnlyElement(forwardCurveCalculationMethods);
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(tsSource, forwardCurveName, forwardCurveCalculationMethod, security, underlyingId);
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
        .with(PROPERTY_FORWARD_CURVE_NAME, forwardCurveName);
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
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
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, FinancialSecurityUtils.getCurrency(security).getUniqueId(), properties);
  }

  private ValueRequirement getForwardCurveRequirement(final HistoricalTimeSeriesSource tsSource, final String forwardCurveName, final String forwardCurveCalculationMethod,
      final Security security, final ExternalId underlyingBuid) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    final String bbgTicker = getBloombergTicker(tsSource, underlyingBuid);
    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), bbgTicker);
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final ValueRequirement desiredValue, final Security security,
      final String surfaceName, final String surfaceCalculationMethod, final ExternalId underlyingBuid) {
    // Targets for equity vol surfaces are the underlying tickers
    final String bbgTicker = getBloombergTicker(tsSource, underlyingBuid);
    if (bbgTicker == null) {
      s_logger.error("Could not get Bloomberg ticker for underlying");
      return null;
    }
    final UniqueId newId = UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), bbgTicker); // FIXME: WEAK Tickers mean stale data. Also, this should NOT be hardcoded
    return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, surfaceName, InstrumentTypeProperties.EQUITY_OPTION, newId);
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
      s_logger.error("Unable to find option underlyer's ticker from the ExternalIdBundle");
      return null;
    }
    final HistoricalTimeSeries historicalTimeSeries = tsSource.getHistoricalTimeSeries("PX_LAST", ExternalIdBundle.of(underlyingBuid), null, null, true, null, true, 1);
    if (historicalTimeSeries == null) {
      s_logger.error("Require a time series for " + underlyingBuid);
      return null;
    }
    final ExternalIdBundle idBundle = tsSource.getExternalIdBundle(historicalTimeSeries.getUniqueId());
    final String bbgTicker = (idBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER)).getValue();
    return bbgTicker;
  }

  /**
   * Gets the value requirement names
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirementNames;
  }

  /**
   * Gets the calculation method.
   * @return The calculation method
   */
  protected abstract String getCalculationMethod();

  /**
   * Gets the model type.
   * @return The model type
   */
  protected abstract String getModelType();

}
