/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

/**
 *
 */
public class ExternalIdFunction extends AbstractFunction.NonCompiledInvoker {
  /** The attribute property name */
  public static final String EXTERNAL_SCHEME_NAME = "ExternalScheme";
  private static final Logger s_logger = LoggerFactory.getLogger(ExternalIdFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties constraints = desiredValue.getConstraints();
    final String schemeName = constraints.getSingleValue(EXTERNAL_SCHEME_NAME);
    final Security security = target.getSecurity();
    if (schemeName != null) {
      final String result = security.getExternalIdBundle().getValue(ExternalScheme.of(schemeName));
      if (result == null) {
        throw new OpenGammaRuntimeException("Could not get id for scheme " + schemeName);
      }
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.EXTERNAL_ID, target.toSpecification(), constraints);
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
    final ExternalIdBundle identifiers = target.getSecurity().getExternalIdBundle();
    if (identifiers.isEmpty()) {
      return null;
    }
    final ValueProperties.Builder properties = createValueProperties();
    for (ExternalId identifier : identifiers) {
      properties.with(EXTERNAL_SCHEME_NAME, identifier.getScheme().getName());
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.EXTERNAL_ID, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> schemeNames = constraints.getValues(EXTERNAL_SCHEME_NAME);
    if (!OpenGammaCompilationContext.isPermissive(context)) {
      if (schemeNames == null || schemeNames.size() != 1) {
        s_logger.error("Did not specify a single attribute name");
        return null;
      }
    }
    return Collections.emptySet();
  }

}
