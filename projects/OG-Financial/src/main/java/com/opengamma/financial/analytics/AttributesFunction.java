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
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class AttributesFunction extends AbstractFunction.NonCompiledInvoker {
  /** The attribute property name */
  public static final String PROPERTY_ATTRIBUTE_NAME = "AttributeName";
  private static final Logger s_logger = LoggerFactory.getLogger(AttributesFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String attributeName = desiredValue.getConstraint(PROPERTY_ATTRIBUTE_NAME);
    final Security security = target.getSecurity();
    final Map<String, String> attributes = security.getAttributes();
    final String result = attributes.get(attributeName);
    if (result == null) {
      throw new OpenGammaRuntimeException("Could not get value for attribute " + attributeName);
    }
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_ATTRIBUTE_NAME, attributeName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.ATTRIBUTES, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_ATTRIBUTE_NAME).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.ATTRIBUTES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> attributeNames = constraints.getValues(PROPERTY_ATTRIBUTE_NAME);
    if (attributeNames == null || attributeNames.size() != 1) {
      s_logger.error("Did not specify a single attribute name");
      return null;
    }
    return Collections.emptySet();
  }

}
