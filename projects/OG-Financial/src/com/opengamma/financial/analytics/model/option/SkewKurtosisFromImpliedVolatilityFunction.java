/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.statistics.descriptive.LognormalPearsonKurtosisFromVolatilityCalculator;
import com.opengamma.analytics.math.statistics.descriptive.LognormalSkewnessFromVolatilityCalculator;
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
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;

/**
 * 
 *
 */
public class SkewKurtosisFromImpliedVolatilityFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Function2D<Double, Double> SKEW_CALCULATOR = new LognormalSkewnessFromVolatilityCalculator();
  private static final Function2D<Double, Double> KURTOSIS_CALCULATOR = new LognormalPearsonKurtosisFromVolatilityCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final UniqueId uid = option.getUniqueId();
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final VolatilitySurface surface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceRequirement(option));
    final double volatility = surface.getVolatility(Pair.of(t, option.getStrike()));
    final double skew = SKEW_CALCULATOR.evaluate(volatility, t);
    final double pearson = KURTOSIS_CALCULATOR.evaluate(volatility, t);
    final double fisher = pearson - 3;
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY, uid), getUniqueId()), skew));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()), pearson));
    results.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()), fisher));
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(getVolatilitySurfaceRequirement((EquityOptionSecurity) target.getSecurity()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SkewKurtosisFromImpliedVolatilityModel";
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final EquityOptionSecurity option) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, option.getUniqueId());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final UniqueId uid = target.getSecurity().getUniqueId();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY, uid), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()));
      results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FISHER_KURTOSIS, ComputationTargetType.SECURITY, uid), getUniqueId()));
      return results;
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
