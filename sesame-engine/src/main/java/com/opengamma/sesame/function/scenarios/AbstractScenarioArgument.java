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
public class AbstractScenarioArgument<T extends ScenarioFunction<?>> implements ScenarioArgument<T> {

  /** The type of scenario function that consumes this argument. */
  private final Class<T> _functionType;

  /**
   * @param functionType  the type of scenario function that consumes this argument
   */
  protected AbstractScenarioArgument(Class<T> functionType) {
    _functionType = ArgumentChecker.notNull(functionType, "functionType");
  }

  @Override
  public Class<T> getFunctionType() {
    return _functionType;
  }
}
