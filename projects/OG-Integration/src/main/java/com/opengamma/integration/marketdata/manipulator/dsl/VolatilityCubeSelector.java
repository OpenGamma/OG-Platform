/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.engine.marketdata.manipulator.StructureType;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class VolatilityCubeSelector extends Selector<VolatilityCubeKey> {

  /* package */ VolatilityCubeSelector(String calcConfigName, Set<String> names, Set<Currency> currencies, Pattern namePattern) {
    super(calcConfigName, names, currencies, namePattern, VolatilityCubeKey.class, StructureType.VOLATILITY_CUBE);
  }

  @Override
  boolean matches(VolatilityCubeKey key) {
    return matches(key.getName(), key.getCurrency());
  }


  public static class Builder extends Selector.Builder {

    /* package */ Builder(Scenario scenario, String calcConfigName) {
      super(scenario, calcConfigName);
    }

    public VolatilityCubeManipulatorBuilder apply() {
      return new VolatilityCubeManipulatorBuilder(selector(), getScenario());
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
    /* package */ VolatilityCubeSelector selector() {
      return new VolatilityCubeSelector(getCalcConfigName(), getNames(), getCurrencies(), getNamePattern());
    }
  }
}
