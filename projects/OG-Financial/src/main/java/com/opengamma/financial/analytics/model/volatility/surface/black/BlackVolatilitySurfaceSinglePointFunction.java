/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.time.TimeCalculator;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Computes a flat volatility surface from a single market traded option price, and a forward curve
 */
public class BlackVolatilitySurfaceSinglePointFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    // The Security itself is the ComputationTarget. From it, we get strike and expiry information to compute implied volatility
    // The types we're concerned about: EquityOptionSecurity, EquityIndexOptionSecurity, EquityIndexFutureOptionSecurity
    // For which the strings are: EQUITY_OPTION, EQUITY_INDEX_OPTION, EQUITY_INDEX_FUTURE_OPTION
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

    // The ForwardCurve was a requirement. We get both forward and spot values from it. 
    // The latter is used to form the forwardOptionPrice given a (spot) market value
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final double forward = forwardCurve.getForward(timeToExpiry);
    final double spot = forwardCurve.getForward(0.0);

    // The Volatility Surface is simply a single point, which must be inferred from the market value
    final Object optionPriceObject = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (optionPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get market value of underlying option");
    }
    final double spotOptionPrice = (double) optionPriceObject;
    final double forwardOptionPrice = spotOptionPrice * forward / spot;

    double impliedVol = BlackFormulaRepository.impliedVolatility(forwardOptionPrice, forward, strike, timeToExpiry, 0.3);

    final Surface<Double, Double, Double> surface = ConstantDoublesSurface.from(impliedVol);
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = new BlackVolatilitySurfaceMoneyness(surface, forwardCurve);
    final ValueProperties properties = getResultProperties(desiredValues.iterator().next());
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
  }

  private ValueProperties getResultProperties(ValueRequirement next) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    // TODO: Check this; there is a cast in the execute method, but the comment suggests other securities are also valid
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .with(SURFACE, "SinglePoint")
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType(target))
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD)
        .withAny(CURVE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  // TODO: Review: I would prefer not to have to create a version of this for each instrument type...
  // TODO: InstrumentType is hard-coded. Could this be derived from the target Security?
  private String getInstrumentType(ComputationTarget target) {
    return InstrumentTypeProperties.EQUITY_OPTION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(SURFACE); // TODO: What is the effect of this? wrt typical setup with actual surfaces?
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveNames = constraints.getValues(CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(target, desiredValue);
    final ValueRequirement optionPriceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId());
    return Sets.newHashSet(forwardCurveRequirement, optionPriceRequirement);
  }

  protected ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .get();

    // FIXME This isn't quite going to fly. We need the Security (i.e. the Option) to get its price, ttm and strike, 
    // but we also need the Underlying, either to specify the Forward to build, or we can do it via the Spot price and a discount curve.. 
    // !!! What I've done below will probably work as a gap-solution. Just need to follow EquityForwardCurveFunction's getResults

    ExternalId underlyingExtId = ((EquityOptionSecurity) target.getSecurity()).getUnderlyingId();
    UniqueId underlyingUniqId = UniqueId.of(underlyingExtId);
    ComputationTargetSpecification underlyingSpec = ComputationTargetSpecification.of(underlyingUniqId);
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, underlyingSpec, properties);
  }

  private static final Logger s_logger = LoggerFactory.getLogger(BlackVolatilitySurfaceSinglePointFunction.class);
}
