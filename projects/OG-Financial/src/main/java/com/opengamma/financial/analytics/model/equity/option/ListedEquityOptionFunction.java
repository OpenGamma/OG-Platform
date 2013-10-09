/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.time.TimeCalculator;
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
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Abstract base function of a family that parallels EquityOptionFunction. As the name implies, they require the security be listed, ie market traded.
 * <p>
 * In this family, we do not take as input an entire volatility surface (ValueRequirementNames.BLACK_VOLATILITY_SURFACE). Instead, the function requires the market_value of the option, and the
 * volatility is implied from that, along with the requirement of a forward curve (ValueRequirementNames.FORWARD_CURVE), and its contract parameters of expiry and strike.
 * <p>
 * <p>
 * This greatly reduces the data requirements of these functions, at the expense of ability to capture structure in strike and expiry space.
 */
public abstract class ListedEquityOptionFunction extends AbstractFunction.NonCompiledInvoker {

  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionFunction.class);

  /** Property name for the discounting curve */
  public static final String PROPERTY_DISCOUNTING_CURVE_NAME = "DiscountingCurveName";
  /** Property name for the discounting curve configuration */
  public static final String PROPERTY_DISCOUNTING_CURVE_CONFIG = "DiscountingCurveConfig";

  private static final ComputationTargetType TARGET_TYPE = FinancialSecurityTypes.EQUITY_OPTION_SECURITY
      .or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY)
      .or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY);

  /** The value requirement name */
  private final String[] _valueRequirementNames;
  /** Converts the security to the form used in analytics. Set in init(), not constructor */
  private EquityOptionsConverter _converter;

  /**
   * @param valueRequirementNames A list of value requirement names, not null or empty
   */
  public ListedEquityOptionFunction(final String... valueRequirementNames) {
    ArgumentChecker.notEmpty(valueRequirementNames, "value requirement names");
    _valueRequirementNames = valueRequirementNames;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return TARGET_TYPE;
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

  /**
   * Constructs a market data bundle of type StaticReplicationDataBundle. In the {@link CalculationPropertyNamesAndValues#BLACK_BASIC_METHOD}, the volatility surface is a constant inferred from the
   * market price and the forward
   * 
   * @param underlyingId The underlying id of the index option
   * @param executionContext The execution context
   * @param inputs The market data inputs
   * @param target The target
   * @param desiredValues The desired values of the function
   * @return The market data bundle used in pricing
   */

  protected StaticReplicationDataBundle buildMarketBundle(final ExternalId underlyingId, final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final YieldCurve discountingCurve = getDiscountingCurve(inputs);
    final ForwardCurve forwardCurve = getForwardCurve(inputs);
    final BlackVolatilitySurface<?> blackVolSurf = getVolatilitySurface(executionContext, inputs, target);
    return new StaticReplicationDataBundle(blackVolSurf, discountingCurve, forwardCurve);
  }

  protected YieldCurve getDiscountingCurve(final FunctionInputs inputs) {
    final Object discountingObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (discountingObject == null) {
      throw new OpenGammaRuntimeException("Could not get discounting Curve");
    }
    if (!(discountingObject instanceof YieldCurve)) {
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    return (YieldCurve) discountingObject;
  }

  protected ForwardCurve getForwardCurve(final FunctionInputs inputs) {
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    return (ForwardCurve) forwardCurveObject;
  }

  // The Volatility Surface is simply a single point, which must be inferred from the market value
  protected BlackVolatilitySurface<?> getVolatilitySurface(final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target) {

    // From the Security, we get strike and expiry information to compute implied volatility
    final double strike;
    final Expiry expiry;
    final boolean isCall;
    final Security security = target.getSecurity();
    if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity option = (EquityOptionSecurity) security;
      strike = option.getStrike();
      expiry = option.getExpiry();
      isCall = option.getOptionType().equals(OptionType.CALL);
    } else if (security instanceof EquityIndexOptionSecurity) {
      final EquityIndexOptionSecurity option = (EquityIndexOptionSecurity) security;
      strike = option.getStrike();
      expiry = option.getExpiry();
      isCall = option.getOptionType().equals(OptionType.CALL);
    } else if (security instanceof EquityIndexFutureOptionSecurity) {
      final EquityIndexFutureOptionSecurity option = (EquityIndexFutureOptionSecurity) security;
      strike = option.getStrike();
      expiry = option.getExpiry();
      isCall = option.getOptionType().equals(OptionType.CALL);
    } else {
      throw new OpenGammaRuntimeException("Security type not handled," + security.getName());
    }
    if (expiry.getAccuracy().equals(ExpiryAccuracy.MONTH_YEAR) || expiry.getAccuracy().equals(ExpiryAccuracy.YEAR)) {
      throw new OpenGammaRuntimeException("There is ambiguity in the expiry date of the target security.");
    }
    final ZonedDateTime expiryDate = expiry.getExpiry();
    final ZonedDateTime valuationDT = ZonedDateTime.now(executionContext.getValuationClock());
    double timeToExpiry = TimeCalculator.getTimeBetween(valuationDT, expiryDate);
    if (timeToExpiry == 0) { // TODO: See JIRA [PLAT-3222]
      timeToExpiry = 0.0015;
    }

    // From the curve requirements, we get the forward and zero coupon prices
    final ForwardCurve forwardCurve = getForwardCurve(inputs);
    final double forward = forwardCurve.getForward(timeToExpiry);
    final double discountFactor = getDiscountingCurve(inputs).getDiscountFactor(timeToExpiry);

    // From the market value, we can then invert the Black formula
    final ComputedValue optionPriceValue = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (optionPriceValue == null) {
      throw new OpenGammaRuntimeException("Could not get market value of underlying option");
    }
    final Double spotOptionPrice = (Double) optionPriceValue.getValue();
    final double forwardOptionPrice = spotOptionPrice / discountFactor;

    // TODO: Have been running into problems, primarily from illiquid option prices, hence we test
    final double impliedVol;
    final double intrinsic = Math.max(0.0, (forward - strike) * (isCall ? 1.0 : -1.0));
    if (intrinsic >= forwardOptionPrice) {
      s_logger.info("Option with intrinsic value (" + intrinsic + ") > price (" + forwardOptionPrice + ")! Setting implied volatility to zero, " + security);
      impliedVol = 0.0;
    } else {
      impliedVol = BlackFormulaRepository.impliedVolatility(forwardOptionPrice, forward, strike, timeToExpiry, isCall);
    }

    final Surface<Double, Double, Double> surface = ConstantDoublesSurface.from(impliedVol);
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = new BlackVolatilitySurfaceMoneyness(surface, forwardCurve);
    return impliedVolatilitySurface;
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

  private static String oneOrNull(final Collection<String> values) {
    if ((values == null) || values.isEmpty() || (values.size() != 1)) {
      return null;
    }
    return Iterables.getOnlyElement(values);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    String discountingCurveName = null;
    String discountingCurveConfig = null;
    String forwardCurveName = null;
    String forwardCurveCalculationMethod = null;
    ValueProperties.Builder additionalConstraintsBuilder = null;
    if ((constraints.getProperties() == null) || constraints.getProperties().isEmpty()) {
      return null;
    }
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod == null || calculationMethod.isEmpty()) {
      return null;
    }
    for (final String property : constraints.getProperties()) {
      if (ValuePropertyNames.CALCULATION_METHOD.equals(property)) {
        if (!constraints.getValues(property).contains(getCalculationMethod())) {
          return null;
        }
      } else if (PROPERTY_DISCOUNTING_CURVE_NAME.equals(property)) {
        discountingCurveName = oneOrNull(constraints.getValues(property));
      } else if (PROPERTY_DISCOUNTING_CURVE_CONFIG.equals(property)) {
        discountingCurveConfig = oneOrNull(constraints.getValues(property));
      } else if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME.equals(property)) {
        forwardCurveName = oneOrNull(constraints.getValues(property));
      } else if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(property)) {
        forwardCurveCalculationMethod = oneOrNull(constraints.getValues(property));
      } else {
        if (additionalConstraintsBuilder == null) {
          additionalConstraintsBuilder = ValueProperties.builder();
        }
        final Set<String> values = constraints.getValues(property);
        if (values.isEmpty()) {
          additionalConstraintsBuilder.withAny(property);
        } else {
          additionalConstraintsBuilder.with(property, values);
        }
      }
    }
    if ((discountingCurveName == null) || (discountingCurveConfig == null) ||
        (forwardCurveName == null) || (forwardCurveCalculationMethod == null)) {
      return null;
    }
    final ValueProperties additionalConstraints = (additionalConstraintsBuilder != null) ? additionalConstraintsBuilder.get() : ValueProperties.none();

    // Get security and its underlying's ExternalId.
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    if (underlyingId == null) {
      return null;
    }
    // Discounting curve
    final ValueRequirement discountingReq = getDiscountCurveRequirement(discountingCurveName, discountingCurveConfig, security, additionalConstraints);
    if (discountingReq == null) {
      return null;
    }
    // Forward curve
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(forwardCurveName, forwardCurveCalculationMethod, underlyingId, additionalConstraints);
    if (forwardCurveReq == null) {
      return null;
    }
    // "Volatility Surface"
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(security);
    if (volReq == null) {
      return null;
    }
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
      } else if (inputName.equals(MarketDataRequirementNames.MARKET_VALUE) && !surfacePropertiesSet) { // BlackBasic case
        surfacePropertiesSet = true;
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

  private ValueRequirement getDiscountCurveRequirement(final String fundingCurveName, final String curveCalculationConfigName, final Security security, final ValueProperties additionalConstraints) {
    final ValueProperties properties = ValueProperties.builder() // TODO: Update to this => additionalConstraints.copy()
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(security)), properties);
  }

  private ValueRequirement getForwardCurveRequirement(final String forwardCurveName, final String forwardCurveCalculationMethod, final ExternalId underlyingBuid,
      final ValueProperties additionalConstraints) {
    final ValueProperties properties = ValueProperties.builder() // TODO: Update to this => additionalConstraints.copy()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    // REVIEW Andrew 2012-01-17 -- Why can't we just use the underlyingBuid external identifier directly here, with a target type of SECURITY, and shift the logic into the reference resolver?
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, underlyingBuid, properties);
  }

  /**
   * Instead of a volatility surface, we're just asking for the market_value of the option
   * 
   * @param security the resolved option
   * @return market_value requirement for the option
   */
  protected ValueRequirement getVolatilitySurfaceRequirement(final Security security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
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
