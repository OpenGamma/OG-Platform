/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
public class GarmanKohlhagenFXOptionModelFunction extends BlackScholesMertonModelFunction {

  @Override
  protected StandardOptionDataBundle getDataBundle(final SecuritySource secMaster, final Clock relevantTime, final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    final FXOptionSecurity fxOption = (FXOptionSecurity) option;
    final Security underlying = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier())); //TODO make sure spot FX rate is right way up
    final Double spotAsObject = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
    if (spotAsObject == null) {
      throw new NullPointerException("No spot value for underlying instrument.");
    }
    final double spot = spotAsObject;
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(option));
    //TODO check call / put are actually the right way around
    final YieldAndDiscountCurve domesticCurve = (YieldAndDiscountCurve) inputs.getValue(getYieldCurveMarketDataRequirement(fxOption.getCallCurrency().getUniqueId()));
    final YieldAndDiscountCurve foreignCurve = (YieldAndDiscountCurve) inputs.getValue(getYieldCurveMarketDataRequirement(fxOption.getPutCurrency().getUniqueId()));
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = foreignCurve.getInterestRate(t); //TODO not great but needs an analytics refactor
    return new StandardOptionDataBundle(domesticCurve, b, volatilitySurface, spot, now);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof OptionSecurity) {
      return target.getSecurity() instanceof FXOptionSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final FXOptionSecurity option = (FXOptionSecurity) target.getSecurity();
      final SecuritySource secMaster = context.getSecuritySource();
      final Security underlying = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier()));
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option));
      requirements.add(getYieldCurveMarketDataRequirement(option.getCallCurrency().getUniqueId()));
      requirements.add(getYieldCurveMarketDataRequirement(option.getPutCurrency().getUniqueId()));
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final OptionSecurity security = (OptionSecurity) target.getSecurity();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final String valueName : AvailableGreeks.getAllGreekNames()) {
        results.add(new ValueSpecification(new ValueRequirement(valueName, security), getUniqueId()));
      }
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GarmanKohlhagenFXOptionModelFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
