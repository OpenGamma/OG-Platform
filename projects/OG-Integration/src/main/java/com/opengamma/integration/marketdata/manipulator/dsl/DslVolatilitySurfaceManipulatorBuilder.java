/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 *
 */
/* package */ final class DslVolatilitySurfaceManipulatorBuilder extends VolatilitySurfaceManipulatorBuilder {

  /* package */ DslVolatilitySurfaceManipulatorBuilder(Scenario scenario, VolatilitySurfaceSelector selector) {
    super(scenario, selector);
  }

  public void shifts(ScenarioShiftType shiftType, Closure<?> body) {
    VolatilitySurfaceShiftManipulatorBuilder builder =
        new VolatilitySurfaceShiftManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }
}
