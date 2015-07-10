/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 * 
 */
public class PractitionerBlackScholesVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final ValueRequirement underlyingPriceRequirement = getPriceRequirement(option.getUnderlyingId());
    final ValueRequirement discountCurveDataRequirement = getDiscountCurveMarketDataRequirement(option.getCurrency());
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(discountCurveDataRequirement);
    final double spotPrice = (Double) inputs.getValue(underlyingPriceRequirement);
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry());
    final double b = discountCurve.getInterestRate(t); // TODO cost-of-carry model
    @SuppressWarnings("unused")
    final StandardOptionDataBundle data = new StandardOptionDataBundle(discountCurve, b, null, spotPrice, now);
    // TODO Map<OptionDefinition, Double> of options that will be used to form surface
    final VolatilitySurface surface = null; // TODO
    final ValueSpecification specification = createResultSpecification(target);
    final ComputedValue result = new ComputedValue(specification, surface);
    return Collections.singleton(result);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    // TODO: need most liquid options on same underlying OR all options around the strike + time to expiry of this
    // option
    // TODO: need to make sure that these options surround the time to expiry and strike of this option
    // TODO: the surface need only be calculated once per _underlying_, not individual option (as long as point 2
    // above holds)
    final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
    optionRequirements.add(getPriceRequirement(option.getUnderlyingId()));
    optionRequirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency()));
    // TODO: add the other stuff
    return optionRequirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Collections.singleton(createResultSpecification(target));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PractitionerBlackScholesMertonVolatilitySurface";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

  private ValueSpecification createResultSpecification(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE, target.toSpecification(), createValueProperties().get());
  }

  private ValueRequirement getPriceRequirement(final ExternalId identifier) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, identifier);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final Currency ccy) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(ccy));
  }
}
