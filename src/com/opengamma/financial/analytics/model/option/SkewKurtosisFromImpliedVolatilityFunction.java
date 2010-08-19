/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;

/**
 * 
 *
 */
public class SkewKurtosisFromImpliedVolatilityFunction extends OptionSkewKurtosisFunction {
  /**
   * Name of value requirement for Skew.
   */
  public static final String SKEW = "Skew";
  /**
   * Name of value requirement for Kurtosis.
   */
  public static final String KURTOSIS = "Kurtosis";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final UniqueIdentifier uid = option.getUniqueIdentifier();
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final VolatilitySurface surface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceRequirement(option));
    final double volatility = surface.getVolatility(Pair.of(t, option.getStrike()));
    final double y = Math.sqrt(Math.exp(volatility * volatility * t) - 1);
    final double ySq = y * y;
    final double skew = y * (3 + ySq);
    final double kurtosis = 16 * ySq + 15 * ySq * ySq + 6 * ySq * ySq * ySq + ySq * ySq * ySq * ySq;
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(new ComputedValue(new ValueSpecification(
        new ValueRequirement(SKEW, ComputationTargetType.SECURITY, uid),
        getUniqueIdentifier()), 
      skew));
    results.add(new ComputedValue(new ValueSpecification(
        new ValueRequirement(KURTOSIS, ComputationTargetType.SECURITY, uid),
        getUniqueIdentifier()), 
      kurtosis));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(getVolatilitySurfaceRequirement((OptionSecurity) target.getSecurity()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SkewKurtosisFromImpliedVolatilityModel";
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final OptionSecurity option) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, option.getUniqueIdentifier());
  }

}
