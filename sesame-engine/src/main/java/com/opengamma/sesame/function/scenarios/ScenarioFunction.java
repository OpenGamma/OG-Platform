/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

/**
 * Interface for functions that implement scenarios by decorating other functions and transforming their output.
 * <p>
 * A scenario decorator must implement this interface plus one additional interface. The other interface is
 * the function that it will decorate. Additionally the decorator implementation must have a constructor that
 * takes an instance of the other interface as an argument. This is the decorated function.
 *
 * @param <F> this type
 * @param <A> the type of argument this function requires. Typically the argument instance describes the
 *   transformation that should be applied to the data and the function contains the logic to perform
 *   the transformation.
 *
 * @deprecated use the new scenario framework
 */
@Deprecated
public interface ScenarioFunction<A extends ScenarioArgument<A, F>, F extends ScenarioFunction<A, F>> {

  /**
   * Returns the type of the scenario arguments this decorator uses.
   * <p>
   * The argument should return this type from {@link ScenarioArgument#getFunctionType()}
   * <p>
   * Arguments should be provided in the environment and are requested by the decorator when it is invoked.
   * Decorator implementations should be written to handle the case when there are no suitable arguments
   * provided in the environment.
   *
   * @return  the type of the scenario arguments this decorator requires
   */
  Class<A> getArgumentType();
}
