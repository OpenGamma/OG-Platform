/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class ExternalIdFunction extends AbstractFunction.NonCompiledInvoker {
  /** The attribute property name */
  public static final String EXTERNAL_SCHEME_NAME = "ExternalScheme";
  private static final Logger s_logger = LoggerFactory.getLogger(ExternalIdFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String schemeName = desiredValue.getConstraint(EXTERNAL_SCHEME_NAME);
    final Security security = target.getSecurity();
    if (schemeName != null) {
      final String result = security.getExternalIdBundle().getValue(ExternalScheme.of(schemeName));
      if (result == null) {
        throw new OpenGammaRuntimeException("Could not get id for scheme " + schemeName);
      }
      final ValueProperties properties = createValueProperties()
          .with(EXTERNAL_SCHEME_NAME, schemeName).get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.EXTERNAL_ID, target.toSpecification(), properties);
      return Collections.singleton(new ComputedValue(spec, result));
    } else {
      throw new OpenGammaRuntimeException("Could not get id for scheme " + schemeName);
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(EXTERNAL_SCHEME_NAME).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.EXTERNAL_ID, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> schemeNames = constraints.getValues(EXTERNAL_SCHEME_NAME);
    if (schemeNames == null || schemeNames.size() != 1) {
      s_logger.error("Did not specify a single attribute name");
      return null;
    }
    return Collections.emptySet();
  }

}
