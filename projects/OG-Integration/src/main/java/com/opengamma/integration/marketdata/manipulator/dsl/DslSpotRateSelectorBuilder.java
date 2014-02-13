/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that define a spot rate transformation in the DSL.
 */
/* package */ final class DslSpotRateSelectorBuilder extends SpotRateSelectorBuilder {

  /* package */ DslSpotRateSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  public void apply(Closure<?> body) {
    SpotRateManipulatorBuilder builder = new SpotRateManipulatorBuilder(getScenario(), getSelector());
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
