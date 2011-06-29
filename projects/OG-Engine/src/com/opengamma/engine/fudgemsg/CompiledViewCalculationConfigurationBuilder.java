/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;

/**
 * Fudge message builder for {@link CompiledViewCalculationConfiguration}.
 */
@GenericFudgeBuilderFor(CompiledViewCalculationConfiguration.class)
public class CompiledViewCalculationConfigurationBuilder implements FudgeBuilder<CompiledViewCalculationConfiguration> {

  private static final String NAME_FIELD = "name";
  private static final String COMPUTATION_TARGETS_FIELD = "computationTargets";
  private static final String TERMINAL_OUTPUT_SPECIFICATIONS_FIELD = "terminalOutputSpecifications";
  private static final String MARKET_DATA_REQUIREMENTS_FIELD = "marketDataRequirements";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CompiledViewCalculationConfiguration object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, NAME_FIELD, null, object.getName());
    context.addToMessage(msg, COMPUTATION_TARGETS_FIELD, null, object.getComputationTargets());
    context.addToMessage(msg, TERMINAL_OUTPUT_SPECIFICATIONS_FIELD, null, object.getTerminalOutputSpecifications());
    context.addToMessage(msg, MARKET_DATA_REQUIREMENTS_FIELD, null, object.getMarketDataRequirements());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompiledViewCalculationConfiguration buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    String name = message.getString(NAME_FIELD);
    Set<ComputationTarget> computationTargets = context.fieldValueToObject(Set.class, message.getByName(COMPUTATION_TARGETS_FIELD));
    Set<ValueSpecification> terminalOutputSpecifications = context.fieldValueToObject(Set.class, message.getByName(TERMINAL_OUTPUT_SPECIFICATIONS_FIELD));
    Map<ValueRequirement, ValueSpecification> marketDataRequirements = context.fieldValueToObject(Map.class, message.getByName(MARKET_DATA_REQUIREMENTS_FIELD));
    return new CompiledViewCalculationConfigurationImpl(name, computationTargets, terminalOutputSpecifications, marketDataRequirements);
  }

}
