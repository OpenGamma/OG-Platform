/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.time.TimeCalculator;
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
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
* Produces a {@link ValueRequirementNames#BLACK_VOLATILITY_SURFACE} 
* from Forward and Discounting Curves, and the Option's {@link MarketDataRequirementNames#MARKET_VALUE} or {@link ValueRequirementNames#MARK_PREVIOUS}.<p>
*/
public class EquityBlackVolatilitySurfaceFromSinglePriceFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionFunction.class);

  /**
   * Property name for what to do if Implied Vol is undefined <p>
   * <p>
   * Property used to select method of dealing with rare case in which option and forward prices are such
   * that the implied volatility is not defined.<p>
   * This occurs when the discounted payoff is worth more than the option price.<p>
   * See child classes of this one.
   */
  public static final String PROPERTY_IMPLIED_VOL_BACKUP = "ImpliedVolBackup";
  /**
   * Selection of {@link PROPERTY_IMPLIED_VOL_BACKUP} which will throw an error
   * if implied vol is undefined
   */
  public static final String NO_VOL_BACKUP = "None";

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY; 
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  public ValueProperties getResultProperties() {
    ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.DISCOUNTING_CURVE_NAME)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.FORWARD_CURVE_NAME)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD)
        .get();
    return properties;
  }

  private ValueProperties getResultProperties(Set<ValueRequirement> desiredValues) {
    return desiredValues.iterator().next().getConstraints();
  }
  
  protected Set<ValueRequirement> getAddlRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<ValueRequirement> requirements = Sets.newHashSet();
    
    // 1. Market/Closing Value Requirement
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, target.toSpecification()));

    // 2. Discounting Curve Requirements
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    final String discountingCurveName = constraints.getStrictValue(ValuePropertyNames.DISCOUNTING_CURVE_NAME);
    if (discountingCurveName == null) {
      return null;
    }
    final String curveCalculationConfig = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfig == null) {
      return null;
    }
    final ValueProperties fundingCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, discountingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    final ValueRequirement discountCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(ccy), fundingCurveProperties);
    requirements.add(discountCurveRequirement);
    
    // 3. Forward Curve Requirement
    final String forwardCurveName = constraints.getStrictValue(ValuePropertyNames.FORWARD_CURVE_NAME);
    if (forwardCurveName == null) {
      return null;
    }   
    final String forwardCurveCalculationMethod = constraints.getStrictValue(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethod == null) {
      return null;
    }
    final ValueProperties forwardCurveProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, forwardCurveName)
      .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
      .get();
    // Next we need to determine the correct target for the ForwardCurve
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    if (underlyingId == null) {
      s_logger.debug("Did not find ExternalId for Security: {}", security);
      return null;
    }
    if (security instanceof EquityIndexFutureOptionSecurity) {
      final SecuritySource securitySource = context.getSecuritySource();
      IndexFutureSecurity future = (IndexFutureSecurity) securitySource.getSingle(ExternalIdBundle.of(underlyingId), context.getComputationTargetResolver().getVersionCorrection());
      if (future == null) {
        s_logger.debug("Did not find anything in SecuritySource for ExternalId: {}", underlyingId);
        return null;
      }
      final ExternalId indexId = future.getUnderlyingId();
      if (indexId == null) {
        s_logger.debug("Did not find ExternalId for underlying future security: {}", future);
        return null;
      }
      requirements.add(new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, indexId, forwardCurveProperties));
    } else {
      requirements.add(new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, underlyingId, forwardCurveProperties));
    }
    
    // 4. Add any additional requirements, and return
    Set<ValueRequirement> addlRequirements = getAddlRequirements(context, target, desiredValue);
    if (addlRequirements != null) {
      requirements.addAll(addlRequirements);
    }
    return requirements;
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final BlackVolatilitySurface<?> blackVolSurf = getVolatilitySurface(executionContext, inputs, target, desiredValues);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), getResultProperties(desiredValues));
    return Collections.singleton(new ComputedValue(spec, blackVolSurf));
  }
  
  // The Volatility Surface is simply a single point, which must be inferred from the market value
  protected BlackVolatilitySurface<?> getVolatilitySurface(final FunctionExecutionContext executionContext,
      final FunctionInputs inputs, final ComputationTarget target, Set<ValueRequirement> desiredValues) {

    // First, get the market value
    final ComputedValue optionPriceValue = inputs.getComputedValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, target.toSpecification()));
    if (optionPriceValue == null) {
      s_logger.error("Could not get value of security: {}", target.getSecurity());
      return null;
    }
    final Double spotOptionPrice = (Double) optionPriceValue.getValue();
    
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

    // From the option value, we invert the Black formula
    double forwardOptionPrice = spotOptionPrice / discountFactor;
    final Double impliedVol;
    final double intrinsic = Math.max(0.0, (forward - strike) * (isCall ? 1.0 : -1.0));
    if (intrinsic >= forwardOptionPrice) {
      s_logger.info("Implied Vol Error: " + security.getName() + " - Intrinsic value (" + intrinsic + ") > price (" + forwardOptionPrice + ")!");
      impliedVol = getImpliedVolIfPriceBelowPayoff(inputs, target, discountFactor, forward, strike, timeToExpiry, isCall);
    } else {
      impliedVol = BlackFormulaRepository.impliedVolatility(forwardOptionPrice, forward, strike, timeToExpiry, isCall);
    }
    if (impliedVol == null) {
      s_logger.error("Unable to compute implied vol");
      return null;
    }
    final Surface<Double, Double, Double> surface = ConstantDoublesSurface.from(impliedVol);
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = new BlackVolatilitySurfaceMoneyness(surface, forwardCurve);
    return impliedVolatilitySurface;
  }
  
  protected Double getImpliedVolIfPriceBelowPayoff(final FunctionInputs inputs, final ComputationTarget target, 
      final double discountFactor, final double forward, final double strike, final double timeToExpiry, final boolean isCall) {
    s_logger.error("Setting implied volatility to null");
    return null;
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


}
