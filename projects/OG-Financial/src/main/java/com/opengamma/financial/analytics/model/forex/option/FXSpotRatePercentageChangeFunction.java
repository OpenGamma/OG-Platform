/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import static com.opengamma.financial.analytics.model.forex.option.FXOptionSpotRateFunction.LAST_CLOSE;
import static com.opengamma.financial.analytics.model.forex.option.FXOptionSpotRateFunction.LIVE;
import static com.opengamma.financial.analytics.model.forex.option.FXOptionSpotRateFunction.PROPERTY_DATA_TYPE;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class FXSpotRatePercentageChangeFunction extends AbstractFunction.NonCompiledInvoker {
  private static final DecimalFormat FORMAT = new DecimalFormat("##.###");
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement liveRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(), ValueProperties.with(PROPERTY_DATA_TYPE, LIVE).get());
    final Object liveObject = inputs.getValue(liveRequirement);
    if (liveObject == null) {
      throw new OpenGammaRuntimeException("Could not get live value");
    }
    final ValueRequirement lastCloseRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
        ValueProperties.with(PROPERTY_DATA_TYPE, LAST_CLOSE).get());
    final Object lastCloseObject = inputs.getValue(lastCloseRequirement);
    if (lastCloseObject == null) {
      throw new OpenGammaRuntimeException("Could not get last close value");
    }
    final double live = (Double) liveObject;
    final double lastClose = (Double) lastCloseObject;
    final String change = FORMAT.format(100 * (live - lastClose) / lastClose) + "%";
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_FX_PERCENTAGE_CHANGE, target.toSpecification(), createValueProperties().get()), change));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SPOT_FX_PERCENTAGE_CHANGE, target.toSpecification(),
        createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueRequirement liveRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(), ValueProperties.with(PROPERTY_DATA_TYPE, LIVE).get());
    final ValueRequirement lastCloseRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
        ValueProperties.with(PROPERTY_DATA_TYPE, LAST_CLOSE).get());
    return Sets.newHashSet(liveRequirement, lastCloseRequirement);
  }

}
