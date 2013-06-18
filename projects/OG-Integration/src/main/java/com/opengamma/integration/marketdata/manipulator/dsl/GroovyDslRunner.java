/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.opengamma.OpenGammaRuntimeException;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 *
 */
public class GroovyDslRunner {

  public static Simulation runScript(String scriptFile) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(SimulationScript.class.getName());
    GroovyShell shell = new GroovyShell(config);
    Script script;
    try {
      script = shell.parse(new File(scriptFile));
      Object scriptOutput = script.run();
      if (scriptOutput instanceof Simulation) {
        return (Simulation) scriptOutput;
      } else {
        throw new IllegalArgumentException("Script " + scriptFile + " didn't create a simulation. output=" + scriptOutput);
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to open script file", e);
    }
  }
}
