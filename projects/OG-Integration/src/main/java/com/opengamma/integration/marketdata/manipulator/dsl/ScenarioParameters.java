/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.core.config.Config;
import com.opengamma.util.ArgumentChecker;

/**
 * Config object for storing parameters required to build a scenario.
 */
@Config
public class ScenarioParameters {

  /** The parameters, keyed by name. */
  private final Map<String, String> _parameters;

  private ScenarioParameters(Map<String, String> parameters) {
    ArgumentChecker.notNull(parameters, "parameters");
    _parameters = ImmutableMap.copyOf(parameters);
  }

  /**
   * @return The parameters, keyed by name
   */
  public Map<String, String> getParameters() {
    return _parameters;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (Map.Entry<String, String> entry : _parameters.entrySet()) {
      serializer.addToMessage(msg, entry.getKey(), null, entry.getValue());
    }
    return msg;
  }

  public static ScenarioParameters fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Map<String, String> parameters = Maps.newHashMap();
    for (FudgeField field : msg) {
      parameters.put(field.getName(), deserializer.fieldValueToObject(String.class, field));
    }
    return new ScenarioParameters(parameters);
  }

}
