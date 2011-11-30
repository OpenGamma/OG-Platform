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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * Function for the Black-Scholes stock option function (i.e. equity option, no dividends)
 */
public class AsayMarginedFutureOptionModelFunction extends BlackScholesMertonModelFunction {

  @Override
  protected StandardOptionDataBundle getDataBundle(final SecuritySource secMaster, final Clock relevantTime, final EquityOptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    final Security underlying = secMaster.getSecurity(ExternalIdBundle.of(option.getUnderlyingId()));
    final Double spotAsObject = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
    if (spotAsObject == null) {
      throw new NullPointerException("No spot value for underlying instrument.");
    }
    final double spot = spotAsObject;
    final YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE);
    final double b = 0;
    return new StandardOptionDataBundle(curve, b, volatilitySurface, spot, now);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof EquityOptionSecurity) {
      //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be 
      /*
      if (target.getSecurity() instanceof FutureOptionSecurity) {
        return ((FutureOptionSecurity) target.getSecurity()).getIsMargined();
      }
      */
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    }
    final String curveName = curveNames.iterator().next();
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final SecuritySource secMaster = context.getSecuritySource();
    final Security underlying = secMaster.getSecurity(ExternalIdBundle.of(option.getUnderlyingId()));
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getUnderlyingMarketDataRequirement(underlying.getUniqueId()));
    requirements.add(getVolatilitySurfaceMarketDataRequirement(option, curveName));
    return requirements;
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
