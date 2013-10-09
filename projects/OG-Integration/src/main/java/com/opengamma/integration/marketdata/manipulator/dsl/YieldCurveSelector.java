/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.util.money.Currency;

/**
 * Selects yield curves for manipulation.
 */
public class YieldCurveSelector extends Selector<YieldCurveKey> {

  /* package */ YieldCurveSelector(Set<String> calcConfigNames,
                                   Set<String> names,
                                   Set<Currency> currencies,
                                   Pattern nameMatchPattern,
                                   Pattern nameLikePattern) {
    super(calcConfigNames,
          names,
          currencies,
          nameMatchPattern,
          nameLikePattern,
          YieldCurveKey.class,
          StructureType.YIELD_CURVE);
  }

  @Override
  boolean matches(YieldCurveKey key) {
    return matches(key.getName(), key.getCurrency());
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
