/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.function;

import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.engine.value.ValueRequirementNames.BLOOMBERG_TICKER;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * If attached to security's ExternalIdBundle, displays its {@link ExternalSchemes.BLOOMBERG_TICKER}
 */
public class BloombergTickerFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext,
                                    final FunctionInputs inputs,
                                    ComputationTarget target,
                                    Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    ValueRequirement desiredValue = functional(desiredValues).first();
    ValueSpecification valueSpecification = ValueSpecification.of(desiredValue.getValueName(),
                                                                  target.toSpecification(),
                                                                  desiredValue.getConstraints());
    Security security = target.getPositionOrTrade().getSecurity();
    if (security != null) {
      ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
      if (externalIdBundle != null) {
        ExternalId externalId = externalIdBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
        if (externalId != null) {
          return newHashSet(new ComputedValue(valueSpecification, externalId.getValue()));
        }
      }
    }
    return newHashSet(new ComputedValue(valueSpecification, ""));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(
        new ValueSpecification(BLOOMBERG_TICKER, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return ImmutableSet.of();
  }

}
