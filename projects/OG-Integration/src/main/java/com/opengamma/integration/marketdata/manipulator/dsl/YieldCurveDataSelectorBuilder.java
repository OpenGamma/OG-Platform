/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

/**
 *
 */
public class YieldCurveDataSelectorBuilder extends Selector.Builder {

  /* package */ YieldCurveDataSelectorBuilder(Scenario scenario) {
    super(scenario);
  }

  public YieldCurveDataManipulatorBuilder apply() {
    return new YieldCurveDataManipulatorBuilder(getSelector(), getScenario());
  }

  @Override
  public YieldCurveDataSelectorBuilder named(String... names) {
    super.named(names);
    return this;
  }

  @Override
  public YieldCurveDataSelectorBuilder currencies(String... codes) {
    super.currencies(codes);
    return this;
  }

  @Override
  public YieldCurveDataSelectorBuilder nameMatches(String regex) {
    super.nameMatches(regex);
    return this;
  }

  /**
   * This is package scoped for testing
   * @return A selector built from this builder's data
   */
    /* package */ YieldCurveDataSelector getSelector() {
    return new YieldCurveDataSelector(getScenario().getCalcConfigNames(),
                                      getNames(),
                                      getCurrencies(),
                                      getNameMatchPattern(),
                                      getNameLikePattern());
  }
}
