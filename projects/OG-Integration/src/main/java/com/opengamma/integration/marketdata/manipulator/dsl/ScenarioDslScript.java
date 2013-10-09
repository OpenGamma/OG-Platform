/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.StringReader;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.config.Config;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinitionFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Configuration object containing a script to define a scenario. The script is written in the Groovy-based
 * scenario DSL.
 */
@Config(description = "Scenario DSL script")
public class ScenarioDslScript implements ScenarioDefinitionFactory {

  /** Field name for Fudge message */
  private static final String SCRIPT = "script";

  /** Script text. */
  private final String _script;

  public ScenarioDslScript(String script) {
    ArgumentChecker.notEmpty(script, "script");
    _script = script;
  }

  @Override
  public ScenarioDefinition create(Map<String, Object> parameters) {
    return SimulationUtils.createScenarioFromDsl(new StringReader(_script), parameters).createDefinition();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SCRIPT, null, _script);
    return msg;
  }

  public static ScenarioDslScript fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    String script = deserializer.fieldValueToObject(String.class, msg.getByName(SCRIPT));
    return new ScenarioDslScript(script);
  }

}

