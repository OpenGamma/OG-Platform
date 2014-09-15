/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base class for {@link ScenarioArgument} implementations that contains a field for the function type.
 */
public class AbstractScenarioArgument<A extends ScenarioArgument<A, F>, F extends ScenarioFunction<A, F>>
    implements ScenarioArgument<A, F> {

  /** The type of scenario function that consumes this argument. */
  private final Class<F> _functionType;

  /**
   * @param functionType  the type of scenario function that consumes this argument
   */
  protected AbstractScenarioArgument(Class<F> functionType) {
    _functionType = ArgumentChecker.notNull(functionType, "functionType");
  }

  @Override
  public Class<F> getFunctionType() {
    return _functionType;
  }
}
