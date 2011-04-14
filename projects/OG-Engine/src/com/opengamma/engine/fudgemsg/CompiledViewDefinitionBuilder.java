/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;

/**
 * Fudge message builder for {@link CompiledViewDefinition}
 */
@GenericFudgeBuilderFor(CompiledViewDefinition.class)
public class CompiledViewDefinitionBuilder implements FudgeBuilder<CompiledViewDefinition> {

  private static final String VIEW_DEFINITION_FIELD = "viewDefinition";
  private static final String PORTFOLIO_FIELD = "portfolio";
  private static final String LIVE_DATA_REQUIREMENTS_FIELD = "liveDataRequirements";
  private static final String OUTPUT_VALUE_NAMES_FIELD = "outputValueNames";
  private static final String COMPUTATION_TARGETS_FIELD = "computationTargets";
  private static final String SECURITY_TYPES_FIELD = "securityTypes";
  private static final String EARLIEST_VALIDITY_FIELD = "earliestValidity";
  private static final String LATEST_VALIDITY_FIELD = "latestValidity";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CompiledViewDefinition object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, VIEW_DEFINITION_FIELD, null, object.getViewDefinition());
    context.addToMessage(msg, PORTFOLIO_FIELD, null, object.getPortfolio());
    context.addToMessage(msg, LIVE_DATA_REQUIREMENTS_FIELD, null, object.getLiveDataRequirements());
    context.addToMessage(msg, OUTPUT_VALUE_NAMES_FIELD, null, object.getOutputValueNames());
    context.addToMessage(msg, COMPUTATION_TARGETS_FIELD, null, object.getComputationTargets());
    context.addToMessage(msg, SECURITY_TYPES_FIELD, null, object.getSecurityTypes());
    context.addToMessage(msg, EARLIEST_VALIDITY_FIELD, null, object.getValidFrom());
    context.addToMessage(msg, LATEST_VALIDITY_FIELD, null, object.getValidTo());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompiledViewDefinition buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ViewDefinition viewDefinition = context.fieldValueToObject(ViewDefinition.class, message.getByName(VIEW_DEFINITION_FIELD));
    Portfolio portfolio = context.fieldValueToObject(Portfolio.class, message.getByName(PORTFOLIO_FIELD));
    Map<ValueRequirement, ValueSpecification> liveDataRequirements = context.fieldValueToObject(Map.class, message.getByName(LIVE_DATA_REQUIREMENTS_FIELD));
    Set<String> outputValueNames = context.fieldValueToObject(Set.class, message.getByName(OUTPUT_VALUE_NAMES_FIELD));
    Set<ComputationTarget> computationTargets = context.fieldValueToObject(Set.class, message.getByName(COMPUTATION_TARGETS_FIELD));
    Set<String> securityTypes = context.fieldValueToObject(Set.class, message.getByName(SECURITY_TYPES_FIELD));
    FudgeField earliestValidityField = message.getByName(EARLIEST_VALIDITY_FIELD);
    Instant earliestValidity = earliestValidityField != null ? context.fieldValueToObject(Instant.class, earliestValidityField) : null;
    FudgeField latestValidityField = message.getByName(LATEST_VALIDITY_FIELD);
    Instant latestValidity = latestValidityField != null ? context.fieldValueToObject(Instant.class, latestValidityField) : null;
    return new CompiledViewDefinitionImpl(viewDefinition, portfolio, liveDataRequirements, outputValueNames, computationTargets, securityTypes, earliestValidity, latestValidity);
  }

}
