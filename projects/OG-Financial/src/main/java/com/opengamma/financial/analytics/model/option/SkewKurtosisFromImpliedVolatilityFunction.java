/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.statistics.descriptive.LognormalPearsonKurtosisFromVolatilityCalculator;
import com.opengamma.analytics.math.statistics.descriptive.LognormalSkewnessFromVolatilityCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 * @deprecated ...
 */
@Deprecated
public class SkewKurtosisFromImpliedVolatilityFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Function2D<Double, Double> SKEW_CALCULATOR = new LognormalSkewnessFromVolatilityCalculator();
  private static final Function2D<Double, Double> KURTOSIS_CALCULATOR = new LognormalPearsonKurtosisFromVolatilityCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
    final Expiry expiry = option.getExpiry();
    final double t = DateUtils.getDifferenceInYears(now, expiry.getExpiry());
    final VolatilitySurface surface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceRequirement(option));
    final double volatility = surface.getVolatility(DoublesPair.of(t, option.getStrike()));
    final double skew = SKEW_CALCULATOR.evaluate(volatility, t);
    final double pearson = KURTOSIS_CALCULATOR.evaluate(volatility, t);
    final double fisher = pearson - 3;
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties().get();
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.SKEW, targetSpec, properties), skew));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PEARSON_KURTOSIS, targetSpec, properties), pearson));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.FISHER_KURTOSIS, targetSpec, properties), fisher));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(getVolatilitySurfaceRequirement((EquityOptionSecurity) target.getSecurity()));
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
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties().get();
    results.add(new ValueSpecification(ValueRequirementNames.SKEW, targetSpec, properties));
    results.add(new ValueSpecification(ValueRequirementNames.PEARSON_KURTOSIS, targetSpec, properties));
    results.add(new ValueSpecification(ValueRequirementNames.FISHER_KURTOSIS, targetSpec, properties));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

}
