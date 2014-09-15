/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

/**
 * Interface for classes that define a transformation applied to data when running a scenario.
 * <p>
 * This allows a scenario to be self-describing. The arguments describe the transformations to apply to
 * the data and also specify which functions must be added to the configuration to perform the transformations.
 * <p>
 * Scenario argument and function types exist in pairs. An argument's {@link #getFunctionType() function type}
 * should return the argument's type from {@link ScenarioFunction#getArgumentType()}.
 *
 * @param <A> this type
 * @param <F> the type of function that consumes this argument. Typically the argument instance describes the
 *   transformation that should be applied to the data and the function contains the logic to perform
 *   the transformation.
 */
public interface ScenarioArgument<A extends ScenarioArgument<A, F>, F extends ScenarioFunction<A, F>> {

  /**
   * @return  the type of the function that uses this type of argument
   */
  Class<F> getFunctionType();
}
