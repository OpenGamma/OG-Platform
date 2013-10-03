/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.StringReader;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.config.Config;
import com.opengamma.engine.marketdata.manipulator.ScenarioParameters;
import com.opengamma.util.ArgumentChecker;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Config object for storing parameters required to build a scenario. This object stores a Groovy script which is
 * executed to populate the parameter values. The script should be of the form:
 * <pre>
 * aString = "FOO"
 * aList = [1, 2, 3]
 * aMap = [key1: "val1", key2: "val2"]
 * </pre>
 */
@Config(description = "Scenario DSL parameters")
public class ScenarioDslParameters implements ScenarioParameters {

  /** Field name for Fudge message */
  private static final String SCRIPT = "script";

  /** The script that populates the parameters. */
  private final String _script;

  /* package */ ScenarioDslParameters(String script) {
    ArgumentChecker.notEmpty(script, "script");
    _script = script;
  }

  /**
   * @return The parameters, keyed by name
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getParameters() {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(SimulationScript.class.getName());
    GroovyShell shell = new GroovyShell(config);
    Script script = shell.parse(new StringReader(_script));
    script.run();
    return script.getBinding().getVariables();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SCRIPT, null, _script);
    return msg;
  }

  public static ScenarioDslParameters fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    String script = deserializer.fieldValueToObject(String.class, msg.getByName(SCRIPT));
    return new ScenarioDslParameters(script);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScenarioDslParameters that = (ScenarioDslParameters) o;

    if (!_script.equals(that._script)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _script.hashCode();
  }
}
