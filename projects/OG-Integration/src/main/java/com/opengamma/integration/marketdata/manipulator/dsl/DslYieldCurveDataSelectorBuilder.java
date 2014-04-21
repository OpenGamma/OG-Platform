/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that define a curve data transformation in the DSL.
 * This affects raw curve data before it's fitted.
 */
/* package */ final class DslYieldCurveDataSelectorBuilder extends YieldCurveDataSelectorBuilder {

  /* package */ DslYieldCurveDataSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  @SuppressWarnings("unused")
  public void apply(Closure<?> body) {
    YieldCurveDataManipulatorBuilder builder = new DslYieldCurveDataManipulatorBuilder(getSelector(), getScenario());
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
