/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that defines closure compatible builder methods
 * for {@link YieldCurveManipulatorBuilder} in the DSL.
 */
/* package */ final class DslYieldCurveManipulatorBuilder extends YieldCurveManipulatorBuilder {

  /* package */ DslYieldCurveManipulatorBuilder(YieldCurveSelector selector, Scenario scenario) {
    super(selector, scenario);
  }

  @SuppressWarnings("unused")
  public void bucketedShifts(ScenarioShiftType shiftType, Closure<?> body) {
    BucketedShiftManipulatorBuilder builder =
        new BucketedShiftManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

  @SuppressWarnings("unused")
  public void pointShifts(ScenarioShiftType shiftType, Closure<?> body) {
    YieldCurvePointShiftManipulatorBuilder builder = new YieldCurvePointShiftManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

}
