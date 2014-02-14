/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that define a curve transformation in the DSL.
 */
/* package */ final class DslYieldCurveSelectorBuilder extends YieldCurveSelector.Builder {

  /* package */ DslYieldCurveSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  @SuppressWarnings("unused")
  public void apply(Closure<?> body) {
    YieldCurveManipulatorBuilder builder = new DslYieldCurveManipulatorBuilder(getSelector(), getScenario());
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
