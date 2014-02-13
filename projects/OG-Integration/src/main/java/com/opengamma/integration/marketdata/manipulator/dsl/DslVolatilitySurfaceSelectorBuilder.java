/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that define a surface transformation in the DSL.
 */
/* package */ final class DslVolatilitySurfaceSelectorBuilder extends VolatilitySurfaceSelector.Builder {

  /* package */ DslVolatilitySurfaceSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  @SuppressWarnings("unused")
  public void apply(Closure<?> body) {
    DslVolatilitySurfaceManipulatorBuilder builder =
        new DslVolatilitySurfaceManipulatorBuilder(getScenario(), getSelector());
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
