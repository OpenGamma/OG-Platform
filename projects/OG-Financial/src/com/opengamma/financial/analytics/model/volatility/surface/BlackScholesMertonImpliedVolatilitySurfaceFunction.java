/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface.fitting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BlackScholesMertonImpliedVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(BlackScholesMertonImpliedVolatilitySurfaceFunction.class);

  private final BlackScholesMertonImpliedVolatilitySurfaceModel _volatilitySurfaceModel;

  public BlackScholesMertonImpliedVolatilitySurfaceFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public String getShortName() {
    return "BlackScholesMertonImpliedVolatilitySurface";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder props = createValueProperties((EquityOptionSecurity) target.getSecurity());
    props.withAny(ValuePropertyNames.CURVE);
    return Sets.newHashSet(createVolSurfaceResultSpecification(target.toSpecification(), props), createImpliedVolResultSpecification(target.toSpecification(), props));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final EquityOptionSecurity optionSec = (EquityOptionSecurity) target.getSecurity();
    SecuritySource securityMaster = context.getSecuritySource();
    Security underlying = securityMaster.getSecurity(ExternalIdBundle.of(optionSec.getUnderlyingId()));
    final ValueRequirement optionMarketDataReq = getPriceRequirement(optionSec.getUniqueId());
    final ValueRequirement underlyingMarketDataReq = getPriceRequirement(underlying.getUniqueId());
    final ValueRequirement discountCurveReq = getDiscountCurveMarketDataRequirement(optionSec.getCurrency().getUniqueId(), curveName);
    // TODO will need a cost-of-carry model as well
    final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
    optionRequirements.add(optionMarketDataReq);
    optionRequirements.add(underlyingMarketDataReq);
    optionRequirements.add(discountCurveReq);
    return optionRequirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (ValueSpecification input : inputs.keySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getValueName())) {
        curveName = input.getProperty(ValuePropertyNames.CURVE);
      }
    }
    final ValueProperties.Builder props = createValueProperties((EquityOptionSecurity) target.getSecurity());
    props.with(ValuePropertyNames.CURVE, curveName);
    return Sets.newHashSet(createVolSurfaceResultSpecification(target.toSpecification(), props), createImpliedVolResultSpecification(target.toSpecification(), props));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime today = executionContext.getValuationClock().zonedDateTime();
    final EquityOptionSecurity optionSec = (EquityOptionSecurity) target.getSecurity();

    SecuritySource secMaster = executionContext.getSecuritySource();
    Security underlying = secMaster.getSecurity(ExternalIdBundle.of(optionSec.getUnderlyingId()));

    // Get inputs:
    final ValueRequirement optionPriceReq = getPriceRequirement(optionSec.getUniqueId());
    final ValueRequirement underlyingPriceReq = getPriceRequirement(underlying.getUniqueId());

    final Double optionPrice = (Double) inputs.getValue(optionPriceReq);
    final Double underlyingPrice = (Double) inputs.getValue(underlyingPriceReq);
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    // TODO cost-of-carry model
    if (optionPrice == null) {
      s_logger.warn("No market value for option price");
    }
    if (underlyingPrice == null) {
      s_logger.warn("No market value for underlying price");
    }

    // Perform the calculation:
    final Expiry expiry = optionSec.getExpiry();
    final double years = DateUtils.getDifferenceInYears(today, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(years); // TODO
    final OptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(optionSec.getStrike(), expiry, (optionSec.getOptionType() == OptionType.CALL));
    final Map<OptionDefinition, Double> prices = new HashMap<OptionDefinition, Double>();
    prices.put(europeanVanillaOptionDefinition, optionPrice);
    final VolatilitySurface volatilitySurface = _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, underlyingPrice, today));
    
    //This is so cheap no need to check desired values
    final double impliedVol = volatilitySurface.getVolatility(0.0, 0.0); //This surface is constant
    
    // Package the result
    final ValueProperties.Builder properties = createValueProperties(optionSec);
    properties.with(ValuePropertyNames.CURVE, desiredValues.iterator().next().getConstraint(ValuePropertyNames.CURVE));
    final ValueSpecification resultSpec = createVolSurfaceResultSpecification(target.toSpecification(), properties);
    final ComputedValue resultValue = new ComputedValue(resultSpec, volatilitySurface);
    final ValueSpecification impliedResultSpec = createImpliedVolResultSpecification(target.toSpecification(), properties);
    final ComputedValue impliedResultValue = new ComputedValue(impliedResultSpec, impliedVol);
    return Sets.newHashSet(resultValue, impliedResultValue);
  }

  protected ValueProperties.Builder createValueProperties(final EquityOptionSecurity targetSecurity) {
    return createValueProperties().with(ValuePropertyNames.CURRENCY, targetSecurity.getCurrency().getCode());
  }

  protected ValueSpecification createVolSurfaceResultSpecification(final ComputationTargetSpecification target, ValueProperties.Builder props) {
    return new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE, target, props.get());
  }

  protected ValueSpecification createImpliedVolResultSpecification(final ComputationTargetSpecification target, ValueProperties.Builder props) {
    return new ValueSpecification(ValueRequirementNames.SECURITY_IMPLIED_VOLATLITY, target, props.get());
  }

  private ValueRequirement getPriceRequirement(final UniqueId uid) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final UniqueId uid, final String curveName) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
  }
}
