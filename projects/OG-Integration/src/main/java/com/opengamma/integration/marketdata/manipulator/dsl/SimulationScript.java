/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Script;

/**
 * Base class for scripts that create {@link Simulation}s and {@link Scenario}s. The methods in this class are available
 * in the script and form the basis of a DSL.
 */
@SuppressWarnings("unused") // it is used reflectively by Groovy
public abstract class SimulationScript extends Script {

  /** The currently building simulation. */
  private Simulation _simulation;
  /** The currently building scenario. */
  private Scenario _scenario;

  public SimulationScript() {
    initialize();
  }

  public SimulationScript(Binding binding) {
    super(binding);
    initialize();
  }

  // TODO is there a nicer way to do this?
  private void initialize() {
    InputStream scriptStream = SimulationScript.class.getResourceAsStream("InitializeScript.groovy");
    try {
      evaluate(IOUtils.toString(scriptStream));
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to initialize DSL script", e);
    }
  }

  /**
   * Defines a method in the DSL taking a block to define the script parameters and their types. It checks the
   * parameters are available in the script's binding and that they have the expected type.
   * <pre>
   * parameters {
   *   foo String
   *   bar Number
   * }
   * </pre>
   * @param body The block that defines the script's parameters
   */
  public void parameters(Closure<?> body) {
    ParametersDelegate parametersDelegate = new ParametersDelegate();
    body.setDelegate(parametersDelegate);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    // check the parameters are all in the binding and have the expected types
    Binding binding = getBinding();
    Map<String, Class<?>> parameters = parametersDelegate.getParameters();
    for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
      String varName = entry.getKey();
      Class<?> varType = entry.getValue();
      if (!binding.hasVariable(varName)) {
        throw new DataNotFoundException("Parameter named " + varName + " not found");
      }
      Object varValue = binding.getVariable(varName);
      if (!varType.isInstance(varValue)) {
        throw new IllegalArgumentException("Parameter " + varName + " has type " + varValue.getClass().getName() + ", " +
                                                "required type is " + varType.getName());
      }
    }
  }

  /**
   * Delegate for the closure that declares the script parameters and their types.
   */
  private static class ParametersDelegate extends GroovyObjectSupport {

    /** Map of parameter names to types. */
    private final Map<String, Class<?>> _params = Maps.newHashMap();

    @Override
    public Object invokeMethod(String name, Object args) {
      ArgumentChecker.notEmpty(name, "name");
      if (!(args instanceof Object[])) {
        throw new IllegalArgumentException();
      }
      Object[] argArray = (Object[]) args;
      if (argArray.length != 1 || !argArray[0].getClass().equals(Class.class)) {
        throw new IllegalArgumentException("parameter declarations must be of the form 'var Type");
      }
      if (_params.containsKey(name)) {
        throw new IllegalArgumentException("parameter " + name + " can only be declared once");
      }
      _params.put(name, (Class<?>) argArray[0]);
      return null;
    }

    private Map<String, Class<?>> getParameters() {
      return _params;
    }
  }

  /**
   * Defines a method in the DSL that takes a closure defining a simulation.
   * @param name The simulation name
   * @param body The block that defines the simulation
   * @return The simulation
   */
  public Simulation simulation(String name, Closure<?> body) {
    _simulation = new Simulation(name);
    body.setDelegate(_simulation);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    return _simulation;
  }

  /**
   * Defines a method in the DSL that takes a closure defining a scenario.
   * @param name The scenario name, not empty
   * @param body The block that defines the scenario
   * @return The scenario
   */
  public Scenario scenario(String name, Closure<?> body) {
    // scenarios can be defined as part of a simulation or stand-alone
    if (_simulation != null) {
      _scenario = _simulation.scenario(name);
    } else {
      _scenario = new Scenario(name);
    }
    body.setDelegate(_scenario);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    return _scenario;
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a curve.
   * @param body The block that defines the selection and transformation
   */
  public void curve(Closure<?> body) {
    DslYieldCurveSelectorBuilder selector = new DslYieldCurveSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a curve.
   * @param body The block that defines the selection and transformation
   */
  public void curveData(Closure<?> body) {
    DslYieldCurveDataSelectorBuilder selector = new DslYieldCurveDataSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a market data point.
   * @param body The block that defines the selection and transformation
   */
  public void marketData(Closure<?> body) {
    DslPointSelectorBuilder selector = new DslPointSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a volatility surface.
   * @param body The block that defines the selection and transformation
   */
  public void surface(Closure<?> body) {
    DslVolatilitySurfaceSelectorBuilder selector = new DslVolatilitySurfaceSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform spot rates.
   * @param body The block that defines the selection and transformation
   */
  public void spotRate(Closure<?> body) {
    DslSpotRateSelectorBuilder builder = new DslSpotRateSelectorBuilder(_scenario);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
