/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * In this form, we do not take as input an entire volatility surface (ValueRequirementNames.BLACK_VOLATILITY_SURFACE). Instead, the implied volatility is implied by the market_value of the security,
 * along with it's contract parameters of expiry and strike, along with the requirement of a forward curve (ValueRequirementNames.FORWARD_CURVE).
 */
public abstract class EquityOptionBlackBasicFunction extends EquityOptionFunction {

  /** @param valueRequirementName The value requirement names, not null */
  public EquityOptionBlackBasicFunction(final String... valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BLACK_BASIC_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }

  @Override
  /** Instead of a volatility surface, we're just asking for the market_value of the option */
  protected ValueRequirement getVolatilitySurfaceRequirement(final HistoricalTimeSeriesSource tsSource, final SecuritySource securitySource,
      final ValueRequirement desiredValue, final Security security, final String surfaceName, final String forwardCurveName,
      final String surfaceCalculationMethod, final ExternalId underlyingBuid, final ValueProperties additionalConstraints) {

    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }

  @Override
  protected void extractInputProperties(final ValueSpecification input, final ValueProperties.Builder properties) {
    if (MarketDataRequirementNames.MARKET_VALUE.equals(input.getValueName())) {
      // TODO: Add any additional properties for the BlackBasic MarketValue result
      // FIXME: For prototyping, I am adding stubs for what the default functions are going to add anyway...
      //        ValueProperties surfaceProperties = BlackVolatilitySurfacePropertyUtils.addAllBlackSurfaceProperties(ValueProperties.none(), 
      //            InstrumentTypeProperties.EQUITY_OPTION, BlackVolatilitySurfacePropertyNamesAndValues.SPLINE).get();
      //        for (final String property : surfaceProperties.getProperties()) {
      //          properties.with(property, surfaceProperties.getValues(property));
      //        }
      return;
    }
    super.extractInputProperties(input, properties);
  }

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
  @Override
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
    if (!(discountingObject instanceof YieldCurve)) { //TODO: make it more generic
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
    // TODO: INDUSTRIALISE: The types we're concerned about: EquityOptionSecurity, EquityIndexOptionSecurity, EquityIndexFutureOptionSecurity
    final EquityOptionSecurity security = (EquityOptionSecurity) target.getSecurity();
    final double strike = security.getStrike();
    final Expiry expiry = security.getExpiry();
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
    final Object optionPriceObject = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (optionPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get market value of underlying option");
    }
    final double spotOptionPrice = (double) optionPriceObject;
    final double forwardOptionPrice = spotOptionPrice / discountFactor;

    final double impliedVol = BlackFormulaRepository.impliedVolatility(forwardOptionPrice, forward, strike, timeToExpiry, 0.3);

    final Surface<Double, Double, Double> surface = ConstantDoublesSurface.from(impliedVol);
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = new BlackVolatilitySurfaceMoneyness(surface, forwardCurve);
    return impliedVolatilitySurface;
  }

}
