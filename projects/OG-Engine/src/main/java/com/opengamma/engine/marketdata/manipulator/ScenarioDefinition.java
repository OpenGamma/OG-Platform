/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable class defining a scenario which holds a map of the market
 * data manipulation targets (e.g. USD 3M Yield Curve) and the manipulations
 * to be performed (e.g. shift by +10bps).
 *
 * ScenarioDefinitions can be stored in the config master and used in the
 * setup of ViewDefinitions.
 */
public class ScenarioDefinition implements ScenarioDefinitionFactory {

  private static final String NAME = "name";
  private static final String SELECTOR = "selector";
  private static final String DEFINITION_MAP = "definitionMap";
  private static final String FUNCTION_PARAMETERS = "functionParameters";

  private final String _name;
  private final Map<DistinctMarketDataSelector, FunctionParameters> _definitionMap;

  public ScenarioDefinition(String name, Map<DistinctMarketDataSelector, FunctionParameters> definitionMap) {
    ArgumentChecker.notEmpty(name, "name");
    _name = name;
    _definitionMap = ImmutableMap.copyOf(definitionMap);
  }

  /**
   * Return an immutable map of the market data selectors to function parameters.
   *
   * @return market data to function parameters mapping
   */
  public Map<DistinctMarketDataSelector, FunctionParameters> getDefinitionMap() {
    return _definitionMap;
  }

  /**
   * @return The scenario name, not null
   */
  public String getName() {
    return _name;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAME, null, _name);
    MutableFudgeMsg mapMsg = serializer.newMessage();
    for (Map.Entry<DistinctMarketDataSelector, FunctionParameters> entry : _definitionMap.entrySet()) {
      MutableFudgeMsg entryMsg = serializer.newMessage();
      DistinctMarketDataSelector selector = entry.getKey();
      FunctionParameters parameters = entry.getValue();
      serializer.addToMessageWithClassHeaders(entryMsg, SELECTOR, null, selector);
      serializer.addToMessageWithClassHeaders(entryMsg, FUNCTION_PARAMETERS, null, parameters);
      serializer.addToMessage(mapMsg, null, null, entryMsg);
    }
    serializer.addToMessage(msg, DEFINITION_MAP, null, mapMsg);
    return msg;
  }

  public static ScenarioDefinition fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    String name = deserializer.fieldValueToObject(String.class, msg.getByName(NAME));
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = Maps.newHashMap();
    if (msg.hasField(DEFINITION_MAP)) {
      FudgeMsg mapMsg = msg.getMessage(DEFINITION_MAP);
      for (FudgeField field : mapMsg) {
        FudgeMsg entryMsg = (FudgeMsg) field.getValue();
        FudgeField selectorField = entryMsg.getByName(SELECTOR);
        DistinctMarketDataSelector selector = deserializer.fieldValueToObject(DistinctMarketDataSelector.class, selectorField);
        FudgeField paramsField = entryMsg.getByName(FUNCTION_PARAMETERS);
        FunctionParameters parameters = deserializer.fieldValueToObject(FunctionParameters.class, paramsField);
        definitionMap.put(selector, parameters);
      }
    }
    return new ScenarioDefinition(name, definitionMap);
  }

  @Override
  public ScenarioDefinition create(Map<String, Object> parameters) {
    return this;
  }
}
