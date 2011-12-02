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
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
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
@FudgeBuilderFor(CompiledViewCalculationConfigurationImpl.class)
public class CompiledViewCalculationConfigurationFudgeBuilder implements FudgeBuilder<CompiledViewCalculationConfiguration> {

  private static final String NAME_FIELD = "name";
  private static final String COMPUTATION_TARGETS_FIELD = "computationTargets";
  private static final String TERMINAL_OUTPUT_SPECIFICATIONS_FIELD = "terminalOutputSpecifications";
  private static final String MARKET_DATA_REQUIREMENTS_FIELD = "marketDataRequirements";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CompiledViewCalculationConfiguration object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAME_FIELD, null, object.getName());
    serializer.addToMessage(msg, COMPUTATION_TARGETS_FIELD, null, object.getComputationTargets());
    serializer.addToMessage(msg, TERMINAL_OUTPUT_SPECIFICATIONS_FIELD, null, object.getTerminalOutputSpecifications());
    serializer.addToMessage(msg, MARKET_DATA_REQUIREMENTS_FIELD, null, object.getMarketDataRequirements());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompiledViewCalculationConfigurationImpl buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    String name = message.getString(NAME_FIELD);
    Set<ComputationTarget> computationTargets = deserializer.fieldValueToObject(Set.class, message.getByName(COMPUTATION_TARGETS_FIELD));
    Map<ValueSpecification, Set<ValueRequirement>> terminalOutputSpecifications = deserializer.fieldValueToObject(Map.class, message.getByName(TERMINAL_OUTPUT_SPECIFICATIONS_FIELD));
    Map<ValueRequirement, ValueSpecification> marketDataRequirements = deserializer.fieldValueToObject(Map.class, message.getByName(MARKET_DATA_REQUIREMENTS_FIELD));
    return new CompiledViewCalculationConfigurationImpl(name, computationTargets, terminalOutputSpecifications, marketDataRequirements);
  }

}
