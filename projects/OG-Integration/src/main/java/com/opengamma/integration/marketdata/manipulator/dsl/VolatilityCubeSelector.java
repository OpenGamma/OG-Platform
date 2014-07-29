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
 * Selects volatility cubes for manipulation.
 */
public class VolatilityCubeSelector extends Selector {

  /* package */ VolatilityCubeSelector(Set<String> calcConfigNames,
                                       Set<String> names,
                                       Set<Currency> currencies,
                                       Pattern nameMatchPattern,
                                       Pattern nameLikePattern) {
    super(calcConfigNames,
          names,
          currencies,
          nameMatchPattern,
          nameLikePattern);
  }

  @Override
  boolean matches(ValueSpecification valueSpecification) {
    if (!ValueRequirementNames.VOLATILITY_CUBE.equals(valueSpecification.getValueName())) {
      return false;
    }
    Currency currency = Currency.parse(valueSpecification.getTargetSpecification().getUniqueId().getValue());
    String cube = valueSpecification.getProperties().getStrictValue(ValuePropertyNames.CUBE);
    if (cube == null) {
      return false;
    }
    return matches(cube, currency);
  }

  /**
   * Mutable builder for {@link VolatilityCubeSelector} instances.
   */
  public static class Builder extends Selector.Builder {

    /* package */ Builder(Scenario scenario) {
      super(scenario);
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
      return new VolatilityCubeSelector(getScenario().getCalcConfigNames(),
                                        getNames(),
                                        getCurrencies(),
                                        getNameMatchPattern(),
                                        getNameLikePattern());
    }
  }
}
