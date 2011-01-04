/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
public class AsayMarginedFutureOptionModelFunction extends BlackScholesMertonModelFunction {

  @Override
  protected StandardOptionDataBundle getDataBundle(final SecuritySource secMaster, final Clock relevantTime, final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    final Security underlying = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier()));
    final Double spotAsObject = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
    if (spotAsObject == null) {
      throw new NullPointerException("No spot value for underlying instrument.");
    }
    final double spot = spotAsObject;
    final YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(option));
    final double b = 0;
    return new StandardOptionDataBundle(curve, b, volatilitySurface, spot, now);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof OptionSecurity) {
      if (target.getSecurity() instanceof FutureOptionSecurity) {
        return ((FutureOptionSecurity) target.getSecurity()).getIsMargined();
      }
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final OptionSecurity option = (OptionSecurity) target.getSecurity();
      final SecuritySource secMaster = context.getSecuritySource();
      final Security underlying = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier()));
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option));
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
        results.add(new ValueSpecification(new ValueRequirement(valueName, security), getUniqueIdentifier()));
      }
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "AsayMarginedFutureOptionModelFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
