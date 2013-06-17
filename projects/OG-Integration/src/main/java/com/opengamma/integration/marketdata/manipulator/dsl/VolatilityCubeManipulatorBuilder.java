/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class VolatilityCubeManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final VolatilityCubeSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  public VolatilityCubeManipulatorBuilder(VolatilityCubeSelector selector, Scenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  // TODO what transformations? what class will they operate on? are there standard transformers for cubes?
}
