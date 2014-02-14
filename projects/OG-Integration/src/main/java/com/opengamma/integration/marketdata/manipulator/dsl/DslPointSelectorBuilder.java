/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for blocks that define a market data point transformation in the DSL.
 */
/* package */ final class DslPointSelectorBuilder extends PointSelector.Builder {

  /* package */ DslPointSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  @SuppressWarnings("unused")
  public void apply(Closure<?> body) {
    PointManipulatorBuilder builder = new PointManipulatorBuilder(getScenario(), getSelector());
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
