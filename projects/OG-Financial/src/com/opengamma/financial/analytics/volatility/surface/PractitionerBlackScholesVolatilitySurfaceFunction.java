/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 * 
 */
public class PractitionerBlackScholesVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final SecuritySource securityMaster = executionContext.getSecuritySource();
    final Security underlying = securityMaster.getSecurity(ExternalIdBundle.of(option.getUnderlyingId()));
    final ValueRequirement underlyingPriceRequirement = getPriceRequirement(underlying.getUniqueId());
    final ValueRequirement discountCurveDataRequirement = getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueId());
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(discountCurveDataRequirement);
    final double spotPrice = (Double) inputs.getValue(underlyingPriceRequirement);
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t); // TODO cost-of-carry model
    @SuppressWarnings("unused")
    final StandardOptionDataBundle data = new StandardOptionDataBundle(discountCurve, b, null, spotPrice, now);
    // TODO Map<OptionDefinition, Double> of options that will be used to form surface
    final VolatilitySurface surface = null; // TODO
    final ValueSpecification specification = createResultSpecification(option);
    final ComputedValue result = new ComputedValue(specification, surface);
    return Collections.singleton(result);
  }

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
    if (canApplyTo(context, target)) {
      final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
      // TODO: need most liquid options on same underlying OR all options around the strike + time to expiry of this
      // option
      // TODO: need to make sure that these options surround the time to expiry and strike of this option
      // TODO: the surface need only be calculated once per _underlying_, not individual option (as long as point 2
      // above holds)
      final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
      final SecuritySource securityMaster = context.getSecuritySource();
      final Security underlying = securityMaster.getSecurity(ExternalIdBundle.of(option.getUnderlyingId()));
      optionRequirements.add(getPriceRequirement(underlying.getUniqueId()));
      optionRequirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueId()));
      // TODO: add the other stuff
      return optionRequirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Collections.singleton(createResultSpecification(target.getSecurity()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PractitionerBlackScholesMertonVolatilitySurface";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private ValueSpecification createResultSpecification(final Security security) {
    final ValueRequirement resultRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, security.getUniqueId());
    final ValueSpecification resultSpec = new ValueSpecification(resultRequirement, getUniqueId());
    return resultSpec;
  }

  private ValueRequirement getPriceRequirement(final UniqueId uid) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final UniqueId uid) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid);
  }
}
