/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Utilities for creating and running {@link Simulation}s.
 */
public class SimulationUtils {

  private SimulationUtils() {
  }

  /**
   * Returns the ID of the latest version of a view definition.
   * @param viewDefName The view definition name
   * @param configSource A source for looking up the view definition
   * @return The ID of the latest version of the named view definition, not null
   * @throws DataNotFoundException If no view definition is found with the specified name
   */
  public static UniqueId latestViewDefinitionId(String viewDefName, ConfigSource configSource) {
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        configSource.get(ViewDefinition.class, viewDefName, VersionCorrection.LATEST);
    if (viewDefs.isEmpty()) {
      throw new DataNotFoundException("No view definition found with name '" + viewDefName + "'");
    }
    return viewDefs.iterator().next().getValue().getUniqueId();
  }

  // TODO overload to read from a Reader for running scripts stored in config
  /**
   * Runs a Groovy script that defines a {@link Simulation} using the DSL.
   * @param groovyScript The script location in the filesystem
   * @return The simulation defined by the script
   */
  public static Simulation createSimulationFromDsl(String groovyScript, Map<String, Object> parameters) {
    return runGroovyDslScript(groovyScript, Simulation.class, parameters);
  }

    // TODO overload to read from a Reader for running scripts stored in config
  /**
   * Runs a Groovy script that defines a {@link Scenario} using the DSL.
   * @param groovyScript The script location in the filesystem
   * @return The scenario defined by the script
   */
  public static Scenario createScenarioFromDsl(String groovyScript, Map<String, Object> parameters) {
    return runGroovyDslScript(groovyScript, Scenario.class, parameters);
  }

  // TODO overload to read from a Reader for running scripts stored in config
  private static <T> T runGroovyDslScript(String scriptFile, Class<T> expectedType, Map<String, Object> parameters) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(SimulationScript.class.getName());
    Binding binding = new Binding(parameters);
    GroovyShell shell = new GroovyShell(binding, config);
    try {
      Script script = shell.parse(new File(scriptFile));
      Object scriptOutput = script.run();
      if (scriptOutput == null) {
        throw new IllegalArgumentException("Script " + scriptFile + " didn't return an object");
      }
      if (expectedType.isInstance(scriptOutput)) {
        return expectedType.cast(scriptOutput);
      } else {
        throw new IllegalArgumentException("Script '" + scriptFile + "' didn't create an object of the expected type. " +
                                               "expected type: " + expectedType.getName() + ", " +
                                               "actual type: " + scriptOutput.getClass().getName() + ", " +
                                               "actual value: " + scriptOutput);
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to open script file", e);
    }
  }
}
