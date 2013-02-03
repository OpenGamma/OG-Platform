/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
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

  private static final Integer MAP_KEY = 1;
  private static final Integer MAP_VALUE = 2;

  protected void encodeComputationTargets(final FudgeSerializer serializer, final MutableFudgeMsg msg, final Set<ComputationTargetSpecification> targets) {
    final MutableFudgeMsg submsg = msg.addSubMessage(COMPUTATION_TARGETS_FIELD, null);
    for (final ComputationTargetSpecification target : targets) {
      serializer.addToMessage(submsg, null, null, target);
    }
  }

  protected Set<ComputationTargetSpecification> decodeComputationTargets(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeMsg submsg = msg.getMessage(COMPUTATION_TARGETS_FIELD);
    if (submsg == null) {
      return Collections.emptySet();
    }
    final Set<ComputationTargetSpecification> result = Sets.newHashSetWithExpectedSize(submsg.getNumFields());
    for (final FudgeField field : submsg) {
      result.add(deserializer.fieldValueToObject(ComputationTargetReference.class, field).getSpecification());
    }
    return result;
  }

  protected void encodeTerminalOutputSpecifications(final FudgeSerializer serializer, final MutableFudgeMsg msg, final Map<ValueSpecification, Set<ValueRequirement>> outputs) {
    final MutableFudgeMsg submsg = msg.addSubMessage(TERMINAL_OUTPUT_SPECIFICATIONS_FIELD, null);
    for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> output : outputs.entrySet()) {
      serializer.addToMessage(submsg, null, MAP_KEY, output.getKey());
      final MutableFudgeMsg submsg2 = submsg.addSubMessage(null, MAP_VALUE);
      for (final ValueRequirement requirement : output.getValue()) {
        serializer.addToMessage(submsg2, null, null, requirement);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<ValueSpecification, Set<ValueRequirement>> decodeTerminalOutputSpecifications(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeMsg submsg = msg.getMessage(TERMINAL_OUTPUT_SPECIFICATIONS_FIELD);
    if (submsg == null) {
      return Collections.emptyMap();
    }
    final Map<ValueSpecification, Set<ValueRequirement>> result = Maps.newHashMapWithExpectedSize(submsg.getNumFields() / 2);
    LinkedList<Object> overflow = null;
    ValueSpecification key = null;
    Set<ValueRequirement> value = null;
    for (final FudgeField field : submsg) {
      if (MAP_KEY.equals(field.getOrdinal())) {
        final ValueSpecification fieldValue = deserializer.fieldValueToObject(ValueSpecification.class, field);
        if (key == null) {
          if (value == null) {
            key = fieldValue;
          } else {
            result.put(fieldValue, value);
            if (overflow != null) {
              value = overflow.isEmpty() ? null : (Set<ValueRequirement>) overflow.removeFirst();
            } else {
              value = null;
            }
          }
        } else {
          if (overflow == null) {
            overflow = new LinkedList<Object>();
          }
          overflow.add(fieldValue);
        }
      } else if (MAP_VALUE.equals(field.getOrdinal())) {
        final FudgeMsg submsg2 = (FudgeMsg) field.getValue();
        final Set<ValueRequirement> fieldValue = Sets.newHashSetWithExpectedSize(submsg2.getNumFields());
        for (final FudgeField field2 : submsg2) {
          fieldValue.add(deserializer.fieldValueToObject(ValueRequirement.class, field2));
        }
        if (value == null) {
          if (key == null) {
            value = fieldValue;
          } else {
            result.put(key, fieldValue);
            if (overflow != null) {
              key = overflow.isEmpty() ? null : (ValueSpecification) overflow.removeFirst();
            } else {
              key = null;
            }
          }
        } else {
          if (overflow == null) {
            overflow = new LinkedList<Object>();
          }
          overflow.add(fieldValue);
        }
      }
    }
    return result;
  }

  protected void encodeMarketDataRequirements(final FudgeSerializer serializer, final MutableFudgeMsg msg, final Map<ValueRequirement, ValueSpecification> marketData) {
    final MutableFudgeMsg submsg = msg.addSubMessage(MARKET_DATA_REQUIREMENTS_FIELD, null);
    for (final Map.Entry<ValueRequirement, ValueSpecification> requirement : marketData.entrySet()) {
      serializer.addToMessage(submsg, null, 1, requirement.getKey());
      serializer.addToMessage(submsg, null, 2, requirement.getValue());
    }
  }

  protected Map<ValueRequirement, ValueSpecification> decodeMarketDataRequirements(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeMsg submsg = msg.getMessage(MARKET_DATA_REQUIREMENTS_FIELD);
    if (submsg == null) {
      return Collections.emptyMap();
    }
    final Map<ValueRequirement, ValueSpecification> result = Maps.newHashMapWithExpectedSize(submsg.getNumFields() / 2);
    LinkedList<Object> overflow = null;
    ValueRequirement key = null;
    ValueSpecification value = null;
    for (final FudgeField field : submsg) {
      if (MAP_KEY.equals(field.getOrdinal())) {
        final ValueRequirement fieldValue = deserializer.fieldValueToObject(ValueRequirement.class, field);
        if (key == null) {
          if (value == null) {
            key = fieldValue;
          } else {
            result.put(fieldValue, value);
            if (overflow != null) {
              value = overflow.isEmpty() ? null : (ValueSpecification) overflow.removeFirst();
            } else {
              value = null;
            }
          }
        } else {
          if (overflow == null) {
            overflow = new LinkedList<Object>();
          }
          overflow.add(fieldValue);
        }
      } else if (MAP_VALUE.equals(field.getOrdinal())) {
        final ValueSpecification fieldValue = deserializer.fieldValueToObject(ValueSpecification.class, field);
        if (value == null) {
          if (key == null) {
            value = fieldValue;
          } else {
            result.put(key, fieldValue);
            if (overflow != null) {
              key = overflow.isEmpty() ? null : (ValueRequirement) overflow.removeFirst();
            } else {
              key = null;
            }
          }
        } else {
          if (overflow == null) {
            overflow = new LinkedList<Object>();
          }
          overflow.add(fieldValue);
        }
      }
    }
    return result;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CompiledViewCalculationConfiguration object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAME_FIELD, null, object.getName());
    encodeComputationTargets(serializer, msg, object.getComputationTargets());
    encodeTerminalOutputSpecifications(serializer, msg, object.getTerminalOutputSpecifications());
    encodeMarketDataRequirements(serializer, msg, object.getMarketDataRequirements());
    return msg;
  }

  @Override
  public CompiledViewCalculationConfigurationImpl buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final Set<ComputationTargetSpecification> computationTargets = decodeComputationTargets(deserializer, message);
    final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputSpecifications = decodeTerminalOutputSpecifications(deserializer, message);
    final Map<ValueRequirement, ValueSpecification> marketDataRequirements = decodeMarketDataRequirements(deserializer, message);
    return new CompiledViewCalculationConfigurationImpl(name, computationTargets, terminalOutputSpecifications, marketDataRequirements);
  }

}
