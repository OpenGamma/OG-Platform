/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;
import groovy.lang.Script;

/**
 *
 */
public abstract class SimulationScript extends Script {

  /** Set to the currently building simulation. */
  private Simulation _simulation;
  /** Set to the currently building scenario. */
  private Scenario _scenario;

  public Simulation simulation(Closure body) {
    _simulation = new Simulation();
    body.setDelegate(_simulation);
    body.call();
    return _simulation;
  }

  public Scenario scenario(String name, Closure body) {
    _scenario = _simulation.scenario(name);
    body.setDelegate(_scenario);
    body.call();
    return _scenario;
  }

  public void curve(Closure body) {
    CurveBuilder selector = new CurveBuilder(_scenario);
    body.setDelegate(selector);
    body.call();
  }

  public void marketData(Closure body) {
    PointBuilder selector = new PointBuilder(_scenario);
    body.setDelegate(selector);
    body.call();
  }

  public void surface(Closure body) {
    SurfaceBuilder selector = new SurfaceBuilder(_scenario);
    body.setDelegate(selector);
    body.call();
  }

  private static class SurfaceBuilder extends VolatilitySurfaceSelector.Builder {

    private SurfaceBuilder(Scenario scenario) {
      super(scenario);
    }

    public void apply(Closure body) {
      VolatilitySurfaceManipulatorBuilder builder = new VolatilitySurfaceManipulatorBuilder(getScenario(), getSelector());
      body.setDelegate(builder);
      body.call();
    }
  }

  private static class PointBuilder extends PointSelector.Builder {

    private PointBuilder(Scenario scenario) {
      super(scenario);
    }

    public void apply(Closure body) {
      PointManipulatorBuilder builder = new PointManipulatorBuilder(getScenario(), getSelector());
      body.setDelegate(builder);
      body.call();
    }
  }

  private static class CurveBuilder extends YieldCurveSelector.Builder {

    private CurveBuilder(Scenario scenario) {
      super(scenario);
    }

    public void apply(Closure body) {
      YieldCurveManipulatorBuilder builder = new YieldCurveManipulatorBuilder(getSelector(), getScenario());
      body.setDelegate(builder);
      body.call();
    }
  }
}
