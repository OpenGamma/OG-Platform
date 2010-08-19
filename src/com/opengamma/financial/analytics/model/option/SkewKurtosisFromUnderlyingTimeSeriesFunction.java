/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 *
 */
public class SkewKurtosisFromUnderlyingTimeSeriesFunction extends OptionSkewKurtosisFunction {
  private final Function1D<DoubleTimeSeries<?>, Double> _skewCalculator;
  private final Function1D<DoubleTimeSeries<?>, Double> _kurtosisCalculator;

  public SkewKurtosisFromUnderlyingTimeSeriesFunction(final Function1D<DoubleTimeSeries<?>, Double> skewCalculator, final Function1D<DoubleTimeSeries<?>, Double> kurtosisCalculator) {
    _skewCalculator = skewCalculator;
    _kurtosisCalculator = kurtosisCalculator;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final UniqueIdentifier uid = option.getUniqueIdentifier();
    // TODO finish this off
    // DoubleTimeSeries<?> ts = time series of underlying.
    final double skew = 0; // _skewCalculator.evaluate(ts);
    final double kurtosis = 0; // _kurtosisCalculator.evaluate(ts);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(new ComputedValue(new ValueSpecification(
        new ValueRequirement(SKEW, ComputationTargetType.SECURITY, uid),
        getUniqueIdentifier()), 
      skew));
    results.add(new ComputedValue(new ValueSpecification(
        new ValueRequirement(KURTOSIS, ComputationTargetType.SECURITY, uid),
        getUniqueIdentifier()), kurtosis));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    // TODO
    // time series of underlying
    return null;
  }

  @Override
  public String getShortName() {
    return "SkewKurtosisFromUnderlyingTimeSeries";
  }

}
