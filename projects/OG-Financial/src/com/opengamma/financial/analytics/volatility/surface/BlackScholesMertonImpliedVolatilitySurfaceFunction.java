/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  // NEW METHODS:
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final EquityOptionSecurity optionSec = (EquityOptionSecurity) target.getSecurity();
    SecuritySource securityMaster = context.getSecuritySource();
    Security underlying = securityMaster.getSecurity(ExternalIdBundle.of(optionSec.getUnderlyingId()));
    final ValueRequirement optionMarketDataReq = getPriceRequirement(optionSec.getUniqueId());
    final ValueRequirement underlyingMarketDataReq = getPriceRequirement(underlying.getUniqueId());
    final ValueRequirement discountCurveReq = getDiscountCurveMarketDataRequirement(optionSec.getCurrency().getUniqueId());
    // TODO will need a cost-of-carry model as well
    final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
    optionRequirements.add(optionMarketDataReq);
    optionRequirements.add(underlyingMarketDataReq);
    optionRequirements.add(discountCurveReq);
    return optionRequirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final ValueProperties.Builder props = createValueProperties((EquityOptionSecurity) target.getSecurity());
    return Collections.singleton(createResultSpecification(target.toSpecification(), props));
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
    final ValueRequirement discountCurveReq = getDiscountCurveMarketDataRequirement(optionSec.getCurrency().getUniqueId());

    final Double optionPrice = (Double) inputs.getValue(optionPriceReq);
    final Double underlyingPrice = (Double) inputs.getValue(underlyingPriceReq);
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(discountCurveReq);
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
    
    // Package the result
    final ValueSpecification resultSpec = createResultSpecification(target.toSpecification(), createValueProperties(optionSec));
    final ComputedValue resultValue = new ComputedValue(resultSpec, volatilitySurface);
    return Collections.singleton(resultValue);
  }

  protected ValueProperties.Builder createValueProperties(final EquityOptionSecurity targetSecurity) {
    return createValueProperties().with(ValuePropertyNames.CURRENCY, targetSecurity.getCurrency().getCode());
  }

  protected ValueSpecification createResultSpecification(final ComputationTargetSpecification target, ValueProperties.Builder props) {
    return new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE, target, props.get());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private ValueRequirement getPriceRequirement(final UniqueId uid) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final UniqueId uid) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid);
  }
}
