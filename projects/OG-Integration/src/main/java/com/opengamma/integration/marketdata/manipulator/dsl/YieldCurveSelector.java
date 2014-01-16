/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Selects yield curves for manipulation.
 */
public class YieldCurveSelector extends Selector {

  /* package */ YieldCurveSelector(Set<String> calcConfigNames,
                                   Set<String> names,
                                   Set<Currency> currencies,
                                   Pattern nameMatchPattern,
                                   Pattern nameLikePattern) {
    super(calcConfigNames, names, currencies, nameMatchPattern, nameLikePattern);
  }

  @Override
  boolean matches(ValueSpecification valueSpecification) {
    if (!ValueRequirementNames.YIELD_CURVE.equals(valueSpecification.getValueName())) {
      return false;
    }
    Currency currency = Currency.of(valueSpecification.getTargetSpecification().getUniqueId().getValue());
    String curve = valueSpecification.getProperties().getStrictValue(ValuePropertyNames.CURVE);
    if (curve == null) {
      return false;
    }
    return matches(curve, currency);
  }

  /**
   * Mutable builder for creating {@link YieldCurveSelector} instances.
   */
  public static class Builder extends Selector.Builder {

    /* package */ Builder(Scenario scenario) {
      super(scenario);
    }

    public YieldCurveManipulatorBuilder apply() {
      return new YieldCurveManipulatorBuilder(getSelector(), getScenario());
    }

    @Override
    public Builder named(String... names) {
      super.named(names);
      return this;
    }

    @Override
    public Builder currencies(String... codes) {
      super.currencies(codes);
      return this;
    }

    @Override
    public Builder nameMatches(String regex) {
      super.nameMatches(regex);
      return this;
    }

    /**
     * This is package scoped for testing
     * @return A selector built from this builder's data
     */
    /* package */ YieldCurveSelector getSelector() {
      return new YieldCurveSelector(getScenario().getCalcConfigNames(),
                                    getNames(),
                                    getCurrencies(),
                                    getNameMatchPattern(),
                                    getNameLikePattern());
    }
  }
}
